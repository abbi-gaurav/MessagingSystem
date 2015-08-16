package com.asl.console;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;
import com.asl.utils.Utils;
import com.server.impl.database.DBManager;

public class QueuesPrinter extends Printer{

	public QueuesPrinter(DBManager d, int start, int end, String format, Object... headers) {
		super(d, start, end, format, headers);
	}

	@Override
	public Printable getPrinter(String choice) {
		switch (choice) {
		case Constants.NEXT:
			return new QueuesPrinter(dbMgr, start+20, end+20,format, headers );
		default:
			return new MessagesPrinter(dbMgr, choice, 0, 20, 
					Constants.MESSAGES_FORMAT_HEADER, (Object[])Constants.MSGS_FORMAT_HEADERS);
		}
	}

	@Override
	public ResultSet fetchData() {
		Connection con = null;
		try {
			con = dbMgr.getConnection();
			PreparedStatement s = dbMgr.getQuery(QueryName.MGMT_FETCH_QUEUES, con);
			s.setInt(1, start);
			s.setInt(2, end);
			return s.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			dbMgr.releaseConnection(con);
		}

		return null;
	}

	@Override
	public void printRow(ResultSet rs) {
		try{
			
			while(rs.next()){
				System.out.println();
				System.out.format(this.format, (Object[])Utils.convertDBResultSet(rs.getString(1)));
			}
		} catch(SQLException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public void printHelpMessage() {
		System.out.println("Enter <NEXT> for fetching next of rows");
		System.out.println("Enter <Queue Name> for viewing messages for a Queue");
		
		System.out.println(Constants.ENTER_QUIT_TO_EXIT);
	}

}
