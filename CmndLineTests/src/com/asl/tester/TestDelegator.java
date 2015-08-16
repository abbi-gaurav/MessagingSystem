package com.asl.tester;

import com.asl.client.CheckMsgFromParams;
import com.asl.client.Client;
import com.asl.client.CreateClientParams;
import com.asl.client.CreateQueueMsgParams;
import com.asl.client.DeleteQueueMsgParams;
import com.asl.client.ListQueueParams;
import com.asl.client.ListQueueWithMsgParams;
import com.asl.client.RRParamsParameter;
import com.asl.client.ReceiveMsgParams;
import com.asl.client.SendMessageParams;
import com.asl.client.exception.ClientException;
import com.asl.client.io.IClientIO;
import com.asl.utils.Constants;
import com.asl.utils.Message;
import com.asl.utils.ReceiveBy;
import com.asl.utils.ValidationException;

public class TestDelegator implements AutoCloseable{
	private Client client;

	public TestDelegator(IClientIO clientIO) throws ClientException, ValidationException {
		this(clientIO,-1);
	}
	
	public TestDelegator(IClientIO clientIO,int clientId) throws ClientException, ValidationException {
		this.client = new Client(clientIO, clientId);
		if(clientId == -1){
			this.doClientCreation();
		}
		
		assert getClientId() != -1;
	}
	
	public Integer[] doListQueue() throws ClientException, ValidationException {
		return client.listQueue(new ListQueueParams());
	}
	
	public Integer[] doListWithMsgQueue() throws ClientException, ValidationException {
		return client.listQueueWithMsg(new ListQueueWithMsgParams());
	}
	
	public Integer[] doCheckMsgFrom(int senderId) throws ClientException, ValidationException {
		return client.checkMsgFrom(new CheckMsgFromParams(senderId));
	}

	public String doDeleteQueue(int queueId) throws ClientException, ValidationException {
		return client.deleteQueue(new DeleteQueueMsgParams(queueId));
	}

	public int doCreateQueue() throws ClientException, ValidationException {
		return client.createQueue(new CreateQueueMsgParams());
	}

	public Message doRecv(boolean isPeek, int queueId) throws ClientException, ValidationException {
		return client.receiveMessage(new ReceiveMsgParams(queueId, isPeek));
	}
	
	public Message doRecvWithReply(boolean isPeek, int queueId, int replyQueue) throws ClientException, ValidationException {
		return doRecvWithReply(false, queueId, replyQueue, Constants.SAMPLE_REPLY);
	}
	
	public Message doRecvWithReply(boolean isPeek, int queueId, int replyQueue, String replBody) throws ClientException, ValidationException {
		return client.receiveMessage(new ReceiveMsgParams(queueId, isPeek,ReceiveBy.Priority,replBody,replyQueue));
	}
	
	public String doSend(boolean isBroadCast, int priority, String msgBody,int... queueIds ) throws ClientException, ValidationException {
		String response = client.sendMessage(isBroadCast ? new SendMessageParams(priority, new int[]{}) : new SendMessageParams(priority, queueIds), 
										msgBody);
		return response;
	}
	
	public String doSend(boolean isBroadCast, int receiver, int priority, String msgBody,int... queueIds ) throws ClientException, ValidationException {
		String response = client.sendMessage(isBroadCast ? new SendMessageParams(receiver, priority, new int[]{}) : new SendMessageParams(receiver, priority, queueIds), 
										msgBody);
		return response;
	}
	
	public String doSendWithContext(int receiver, int priority, String msgBody,int queueIds, int context, int replyQueue ) throws ClientException, ValidationException {
		String response = client.sendMessage(new SendMessageParams(receiver, new RRParamsParameter(context, replyQueue), priority, queueIds), msgBody);
		return response;
	}
	
	public int doClientCreation() throws ClientException, ValidationException {
		return client.createClient(new CreateClientParams());
	}
	
	public int getClientId(){
		return client.getClientId();
	}

	@Override
	public void close() {
		client.close();
	}
	
	public int getPort(){
		return this.client.getPort();
	}
}
