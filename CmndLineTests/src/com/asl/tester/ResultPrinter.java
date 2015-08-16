package com.asl.tester;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;

public class ResultPrinter<T extends ResultValues> implements Runnable{
	private static final Logger LOGGER = Logger.getLogger(ResultPrinter.class.getCanonicalName());
	private BlockingQueue<T> queue;
	private final int clientId;
	PrintStream ps;
	public volatile boolean isRunning = true;
	
	public ResultPrinter(int clientId, BlockingQueue<T> queue,String expmntId, QueryName reqquestType, int port) throws Exception {
		this.queue = queue;
		this.clientId = clientId;
		
		ps = new PrintStream(Constants.USER_HOME+Constants.PATH_SEPERATOR
				+Constants.TEST_RUN_DIR+Constants.PATH_SEPERATOR
				+Constants.LOGS_DIR+Constants.PATH_SEPERATOR
				+Constants.PERF_RUNS_DIR+Constants.PATH_SEPERATOR
				+expmntId+Constants.PATH_SEPERATOR
				+Constants.CLIENT_ID_LABEL+"_"+clientId+"_"+reqquestType.toString()+"_"+port+"_"
				+System.currentTimeMillis()+"_"
				+Constants.PERF_RESULT_TXT_FILE);
		ps.println("Constants.CLIENT_ID_LABEL:::"+this.clientId);
	}

	@Override
	public void run() {
		Calendar start = Calendar.getInstance();
		ps.println("test start time::"+start.getTime());
		
		printHeader();
		ps.println();
		long count = 0;
		long failedCount = 0;
		try(PrintStream printStream = ps){
			while(isRunning){
				T resultValue = queue.poll(2, TimeUnit.SECONDS);
				if(resultValue != null){
					printResultRow(resultValue);
					ps.println();
					
					count++;
					
					if(resultValue.operationTime == -1){
						failedCount++;
					}
				}
			}
			ps.println();
			ps.println("Total number of requests server: "+count);
			ps.println("Failed number of requests server: "+failedCount);
			Calendar end = Calendar.getInstance();
			ps.println("Test end time: "+end.getTime());
			ps.println("Approx test run time in seconds: "+(end.getTimeInMillis() - start.getTimeInMillis())/1000);
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, "Request print  for client id: "+clientId, e);
			return;
		}
	}

	void printResultRow(T resultValue) {
		ps.format("%s;%d;%d", resultValue.taskType.toString(),
				resultValue.queueId,resultValue.operationTime);
	}

	void printHeader() {
		ps.format("%20s%20s%32s", 
				Constants.QUERY_TYPE_LABEL,Constants.QUEUE_ID_LABEL,Constants.TIME_TAKEN);
	}
}
