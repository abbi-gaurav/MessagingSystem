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

public class ListQueue extends Task {
	private static final Logger LOGGER = Logger.getLogger(ListQueue.class.getCanonicalName());
	private String queues;
	public ListQueue(int cid){
		client_id=cid;
		type = QueryName.LIST_QUEUE;
	}

	@Override
	public void doTask(DBManager db, HashMap<String, String> headers) {
		Connection con = null;
		try {
			con = db.getConnection();
			PreparedStatement s = db.getQuery(QueryName.LIST_QUEUE, con);
			prepareAnswer(s.executeQuery(), headers);
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
		queues="[";
		boolean empty=true;
		while (rs.next()) {
			queues += rs.getInt(1) + ", ";
			empty=false;
		}
		queues=queues.substring(0,empty?1:queues.lastIndexOf(", "))+"]";
		if (rs != null)
			rs.close();
		headers.put(Constants.QUEUE_ID_LABEL, queues);
		LOGGER.log(Level.FINE, "Queues currently in the db: {0}", new Object[]{queues});
	}

	@Override
	public String toString(){
		return this.getClass().getCanonicalName();
	}
	
	@Override
	public String getQueuesId() {
		return queues;
	}
}
