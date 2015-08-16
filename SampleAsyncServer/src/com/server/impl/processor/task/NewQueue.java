package com.server.impl.processor.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;
import com.server.impl.database.DBManager;

public class NewQueue extends Task {
	private static final Logger LOGGER = Logger.getLogger(NewQueue.class.getCanonicalName());

	private int queue_id;
	
	public NewQueue(int cid){
		client_id=cid;
		type = QueryName.NEW_QUEUE;
	}

	@Override
	public void doTask(DBManager db, HashMap<String, String> headers) {
		Connection con = null;
		try {
			con = db.getConnection();
			PreparedStatement s = db.getQuery(QueryName.NEW_QUEUE, con);
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
		
		if(rs.next())
			queue_id = rs.getInt(1);
		if (rs != null)
			rs.close();
		headers.put(Constants.QUEUE_ID_LABEL, ""+queue_id);
		LOGGER.log(Level.FINE, "The new queue id is {0}", new Object[]{queue_id});
	}
	
	@Override
	public String toString(){
		return this.getClass().getCanonicalName() + ". The newly created queue is "+queue_id+".";
	}
	
	@Override
	public QueryName getType() {
		return QueryName.NEW_QUEUE;
	}
	
	@Override
	public String getQueuesId() {
		return Arrays.toString(new Integer[]{queue_id});
	}
}
