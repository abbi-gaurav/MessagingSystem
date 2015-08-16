package com.server.impl.processor.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;
import com.server.impl.database.DBManager;
import com.server.impl.processor.MessageFields;

public class CheckMessageFrom extends Task {
	private static final Logger LOGGER = Logger.getLogger(CheckMessageFrom.class.getCanonicalName());

	private int sender_id;

	public CheckMessageFrom(int sid, int cid) {
		sender_id = sid;
		client_id = cid;
		type = QueryName.CHECK_MESSAGE_FROM;
	}

	@Override
	public void doTask(DBManager db, HashMap<String, String> headers) {
		Connection con = null;
		try {
			con = db.getConnection();
			PreparedStatement s = db
					.getQuery(QueryName.CHECK_MESSAGE_FROM, con);
			s.setInt(1, sender_id);
			s.setInt(2, client_id);
			prepareAnswer(getMessRecordFromDB(s), headers);
			s.close();
		} catch (SQLException e) {
			handleError(headers, e);
		} finally {
			db.releaseConnection(con);
			completeTask();
		}
	}

	@Override
	protected void prepareAnswer(ResultSet rs, HashMap<String, String> headers)
			throws SQLException {
		super.prepareAnswer(rs, headers);
		MessageFields m = null;
		if(rs.next())
			m = MessageFields.readMessage(rs.getString(1));
		
		if (rs != null)
			rs.close();
		String none = Constants.NONE;
		headers.put(Constants.BODY_LABEL, m==null ? none : m.getBody());
		headers.put(Constants.MESSAGE_ID_LABEL, m==null ? none : m.getId()+"");
		headers.put(Constants.CONTEXT_LABEL, m==null||m.isOneWay()?none:""+m.getContext());

		LOGGER.log(Level.FINE, "Client {0} is checking for a message from sender {1}: {2} ", new Object[]{client_id,sender_id,(m==null ? none : m.toString())});
	}
	
	@Override
	public String toString(){
		return this.getClass().getCanonicalName() + " from "+sender_id+".";
	}
	
}
