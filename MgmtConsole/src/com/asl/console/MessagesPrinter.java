package com.asl.console;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;
import com.asl.utils.Utils;
import com.server.impl.database.DBManager;

public class MessagesPrinter extends Printer{

	private final String queueName;

	public MessagesPrinter(DBManager d, String queueName, int start, int end, String format, Object... headers) {
		super(d, start, end, format, headers);
		this.queueName = queueName;
	}
	
	@Override
	public void print() throws IOException {
		System.out.println(Constants.QUEUE_ID_LABEL+":: "+ queueName);
		super.print();
	}
	
	@Override
	public ResultSet fetchData() {
		Connection con = null;
		try {
			con = dbMgr.getConnection();
			PreparedStatement s = dbMgr.getQuery(QueryName.MGMT_FETCH_MESSAGES, con);
			s.setInt(1, start);
			s.setInt(2, end);
			s.setInt(3, Integer.parseInt(queueName));
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
			//(73,13,9,"2013-10-19 07:49:06.836027",42,)
			while(rs.next()){
				// MESSAGES_FORMAT_HEADER 		= "%10s%10s%10s%40s%10s";
				System.out.println();
				System.out.format(Constants.MESSAGES_FORMAT_HEADER, (Object[])Utils.convertDBResultSet(rs.getString(1)));
			}
			System.out.println();
		} catch(SQLException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public Printable getPrinter(String choice) {
		switch (choice) {
		case Constants.NEXT:
			return new MessagesPrinter(dbMgr, queueName,start+20, end+20,format, headers );
		case Constants.BACK:
			return new QueuesPrinter(dbMgr, 0, 20, 
					Constants.QUEUES_PRINT_FORMAT_HEADER, (Object[])Constants.QUEUES_PRINT_FORMAT_HEADERS);
		default:
			return new MessageDetailsPrinter(dbMgr,choice, queueName);
		}
	}
	
	@Override
	public void printHelpMessage() {
		System.out.println("Enter <NEXT> for fetching next set of messages");
		System.out.println("Enter <MessageId> for viewing a particular messages");
		System.out.println("Enter <BACK> to go back to the Queues list");
		
		System.out.println(Constants.ENTER_QUIT_TO_EXIT);
	}

}
