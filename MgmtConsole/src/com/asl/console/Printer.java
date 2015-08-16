package com.asl.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;

import com.asl.utils.Constants;
import com.server.impl.database.DBManager;

public abstract class Printer extends AbstractPrinter {
	
	final int start;
	final int end;
	
	public Printer(DBManager d, int start, int end, String format,Object... headers) {
		super(d, format, headers);
		this.start = start;
		this.end = end;
	}
	
	/* (non-Javadoc)
	 * @see com.asl.console.Printable#print()
	 */
	@Override
	public void print() throws IOException{
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
	
}
