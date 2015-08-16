package com.server.impl.processor.task;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;

import com.asl.utils.Constants;
import com.asl.utils.MessageParser.MessageType;
import com.asl.utils.QueryName;
import com.asl.utils.Status;
import com.server.impl.database.DBManager;
import com.server.impl.processor.MessageFields;
import com.sun.rowset.CachedRowSetImpl;

@SuppressWarnings("restriction")
public abstract class Task {
	private static final Logger LOGGER = Logger.getLogger(Task.class
			.getCanonicalName());

	private static final int MAX_TENTATIVE = 3;
	private static final int DELAY_BETWEEN_TENTATIVES = 100;

	protected int client_id = -1; // not yet specified
	protected boolean success = true;
	protected QueryName type = QueryName.ABSTRACT;

	public abstract void doTask(DBManager db, HashMap<String, String> headers);

	protected void prepareAnswer(ResultSet rs, HashMap<String, String> headers)
			throws SQLException {
		headers.put(MessageType.Response.toString(), Status.SUCCESS.toString());
		headers.put(Constants.CLIENT_ID_LABEL, "" + client_id);
	}

	protected void handleError(HashMap<String, String> headers, SQLException e) {
		headers.put(MessageType.Response.toString(), Status.FAILURE.toString());
		LOGGER.log(
				Level.SEVERE,
				"Client " + client_id + " encountered a DB Error for "
						+ this.toString() + ": " + e.getMessage(), e);
		success = false;
	}

	protected void completeTask() {
		if (success) {
			LOGGER.log(Level.FINE,
					"Client {0} completed the following task: {1}",
					new Object[] { client_id, this.toString() });
		}
	}

	protected ResultSet getMessRecordFromDB(PreparedStatement s)
			throws SQLException {
		MessageFields m = null;
		CachedRowSet crset = null;
		int compt = 0;
		do {
			compt++;
			crset = new CachedRowSetImpl();
			crset.populate(s.executeQuery());
			if (crset.next()){
				m = MessageFields.readMessage(crset.getString(1));
			}
			
			if (m == null && compt < MAX_TENTATIVE) {
				try {
					if (crset != null){
						crset.close();
					}
					Thread.sleep(DELAY_BETWEEN_TENTATIVES);
				} catch (InterruptedException e) {
					LOGGER.log(Level.SEVERE,"Client "+ client_id + " was interrupted in "+ this.toString()
									+ " while sleeping before sending the request another time: "
									+ e.getMessage(), e);
				}
			}
		} while (m == null && compt < MAX_TENTATIVE);
		crset.beforeFirst();
		return crset;
	}

	@Override
	public String toString() {
		return this.getClass().getCanonicalName();
	}

	public int getClientId() {
		return client_id;
	}

	public QueryName getType() {
		return type;
	}

	public String getQueuesId() {
		return "[]";
	}
}
