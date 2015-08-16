package com.asl.console;

import java.io.IOException;
import java.sql.SQLException;

import com.asl.utils.Constants;
import com.server.impl.database.DBManager;

public class MgmtConsole {
	public static void main(String[] args) throws IOException, SQLException {
		if(args.length < 3){
			printHelp();
		}
		try {
			String dbHost = args[0];
			String portNumString = args[1];
			String userName = args[2];
			DBManager db = new DBManager(5, dbHost, Integer.parseInt(portNumString), userName);
			new QueuesPrinter(db,0, 20, 
					Constants.QUEUES_PRINT_FORMAT_HEADER, (Object[])Constants.QUEUES_PRINT_FORMAT_HEADERS).print();
		} catch (Exception e) {
			printHelp();
		}
	}

	private static void printHelp() {
		System.out.println("usage : java MgmtConsole <db host> <db port> <db user name>");
	}

}
