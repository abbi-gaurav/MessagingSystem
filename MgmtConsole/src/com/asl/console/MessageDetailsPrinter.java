package com.asl.console;

import java.io.IOException;
import java.nio.channels.IllegalSelectorException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;
import com.asl.utils.Utils;
import com.server.impl.database.DBManager;

public class MessageDetailsPrinter extends AbstractPrinter{
	private final String messageId;
	private final String queueName;

	public MessageDetailsPrinter(DBManager dbMgr, String messageId, String queueName) {
		//TODO: add any other fields as required and then modify format accordingly
		//"%25s%10s%10s%10s%40s%30s%10s"
		//(290,166,,,9,"2013-10-19 09:11:45.897824","Sample Message body",103)
		super(dbMgr, Constants.MESSAGE_DETAIL_FORMAT_HEADER, 
				(Object[])Constants.MSG_DETAIL_FORMAT_HEADERS );
		
		this.messageId = messageId;
		this.queueName = queueName;
	}
	
	@Override
	public void print() throws IOException{
		System.out.println(Constants.MESSAGE_BODY);
		System.out.println();
		super.print();
		//TODO:print message body
	}
	
	@Override
	public void printHelpMessage() {
		System.out.println("Enter <BACK> to go back to the Messages List Queue details");
		System.out.println(Constants.ENTER_QUIT_TO_EXIT);
	}

	public Printable getPrinter(String choice) {
		switch (choice) {
		case Constants.BACK:
			return new MessagesPrinter(dbMgr,queueName,0, 20, Constants.MESSAGES_FORMAT_HEADER, 
													(Object[])Constants.MSG_DETAIL_FORMAT_HEADERS);
		default:
			throw new IllegalSelectorException();
		}
	}

	@Override
	public ResultSet fetchData() {
		Connection con = null;
		try {
			con = dbMgr.getConnection();
			PreparedStatement s = dbMgr.getQuery(QueryName.MGMT_FETCH_MESSAGE_DETAILS, con);
			s.setInt(1, Integer.parseInt(messageId));
			return s.executeQuery();
		} catch (SQLException e) {
			dbMgr.releaseConnection(con);
			throw new RuntimeException(e);
		} finally{
			dbMgr.releaseConnection(con);
		}

	}

	@Override
	public void printRow(ResultSet rs) {
		try{
			//(290,166,,,9,"2013-10-19 09:11:45.897824","Sample Message body",103)
			if(rs.next()){
				System.out.println();
				System.out.format(this.format,(Object[]) Utils.convertDBResultSet(rs.getString(1)));
			}
			System.out.println();
		} catch(SQLException e){
			throw new RuntimeException(e);
		}
	}
}
