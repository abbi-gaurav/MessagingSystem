package com.server.impl.processor.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.QueryName;
import com.server.impl.database.DBManager;

public class NewClient extends Task {
	private static final Logger LOGGER = Logger.getLogger(NewClient.class.getCanonicalName());

	private String role;

	public NewClient(String r) {
		role = r;
		type = QueryName.NEW_CLIENT;
	}

	@Override
	public void doTask(DBManager db, HashMap<String, String> headers) {
		Connection con = null;
		try {
			con = db.getConnection();
			PreparedStatement s = db.getQuery(QueryName.NEW_CLIENT, con);
			s.setString(1, role);
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

		if(rs.next())
			client_id = rs.getInt(1);
		
		if (rs != null)
			rs.close();

		super.prepareAnswer(rs, headers);	//adds the new client_id to the headers
		
		LOGGER.log(Level.FINE, "The new {0} client id is {1}", new Object[]{role,client_id});
	}

	@Override
	public String toString(){
		return this.getClass().getCanonicalName() + ". The newly created client is "+client_id+".";
	}
}
