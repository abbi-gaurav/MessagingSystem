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

public class Read extends Task {
	private static final Logger LOGGER = Logger.getLogger(Read.class.getCanonicalName());

	private int queue_id;

	public Read(int qid, int cid) {
		queue_id = qid;
		client_id = cid;
		type= QueryName.READ;
	}

	@Override
	public void doTask(DBManager db, HashMap<String, String> headers) {
		Connection con = null;
		try {
			con = db.getConnection();
			PreparedStatement s = db.getQuery(QueryName.READ, con);
			s.setInt(1, queue_id);
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
		headers.put(Constants.MESSAGE_ID_LABEL, m==null?none:""+m.getId());
		headers.put(Constants.QUEUE_ID_LABEL, ""+queue_id);
		headers.put(Constants.CONTEXT_LABEL, m==null||m.isOneWay()?none:""+m.getContext());
		LOGGER.log(Level.FINE, "Client {0} is reading a message {1} from queue {2} ",
						new Object[]{client_id, m==null ? "none" : m, queue_id});
	}

	/*//VERSION WHERE READ RETURNED ALL MESS AVAILABLE IN QUEUE
	@Override
	protected void prepareAnswer(ResultSet rs, HashMap<String, String> headers)
			throws SQLException {
		super.prepareAnswer(rs, headers);

		String res = "Client " + client_id + " is reading messages from queue "
				+ queue_id + ":\n";
		String q="",records="";
		MessageFields m = null;
		boolean empty=true;
		while (rs.next()) {
			String r = rs.getString(1);
			m = MessageFields.readMessage(r);
			if(m!=null){
				records+=r+"\n";
				q += m.getId() + ", ";
				res+=m.toString()+"\n";
			}
			empty=false;
		}
		q=q.substring(0,empty?0:q.lastIndexOf(", "))+"";
		if (rs != null)
			rs.close();
		headers.put(Constants.BODY_LABEL,records);
		headers.put(Constants.MESSAGE_ID_LABEL, q);
		LOGGER.log(Level.FINE, res);
	}
	*/

	@Override
	public String toString(){
		return this.getClass().getCanonicalName() + " on queue "+queue_id+".";
	}
	
	@Override
	public String getQueuesId() {
		return Arrays.toString(new Integer[]{queue_id});
	}
}
