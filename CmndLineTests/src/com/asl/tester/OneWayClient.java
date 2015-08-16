package com.asl.tester;

import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.client.io.ClientIO;
import com.asl.client.io.IClientIO;
import com.asl.utils.Constants;
import com.asl.utils.DataConstants;
import com.asl.utils.Message;
import com.asl.utils.QueryName;
import com.asl.utils.DataConstants.MsgLength;

public class OneWayClient extends SingleClientContinuousRun<OWResultValues, OWResultPrinter> {
	private static final Logger LOGGER = Logger.getLogger(OneWayClient.class.getCanonicalName());
	private static final Random RANDOM = new Random();
	private static final int WAIT_TIME = 100;
	private final int queueId;
	private final int receiverRangeStart;
	private final int receiverRangeEnd;
	private int countHop;

	public OneWayClient(int queueId, int receiverRangeStart, int receiverRangeEnd) {
		this.queueId = queueId;
		this.receiverRangeStart = receiverRangeStart;
		this.receiverRangeEnd = receiverRangeEnd;
		countHop = 0;
	}

	public static void main(String[] args) {
		try {
			int clientId = Integer.parseInt(args[0]);
			int queue = Integer.parseInt(args[1]);
			int clientRangeStart = Integer.parseInt(args[2]);
			int clientRangeEnd = Integer.parseInt(args[3]);
			
			String middlewareHost = args[4];
			int middlewarePort = Integer.parseInt(args[5]);
			
			DataConstants.MsgLength msgLength = DataConstants.MsgLength.valueOf(args[6]);
			
			long runTimeSeconds = Integer.parseInt(args[7]);
			
			String expmntId  = args[8];
			
			IClientIO clientIO = new ClientIO(InetAddress.getByName(middlewareHost), middlewarePort);
			TestDelegator testDelegator = new TestDelegator(clientIO, clientId);
			
			new OneWayClient(queue, clientRangeStart, clientRangeEnd).runTest(testDelegator, QueryName.ONE_WAY, msgLength, runTimeSeconds, expmntId, queue);
		} catch (Exception e) {
			e.printStackTrace();
			printHelp();
		} 
	}

	@Override
	protected long doOperation(QueryName taskType, String msgBody, TestDelegator testdelegator, int reqQueueId) {
		assert (taskType == QueryName.ONE_WAY);
		long operationStart = System.currentTimeMillis();
		try {
			msgBody = insertHopInfo(msgBody);
			int  receiverId = RANDOM.nextInt((receiverRangeEnd - receiverRangeStart)+1)+receiverRangeStart;
			testdelegator.doSend(false, receiverId, 9, msgBody, queueId);
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE,"OWClient was interrupted before he could try to retrieve a message: "+ e.getMessage(), e);
			}
			Message msg = testdelegator.doRecv(false, queueId);
			collectHopInfo(msg.getBody());
			return System.currentTimeMillis() - operationStart;
		} catch (AssertionError|Exception e) {
			LOGGER.log(Level.SEVERE, "Request failed for client id: "+testdelegator.getClientId(), e);
			return -1;
		}
	}
	
	private void collectHopInfo(String body){
		String key = Constants.HOP_COUNT_LABEL+'=';
		int index = body.indexOf(key);
		countHop = index == -1 ? 0 : Integer.parseInt(body.substring(index+key.length(),body.length()));
	}
	
	//baseString is generated body of specified length
	private String insertHopInfo(String baseString){
		countHop++;
		String count = Constants.HOP_COUNT_LABEL+'='+countHop;
		assert (count.length() <= baseString.length());
		return baseString.substring(0, baseString.length()-count.length())+count;
	}
	
	@Override
	OWResultValues createResultValue(QueryName requestType, MsgLength msgLength, int queueId, long operationTime) {
		return  new OWResultValues(requestType, operationTime , queueId, msgLength, countHop);
	}
	
	@Override
	OWResultPrinter createPrinter(TestDelegator testDelegator, QueryName requestType, String expmntId,
			BlockingQueue<OWResultValues> queue) throws Exception {
		return new OWResultPrinter(testDelegator.getClientId(), queue, expmntId, requestType, testDelegator.getPort());
	}
}
