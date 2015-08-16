package com.asl.client;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.client.exception.ClientException;
import com.asl.client.io.ClientIO;
import com.asl.client.io.IClientIO;
import com.asl.utils.Constants;
import com.asl.utils.Message;
import com.asl.utils.MessageParser;
import com.asl.utils.MessageParser.MessageType;
import com.asl.utils.Status;
import com.asl.utils.Utils;
import com.asl.utils.ValidationException;

/**
 * Represents the set of APIs that provide messaging functionality
 * @author gabbi
 *
 */
public class Client {
	private static final Logger LOGGER = Logger.getLogger(Client.class.getCanonicalName());

	private final IClientIO clientIO;
	private int clientId; 

	/**
	 * @param middleware
	 * @param clientId
	 * @param port
	 * @throws ClientException
	 */
	public Client(InetAddress middleware, int clientId, int port) throws ClientException  {
		this(new ClientIO(middleware,port), clientId);
	}
	
	/**
	 * @param middleware
	 * @param port
	 * @throws ClientException
	 */
	public Client(InetAddress middleware,int port) throws ClientException{
		this(middleware,-1,port);
	}
	
	/**
	 * @param clientIO
	 * @param clientId
	 */
	public Client(IClientIO clientIO, int clientId) {
		this.clientId = clientId;
		this.clientIO = clientIO;
	}
	
	/**
	 * @param clientIO
	 */
	public Client(IClientIO clientIO){
		this(clientIO, -1);
	}
	
	/**
	 * Sends a message to the middleware
	 * @param msgParams properties of the message
	 * @param messageBody
	 * @return Status String
	 * @throws ClientException on failure
	 * @throws ValidationException if properties are not correct
	 */
	public String sendMessage(SendMessageParams msgParams, String messageBody) throws ClientException, ValidationException {
		valiateClientId();
		
		ByteBuffer buffer = createBufferAndAddHeaders(msgParams);
		
		Utils.addBody(buffer, messageBody);

		Utils.markEnd(buffer);

		String sendResponse = writeRead(buffer);
		Message message = MessageParser.parse(sendResponse, MessageType.Response);
		assertResponseSuccess(message);
		return message.getValue(Constants.MESSAGE_ID_LABEL);
	}

	/**
	 * Receieves the message
	 * @param msgParams properties of the message to be received
	 * @return {@link Message} which represents the Message body and the properties
	 * @throws ClientException on failure
	 * @throws ValidationException if properties are not correct
	 */
	public Message receiveMessage(ReceiveMsgParams msgParams) 
			throws ClientException, ValidationException{
		valiateClientId();

		ByteBuffer buffer = createBufferAndAddHeaders(msgParams);

		Utils.markEnd(buffer);

		String response = writeRead(buffer);

		//parse response, check if this needs a reply to be send
		Message receivedMessage = MessageParser.parse(response, MessageType.Response);
		assertResponseSuccess(receivedMessage);

		if(canReply(msgParams, receivedMessage)){
			//this is request reply interaction, send the response back
			//TODO: add correct context
			String repliedMsgId = sendReply(receivedMessage, msgParams.getRrBody(),msgParams.getReplyQueue());
			receivedMessage.addHeader(Constants.REPLIED_MSG_ID, repliedMsgId);
		}
		return receivedMessage;
	}

	private boolean canReply(ReceiveMsgParams msgParams, Message receivedMessage) {
		 if (Constants.MINUS_ONE.equals(msgParams.getReplyQueue()+"")) return false;
		 if (Utils.isBlank(receivedMessage.getValue(Constants.CONTEXT_LABEL)) || 
				 Constants.NONE.equals(receivedMessage.getValue(Constants.CONTEXT_LABEL))) return false;
		 if(Utils.isBlank(receivedMessage.getValue(Constants.FROM_LABEL))) return false;
		 
		 return true;
		 
	}
	
	/**
	 * Creates a new Client ID and sets that as the client Id of this {@link Client} object
	 * @param params properties of the client to be created
	 * @return client id
	 * @throws ClientException
	 * @throws ValidationException
	 */
	public int createClient(CreateClientParams params) throws ClientException, ValidationException{
		if(clientId > 0){
			return clientId;
		}
		String clientIdString = performMsgSendAndRecv(params).getValue(Constants.CLIENT_ID_LABEL);
		clientId = Integer.parseInt(clientIdString);
		return clientId;
	}

	
	/**
	 * creates a queue
	 * @param params queue properties
	 * @return queue id
	 * @throws ClientException
	 * @throws ValidationException
	 */
	public int createQueue(CreateQueueMsgParams params) throws ClientException, ValidationException{
		valiateClientId();
		String queueIdString = performMsgSendAndRecv(params).getValue(Constants.QUEUE_ID_LABEL);
		return Integer.parseInt(queueIdString);
	}
	
	/**
	 * deletes a queue
	 * @param params
	 * @return status of the operation
	 * @throws ClientException
	 * @throws ValidationException
	 */
	public String deleteQueue(DeleteQueueMsgParams params) throws ClientException, ValidationException{
		valiateClientId();
		return performMsgSendAndRecv(params).getValue(MessageType.Response.toString());
	}
	
	/**
	 * List all queues in the system
	 * @param params 
	 * @return {@link Integer} Array containing all queue ids in the system
	 * @throws ClientException
	 * @throws ValidationException
	 */
	public Integer[] listQueue(ListQueueParams params) throws ClientException, ValidationException{
		return doGetListOperation(params);
	}

	/**
	 * List all queues with messages in the system
	 * @param params
	 * @return {@link Integer} Array containing all queue ids in the system
	 * @throws ClientException
	 * @throws ValidationException
	 */
	public Integer[] listQueueWithMsg(ListQueueWithMsgParams params) throws ClientException, ValidationException{
		return doGetListOperation(params);
	}
	
	/**
	 * queries the system to find out the messages from a particular client
	 * @param params contains client Id
	 * @return {@link Integer} Array containing all the message ids
	 * @throws ClientException
	 * @throws ValidationException
	 */
	public Integer[] checkMsgFrom(CheckMsgFromParams params) throws ClientException, ValidationException{
		valiateClientId();
		Message msg = performMsgSendAndRecv(params);
		String msgIdStr = msg.getValue(Constants.MESSAGE_ID_LABEL);
		if(Constants.NONE.equals(msgIdStr)){
			return new Integer[]{};
		}
		return new Integer[]{Integer.parseInt(msgIdStr)};
	}
	
	/**
	 * this effectively means closing the TCP connection
	 */
	public void close(){
		this.clientIO.close();
	}
	
	/**
	 * @return the client Id represented by this Client object
	 */
	public int getClientId() {
		return clientId;
	}
	
	
	private void valiateClientId() {
		assert(clientId > 0 );
	}
	
	
	private Message performMsgSendAndRecv(MsgParams params)
			throws ClientException, ValidationException {
		ByteBuffer buffer = createBufferAndAddHeaders(params);
		Utils.markEnd(buffer);
		
		String response = writeRead(buffer);
		
		Message message = MessageParser.parse(response, MessageType.Response);
		assertResponseSuccess(message);
		return message;
	}

	
	private String sendReply(Message receivedMessage, String  reply, int replyQueue) throws ClientException, ValidationException {
		assert replyQueue > 0;
		String from = receivedMessage.getValue(Constants.FROM_LABEL);
		assert from != null; 
		
		String priorityString = receivedMessage.getValue(Constants.PRIORITY_LABEL);
		
		LOGGER.log(Level.FINE, "Reply to {0} on replyQueue {1}", new Object[]{from, replyQueue});
		
		SendMessageParams params = new SendMessageParams(Integer.parseInt(from),
											new RRParamsParameter(Integer.parseInt(receivedMessage.getValue(Constants.CONTEXT_LABEL)), -1),
											Utils.isNotBlank(priorityString)?Integer.parseInt(priorityString):9, 
											new int[]{replyQueue});
		
		String repliedMsgId = this.sendMessage(params, reply);
		
		return repliedMsgId;
	}

	private void assertResponseSuccess(Message response) {
		assert (response.getValue(MessageType.Response.toString()).equals(Status.SUCCESS.toString()));
	}

	private ByteBuffer createBufferAndAddHeaders(MsgParams msgParams) {
		ByteBuffer buffer = Utils.getByteBuffer();
		
		if(clientId != -1){
			Utils.addKeyValue(buffer, Constants.CLIENT_ID_LABEL, Integer.toString(clientId));
			buffer.put(Utils.stringToBytes(Constants.HEADERS_DELIM));
		}
		
		msgParams.addHeaders(buffer);
		return buffer;
	}
	

	private String writeRead(ByteBuffer buffer) throws ClientException {
		try{
			long start = System.currentTimeMillis();
			clientIO.write(buffer);
			LOGGER.log(Level.FINER, "request sent, now gettig the response");
			String readString = clientIO.read(buffer);
			System.out.println(clientIO.getSocketChannel()+";WriteRead;"+(System.currentTimeMillis()-start));
			return readString;
		} catch (InterruptedException | ExecutionException | IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new ClientException(e);
		}
	}
	
	private Integer[] doGetListOperation(MsgParams params)
			throws ClientException, ValidationException {
		valiateClientId();
		return Utils.parseIdArray(performMsgSendAndRecv(params).getValue(Constants.QUEUE_ID_LABEL));
	}
	
	/**
	 * @return the port number of the middleware to which this client is connected
	 */
	public int getPort(){
		return this.clientIO.getPort();
	}
}
