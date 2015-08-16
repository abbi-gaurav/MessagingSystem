package com.asl.test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

import com.asl.client.exception.ClientException;
import com.asl.client.io.ClientIO;
import com.asl.tester.TestDelegator;
import com.asl.utils.Constants;
import com.asl.utils.DataConstants;
import com.asl.utils.Message;
import com.asl.utils.Status;
import com.asl.utils.Utils;
import com.asl.utils.ValidationException;

public class ClientTestFullFlow{

	private static final String SAMPLE_MESSAGE_BODY = "Sample Message body";
	private final TestDelegator delegatorSender;
	private final TestDelegator delegatorRecv;
	private final int queueId;

	public ClientTestFullFlow()
			throws ClientException, UnknownHostException, ValidationException {
		this.delegatorSender = new TestDelegator(new ClientIO(InetAddress.getLocalHost(),2500));
		this.delegatorRecv = new TestDelegator(new ClientIO(InetAddress.getLocalHost(), 2500));
		queueId = delegatorSender.doCreateQueue();
	}
	
	@Test()
	public void testSendRecInSeq() throws ClientException, ValidationException{
		String msgString = SAMPLE_MESSAGE_BODY;
		for(int i=0;i<10;i++){
			Message recvdMsg = sendRecv(msgString);
			System.out.println(recvdMsg.getBody());
		}
	}

	
	@Test
	public void testBroadcast() throws ClientException, ValidationException{
		int queueId2 = delegatorSender.doCreateQueue();
		try{
			String msgId = delegatorSender.doSend(true, 9, SAMPLE_MESSAGE_BODY);

			Message recvdMsg1 = delegatorRecv.doRecv(false, queueId);
			verifyReceivedMsg(msgId, recvdMsg1);

			Message recvdMsg2 = delegatorRecv.doRecv(false, queueId2);
			verifyReceivedMsg(msgId, recvdMsg2);
		}finally{
			Assert.assertEquals(Status.SUCCESS.toString(),delegatorSender.doDeleteQueue(queueId2));
		}

	}
	
	@Test
	public void testContextFunctionality() throws ClientException, ValidationException{
		int replyQueue = delegatorSender.doCreateQueue();
		try{
			delegatorSender.doSendWithContext(delegatorRecv.getClientId(), 9, SAMPLE_MESSAGE_BODY, queueId, 123, replyQueue);
			
			Message recvdMsg1 = delegatorRecv.doRecvWithReply(false, queueId, replyQueue);
			Assert.assertNotNull(recvdMsg1.getValue(Constants.REPLIED_MSG_ID));
			Message replyMsg = delegatorSender.doRecv(false, replyQueue);
			Assert.assertTrue(recvdMsg1.getValue(Constants.REPLIED_MSG_ID).equals(replyMsg.getValue(Constants.MESSAGE_ID_LABEL)));
		}finally{
			Assert.assertEquals(Status.SUCCESS.toString(),delegatorSender.doDeleteQueue(replyQueue));
		}
	}
	
	@Test
	public void testRead() throws ClientException, ValidationException{
		String msgId = delegatorSender.doSend(false, 9, SAMPLE_MESSAGE_BODY, queueId);
		Message readMsg = delegatorRecv.doRecv(true, queueId);
		verifyReceivedMsg(msgId, readMsg);
		
		Message recvdMsg = delegatorRecv.doRecv(false, queueId);
		verifyReceivedMsg(msgId, recvdMsg);
	}
	
	@Test
	public void testListQueues() throws ClientException, ValidationException{
		Integer[] queues = delegatorRecv.doListQueue();
		verifyQueuesList(queues);
		
		Integer[] queuesWithMsgs = delegatorRecv.doListWithMsgQueue();
		verifyQueuesList(queuesWithMsgs);
	}
	
	@Test
	public void testCheckMsgFrom() throws ClientException, ValidationException{
		Integer[] msgIds = delegatorRecv.doCheckMsgFrom(delegatorSender.getClientId());
		Assert.assertTrue(msgIds.length == 0);
		
		int msgId = Integer.parseInt(delegatorSender.doSend(false, 9, SAMPLE_MESSAGE_BODY, queueId));
		Integer[] msgIds2 = delegatorRecv.doCheckMsgFrom(delegatorSender.getClientId());
		Assert.assertTrue(msgIds.length == 0);
		Assert.assertEquals(msgId, msgIds2[0].intValue());
	}
	
	@Test
	public void clientIdTest()
			throws ClientException, ValidationException {
		clientIdVerify(delegatorRecv);
		clientIdVerify(delegatorSender);
	}
	
	@Test
	public void test2KMessage() throws ClientException, ValidationException{
		for(int i=0;i<10;i++){
			sendRecv(DataConstants.TWO_K_LONG);
		}
	}
	
	private Message sendRecv(String msgString) throws ClientException, ValidationException {
		String msgId = delegatorSender.doSend(false, 9, msgString, queueId);

		Message recvdMsg = delegatorRecv.doRecv(false, queueId);
		
		verifyReceivedMsg(msgId, recvdMsg,msgString);
		return recvdMsg;
	}
	
	private void clientIdVerify(TestDelegator delegator) throws ClientException, ValidationException {
		int clientId = delegator.getClientId();
		int clientIdAfter = 	delegator.doClientCreation();
		Assert.assertEquals(clientId,clientIdAfter);
		
		System.out.println("client"+clientId);
	}

	private void verifyQueuesList(Integer[] queues) {
		Assert.assertNotNull(queues);
		Assert.assertTrue(queues.length > 0);
		System.out.println(Arrays.toString(queues));
	}
	private void verifyReceivedMsg(String msgId, Message recvdMsg) {
		verifyReceivedMsg(msgId, recvdMsg,SAMPLE_MESSAGE_BODY);
	}
	
	private void verifyReceivedMsg(String msgId, Message recvdMsg, String msgStr) {
		Assert.assertEquals(msgId, recvdMsg.getValue(Constants.MESSAGE_ID_LABEL));
		Assert.assertEquals(msgStr, Utils.removeStartAndEndQuotes(recvdMsg.getBody(),"\""));
	}
	
	@After
	public  void close(){
		delegatorRecv.close();
		delegatorSender.close();
	}
	
}
