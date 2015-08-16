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
import com.server.impl.processor.MessageFields;


public class Post extends Task {
	protected static final Logger LOGGER = Logger.getLogger(Post.class.getCanonicalName());
	private MessageFields mess;
	private Integer queues_id[];
	private boolean broadcast;
	private int new_mess_id;
	
	public Post(MessageFields m, Integer qid[]){
		//TODO: check if client can send;
		mess=m;
		client_id = mess.getFromId();
		queues_id=qid;
		broadcast = false;
		type = QueryName.POST;
	}
	
	public Post(MessageFields m){
		this(m,null);
		broadcast=true;
		type = QueryName.BROADCAST;
	}
	
	@Override
	public void doTask(DBManager db, HashMap<String, String> headers) {
		Connection con = null;
		try {
			con = db.getConnection();
			PreparedStatement s;
			if(broadcast){
				LOGGER.log(Level.FINE, "Doing broadcast");
				s = db.getQuery(QueryName.BROADCAST, con);
			}
			else{
				s = db.getQuery(QueryName.POST, con);
				s.setArray(6, con.createArrayOf("integer", queues_id));
			}
			mess.prepareStmt(s, 0);
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
		if(rs.next())
			new_mess_id = rs.getInt(1);
			
		if (rs != null)
			rs.close();
		headers.put(Constants.MESSAGE_ID_LABEL, ""+new_mess_id);
		LOGGER.log(Level.FINE, "Client {0} successfully posted message {1}"+ (broadcast ? " (broadcast)":"in queues {2}"),
				new Object[]{client_id, new_mess_id, Arrays.toString(queues_id) });
	}
	
	@Override
	public String toString(){
		String m = broadcast ? "in all queues" : "in queues "+Arrays.toString(queues_id);
		return this.getClass().getCanonicalName() + " "+mess.toString()+" "+m+".";
	}
	
	@Override
	public String getQueuesId() {
		if(broadcast)
			return "[]";
		else
			return Arrays.toString(queues_id);
	}
}
