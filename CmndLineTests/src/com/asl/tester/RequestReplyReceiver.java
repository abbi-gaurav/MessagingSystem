package com.asl.tester;

import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.client.exception.ClientException;
import com.asl.client.io.ClientIO;
import com.asl.client.io.IClientIO;
import com.asl.utils.Constants;
import com.asl.utils.DataConstants;
import com.asl.utils.Message;
import com.asl.utils.QueryName;
import com.asl.utils.Utils;
import com.asl.utils.ValidationException;
import com.asl.utils.DataConstants.MsgLength;

public class RequestReplyReceiver extends SingleClientContinuousRun<RRResultValues, RRReceiverResultsPrinter>{
	private static final Logger LOGGER = Logger.getLogger(RequestReplyReceiver.class.getCanonicalName());
	private final int replyQueueId;

	public RequestReplyReceiver(int replyQueueId) {
		this.replyQueueId = replyQueueId;
	}

	public static void main(String[] args) {
		try {
			int clientId = Integer.parseInt(args[0]);
			int receiverQueueId = Integer.parseInt(args[1]);
			int replyQueueId = Integer.parseInt(args[2]);
			
			String middlewareHost = args[3];
			int middlewarePort = Integer.parseInt(args[4]);
			
			DataConstants.MsgLength msgLength = DataConstants.MsgLength.valueOf(args[5]);
			
			long runTimeSeconds = Integer.parseInt(args[6]);
			
			String expmntId  = args[7];
			
			IClientIO clientIO = new ClientIO(InetAddress.getByName(middlewareHost), middlewarePort);
			TestDelegator testDelegator = new TestDelegator(clientIO, clientId);
			
			new RequestReplyReceiver(replyQueueId).runTest(testDelegator, QueryName.RETRIEVE_MESSAGE, msgLength,
					runTimeSeconds, expmntId, receiverQueueId);
		} catch (Exception e) {
			e.printStackTrace();
			printHelp();
		} 
	}
	
	@Override
	protected long doOperation(QueryName taskType, String msgBody, TestDelegator testdelegator, int receiverQueueId) {
		assert (taskType == QueryName.RETRIEVE_MESSAGE);
		long operationStart = System.currentTimeMillis();
		try {
			Message recvdMsg = testdelegator.doRecvWithReply(false, receiverQueueId, replyQueueId,msgBody);
			System.out.println("finished One cycle");
			assert(Utils.isNotBlank(recvdMsg.getValue(Constants.REPLIED_MSG_ID)));
			return System.currentTimeMillis() - operationStart;
		} catch (ClientException | ValidationException e) {
			LOGGER.log(Level.SEVERE, "Request failed for client id: "+testdelegator.getClientId(), e);
			return -1;
		}
	}
	
	RRResultValues createResultValue(QueryName requestType, MsgLength msgLength, int queueId, long operationTime) {
		return  new RRResultValues(requestType, operationTime , queueId, msgLength, replyQueueId);
	}
	
	RRReceiverResultsPrinter createPrinter(TestDelegator testDelegator, QueryName requestType, String expmntId,
			BlockingQueue<RRResultValues> queue) throws Exception {
		return new RRReceiverResultsPrinter(testDelegator.getClientId(), queue, expmntId, requestType,testDelegator.getPort());
	}
}
