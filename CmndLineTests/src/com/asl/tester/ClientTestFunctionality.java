package com.asl.tester;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.asl.client.exception.ClientException;
import com.asl.client.io.ClientIO;
import com.asl.tester.TestDelegator;
import com.asl.utils.Constants;
import com.asl.utils.Message;
import com.asl.utils.Status;
import com.asl.utils.Utils;
import com.asl.utils.ValidationException;

public class ClientTestFunctionality{

	private final TestDelegator delegatorSender;
	private final TestDelegator delegatorRecv;
	private final int queueId;

	public ClientTestFunctionality()
			throws ClientException, UnknownHostException, ValidationException {
		this.delegatorSender = new TestDelegator(new ClientIO(InetAddress.getLocalHost(),2500));
		this.delegatorRecv = new TestDelegator(new ClientIO(InetAddress.getLocalHost(), 2500));
		clientCreation();
		queueId = delegatorSender.doCreateQueue();
	}
	
	public void testSendRecInSeq() throws ClientException, ValidationException{
		for(int i=0;i<10;i++){
			String msgId = delegatorSender.doSend(false, 9, Constants.SAMPLE_MESSAGE_BODY, queueId);

			Message recvdMsg = delegatorRecv.doRecv(false, queueId);
			verifyReceivedMsg(msgId, recvdMsg);
			System.out.println(recvdMsg.getBody());
		}
	}

	
	public void testBroadcast() throws ClientException, ValidationException{
		int queueId2 = delegatorSender.doCreateQueue();
		try{
			String msgId = delegatorSender.doSend(true, 9, Constants.SAMPLE_MESSAGE_BODY);

			Message recvdMsg1 = delegatorRecv.doRecv(false, queueId);
			verifyReceivedMsg(msgId, recvdMsg1);

			Message recvdMsg2 = delegatorRecv.doRecv(false, queueId2);
			verifyReceivedMsg(msgId, recvdMsg2);
		}finally{
			assert (Status.SUCCESS.toString().equals(delegatorSender.doDeleteQueue(queueId2)));
		}

	}
	
	public void testRead() throws ClientException, ValidationException{
		String msgId = delegatorSender.doSend(false, 9, Constants.SAMPLE_MESSAGE_BODY, queueId);
		Message readMsg = delegatorRecv.doRecv(true, queueId);
		verifyReceivedMsg(msgId, readMsg);
		
		Message recvdMsg = delegatorRecv.doRecv(false, queueId);
		verifyReceivedMsg(msgId, recvdMsg);
	}
	
	public void testListQueues() throws ClientException, ValidationException{
		Integer[] queues = delegatorRecv.doListQueue();
		verifyQueuesList(queues);
		
		Integer[] queuesWithMsgs = delegatorRecv.doListWithMsgQueue();
		verifyQueuesList(queuesWithMsgs);
	}
	
	public void testCheckMsgFrom() throws ClientException, ValidationException{
		Integer[] msgIds = delegatorRecv.doCheckMsgFrom(delegatorSender.getClientId());
		assert(msgIds.length == 0);
		
		int msgId = Integer.parseInt(delegatorSender.doSend(false, 9, Constants.SAMPLE_MESSAGE_BODY, queueId));
		Integer[] msgIds2 = delegatorRecv.doCheckMsgFrom(delegatorSender.getClientId());
		assert(msgIds.length == 0);
		assert (msgId == msgIds2[0].intValue());
	}
	public void verifyQueuesList(Integer[] queues) {
		assert(queues != null);
		assert(queues.length > 0);
		System.out.println(Arrays.toString(queues));
	}
	private void verifyReceivedMsg(String msgId, Message recvdMsg) {
		assert(msgId.equals(recvdMsg.getValue(Constants.MESSAGE_ID_LABEL)));
		assert(Constants.SAMPLE_MESSAGE_BODY.equals(Utils.removeStartAndEndQuotes(recvdMsg.getBody(),"\"")));
	}
	
	private void clientCreation() throws ClientException, ValidationException {
		createClient(delegatorSender);
		createClient(delegatorRecv);
	}

	private int createClient(TestDelegator delegator)
			throws ClientException, ValidationException {
		assert(delegator.getClientId() == -1);
		int clientId = 	delegator.doClientCreation();
		assert(delegator.getClientId() == clientId);
		System.out.println("client"+clientId);
		return clientId;
	}
	
	public  void close(){
		delegatorRecv.close();
		delegatorSender.close();
	}
	
}
