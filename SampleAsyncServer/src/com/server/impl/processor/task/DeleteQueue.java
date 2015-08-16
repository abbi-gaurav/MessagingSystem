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


public class DeleteQueue extends Task {
	private static final Logger LOGGER = Logger.getLogger(DeleteQueue.class.getCanonicalName());

	private Integer queues_id[];
	
	public DeleteQueue(int cid, Integer id[]) {
		client_id=cid;
		queues_id=id;
		type = QueryName.DELETE_QUEUE;
	}

	@Override
	public void doTask(DBManager db, HashMap<String, String> headers){
		Connection con = null;
		try {
			con = db.getConnection();
			PreparedStatement s = db.getQuery(QueryName.DELETE_QUEUE, con);
			s.setArray(1, con.createArrayOf("integer", queues_id));
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
	protected void prepareAnswer(ResultSet rs, HashMap<String, String> headers) throws SQLException{
		super.prepareAnswer(rs, headers);
		headers.put(Constants.QUEUE_ID_LABEL, Arrays.toString(queues_id));
		LOGGER.log(Level.FINE, "Deleted queue: {0}", new Object[]{Arrays.toString(queues_id)});
		//TODO: will say this even if nothing was deleted (eg wrong queue_id)
	}
	
	@Override
	public String toString(){
		return this.getClass().getCanonicalName() + " queues "+Arrays.toString(queues_id)+".";
	}
	
	@Override
	public String getQueuesId() {
		return Arrays.toString(queues_id);
	}
}
