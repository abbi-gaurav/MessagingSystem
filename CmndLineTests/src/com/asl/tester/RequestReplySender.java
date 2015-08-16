package com.asl.tester;

import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.client.io.ClientIO;
import com.asl.client.io.IClientIO;
import com.asl.utils.DataConstants;
import com.asl.utils.Message;
import com.asl.utils.QueryName;
import com.asl.utils.DataConstants.MsgLength;

public class RequestReplySender extends SingleClientContinuousRun<RRResultValues, RRSenderResultsPrinter>{
	private static final Logger LOGGER = Logger.getLogger(RequestReplySender.class.getCanonicalName());
	private final int replyQueueId;
	private final int contextId;
	private final int receiverId;

	public RequestReplySender(int replyQueueId, int contextId, int receiverId) {
		this.replyQueueId = replyQueueId;
		this.contextId = contextId;
		this.receiverId = receiverId;
	}

	public static void main(String[] args) {
		try {
			int clientId = Integer.parseInt(args[0]);
			int reqQueueId = Integer.parseInt(args[1]);
			int replyQueueId = Integer.parseInt(args[2]);
			
			String middlewareHost = args[3];
			int middlewarePort = Integer.parseInt(args[4]);
			
			DataConstants.MsgLength msgLength = DataConstants.MsgLength.valueOf(args[5]);
			
			long runTimeSeconds = Integer.parseInt(args[6]);
			int contextId = Integer.parseInt(args[7]);
			
			int  receiverId = Integer.parseInt(args[8]);
			String expmntId  = args[9];
			
			IClientIO clientIO = new ClientIO(InetAddress.getByName(middlewareHost), middlewarePort);
			TestDelegator testDelegator = new TestDelegator(clientIO, clientId);
			
			new RequestReplySender(replyQueueId, contextId, receiverId).runTest(testDelegator, QueryName.POST, msgLength, runTimeSeconds, expmntId, reqQueueId);
		} catch (Exception e) {
			e.printStackTrace();
			printHelp();
		} 
	}

	@Override
	protected long doOperation(QueryName taskType, String msgBody, TestDelegator testdelegator, int reqQueueId) {
		assert (taskType == QueryName.POST);
		long operationStart = System.currentTimeMillis();
		try {
			testdelegator.doSendWithContext(receiverId, 9, msgBody, reqQueueId, contextId, replyQueueId);
			Message replyMsg = testdelegator.doRecv(false, replyQueueId);
			System.out.println("finished One cycle");
			assert(msgBody.equals(replyMsg.getBody()));

			return System.currentTimeMillis() - operationStart;
		} catch (AssertionError|Exception e) {
			LOGGER.log(Level.SEVERE, "Request failed for client id: "+testdelegator.getClientId(), e);
			return -1;
		}
		
	}
	
	RRResultValues createResultValue(QueryName requestType, MsgLength msgLength, int queueId, long operationTime) {
		return  new RRResultValues(requestType, operationTime , queueId, msgLength, replyQueueId);
	}
	
	RRSenderResultsPrinter createPrinter(TestDelegator testDelegator, QueryName requestType, String expmntId,
			BlockingQueue<RRResultValues> queue) throws Exception {
		return new RRSenderResultsPrinter(testDelegator.getClientId(), queue, expmntId, requestType, testDelegator.getPort());
	}
}
