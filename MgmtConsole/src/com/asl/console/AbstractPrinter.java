package com.asl.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;

import com.asl.utils.Constants;
import com.server.impl.database.DBManager;

public abstract class AbstractPrinter implements Printable{

	final Object[] headers;
	final String format;
	final DBManager dbMgr;
	
	public AbstractPrinter(DBManager dbMgr, String format,Object... headers){
		this.format = format;
		this.headers = headers;
		this.dbMgr = dbMgr;
	}
	
	public static String readChoice() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String choice = reader.readLine();
		
		if(choice == null || choice.trim().length() < 1){
			return Constants.NEXT;
		}
		
		if(Constants.QUIT.equalsIgnoreCase(choice)){
			System.exit(0);
		}
		
		return choice;
	}

	@Override
	public void print() throws IOException {
		ResultSet rs = fetchData();
		
		System.out.format(format, headers);
		
		//this should iterate for all the rows
		printRow(rs);
		
		System.out.println();
		
		printHelpMessage();
		String choice = readChoice();
		
		clearConsole();
		getPrinter(choice).print();
	}

	void clearConsole() throws IOException {
		String clearScreenCommand = null;
		if( System.getProperty( Constants.OS_NAME ).startsWith( Constants.WINDOW ) ){
		    clearScreenCommand = Constants.CLS;
		}
		else{
		    clearScreenCommand = Constants.CLEAR;
		}
		Runtime.getRuntime().exec( clearScreenCommand );
	}

	
	public abstract ResultSet fetchData();
	
	public abstract void printRow(ResultSet rs);

}