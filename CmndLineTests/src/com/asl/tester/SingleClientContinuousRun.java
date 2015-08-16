package com.asl.tester;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.client.io.ClientIO;
import com.asl.client.io.IClientIO;
import com.asl.utils.DataConstants;
import com.asl.utils.DataConstants.MsgLength;
import com.asl.utils.Message;
import com.asl.utils.QueryName;

public class SingleClientContinuousRun<T extends ResultValues, R extends ResultPrinter<T>> {
	private static final Logger LOGGER = Logger.getLogger(SingleClientContinuousRun.class.getCanonicalName());
	public static void main(String[] args) {
		if(args.length < 8){
			printHelp();
			System.exit(1);
		}
		try{
			String middlewareHost = args[0];
			int middlewarePort = Integer.parseInt(args[1]);
			
			QueryName requestType = QueryName.valueOf(args[2]);
			
			int clientId = Integer.parseInt(args[3]);
			int queueId = Integer.parseInt(args[4]);
			DataConstants.MsgLength msgLength = DataConstants.MsgLength.valueOf(args[5]);
			
			long runTimeSeconds = Integer.parseInt(args[6]);
			String expmntId = args[7];
			
			IClientIO clientIO = new ClientIO(InetAddress.getByName(middlewareHost), middlewarePort);
			TestDelegator testDelegator = new TestDelegator(clientIO, clientId);
			if(queueId == -1){
				queueId = testDelegator.doCreateQueue();
			}
			
			SingleClientContinuousRun<ResultValues, ResultPrinter<ResultValues>> runner = new SingleClientContinuousRun<>();
			runner.runTest(testDelegator, requestType,msgLength,runTimeSeconds, expmntId, queueId);
		}catch(Exception e){
			e.printStackTrace();
			printHelp();
		}
	}

	public static void printHelp() {
		System.out.println("usage:");
		System.out.println("<MiddlewareRemoteHostAddress> <PORT> "
				+ "<RequestType>"
				+ "<clientId> (-1 for new client)"
				+"<queueId> (-1 for new queue)"
				+"<Message Length>"
				+ "<run Time in seconds>"
				+"<experiment id>"
				);
		System.out.println("valid Length options are:");
		System.out.println(Arrays.toString(MsgLength.values()));

		System.out.println("valid request types are:");
		System.out.println(Arrays.toString(QueryName.values()));
	}

	public void runTest(TestDelegator testDelegator, QueryName requestType, MsgLength msgLength,
			long runTimeSeconds,String expmntId, int queueId) throws Exception {
		long totalTimeInMs = runTimeSeconds * 1000;
		boolean toFinish = false;
		long startTime = System.currentTimeMillis();
		
		BlockingQueue<T> queue = new ArrayBlockingQueue<>(10);
		ResultPrinter<T> printer = createPrinter(testDelegator, requestType, expmntId, queue);
		
		new Thread(printer).start();
		
		long count = 0;
		long failedCount = 0;
		
		while(!toFinish){
			boolean isFailed = !perform(testDelegator, requestType, msgLength, queue, queueId);
			count++;
			if(isFailed){
				failedCount++;
			}
			toFinish = ((System.currentTimeMillis() - startTime) >= totalTimeInMs);
		}
		printer.isRunning = false;
		System.out.println("Total Number of requests: "+count);
		System.out.println("Failed Requests::"+failedCount);
	}

	private boolean perform(TestDelegator testDelegator, QueryName requestType, MsgLength msgLength,
			BlockingQueue<T> queue, int queueId) {
		long operationTime = doOperation(requestType, msgLength.getStr(), testDelegator, queueId);
		T resValue = createResultValue(requestType, msgLength, queueId, operationTime);
		boolean resAdded = queue.offer(resValue);
		if(!resAdded){
			System.out.println("Result not added to queue for printing::"+resValue.toString());
		}
		return operationTime > 0;
	}

	@SuppressWarnings("unchecked")
	T createResultValue(QueryName requestType, MsgLength msgLength, int queueId, long operationTime) {
		return (T) new ResultValues(requestType, operationTime , queueId, msgLength);
	}
	
	@SuppressWarnings("unchecked")
	R createPrinter(TestDelegator testDelegator, QueryName requestType, String expmntId,
			BlockingQueue<T> queue) throws Exception {
		return (R) new ResultPrinter<>(testDelegator.getClientId(), queue, expmntId, requestType, testDelegator.getPort());
	}
	
	protected long doOperation(QueryName taskType, String msgBody, TestDelegator testdelegator, int queueId){
		long operationStart = System.currentTimeMillis();
		try{
			Message msg;
			switch (taskType) {
			case NEW_QUEUE:
				testdelegator.doCreateQueue();
				break;
			case BROADCAST:
				testdelegator.doSend(true, 9, msgBody);
				break;
			case POST:
				testdelegator.doSend(false, 9, msgBody, queueId);
				break;
			case RETRIEVE_MESSAGE:
				msg = testdelegator.doRecv(false, queueId);
				assert (msg.getBody().equals(msgBody));
				break;
			case READ:
				testdelegator.doRecv(true, queueId);
				break;
			case LIST_QUEUE:
				testdelegator.doListQueue();
				break;
			case DELETE_QUEUE:
				testdelegator.doDeleteQueue(queueId);
				break;
			default:
				break;
			}
		}catch(Exception |AssertionError e){
			LOGGER.log(Level.SEVERE, "Request failed for client id: "+testdelegator.getClientId(), e);
			return -1;
		}
		return System.currentTimeMillis() - operationStart;
	}
}
