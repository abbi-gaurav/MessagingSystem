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
import com.asl.utils.ReceiveBy;
import com.server.impl.database.DBManager;
import com.server.impl.processor.MessageFields;

public class RetrieveMessage extends Task {
	private static final Logger LOGGER = Logger.getLogger(RetrieveMessage.class.getCanonicalName());

	private int queue_id;
	private boolean order_by_time;

	public RetrieveMessage(int qid, int cid, ReceiveBy order) {
		queue_id = qid;
		client_id = cid;
		order_by_time = order==ReceiveBy.Timestamp;
		type = QueryName.RETRIEVE_MESSAGE;
	}

	//default order by priority
	public RetrieveMessage(int qid, int cid) {
		this(qid, cid, ReceiveBy.Priority);
	}

	@Override
	public void doTask(DBManager db, HashMap<String, String> headers) {
		Connection con = null;
		try {
			con = db.getConnection();
			PreparedStatement s = db.getQuery(QueryName.RETRIEVE_MESSAGE, con);
			s.setInt(1, queue_id);
			s.setInt(2, client_id);
			s.setBoolean(3, order_by_time);
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
		headers.put(Constants.MESSAGE_ID_LABEL, m==null ? none : ""+m.getId());
		headers.put(Constants.QUEUE_ID_LABEL, ""+queue_id);
		headers.put(Constants.CONTEXT_LABEL, m==null||m.isOneWay()?none:""+m.getContext());
		headers.put(Constants.FROM_LABEL, m==null ? none : m.getFromId()+"");
		LOGGER.log(Level.FINE, "Client {0} is retrieving a message {1} from queue {2} ",
				new Object[]{client_id, m==null ? "none" : m, queue_id});
	}
	
	@Override
	public String toString(){
		return this.getClass().getCanonicalName() + " on queue "+queue_id+" and order by time "+order_by_time+".";
	}
	
	@Override
	public String getQueuesId() {
		return Arrays.toString(new Integer[]{queue_id});
	}
}
