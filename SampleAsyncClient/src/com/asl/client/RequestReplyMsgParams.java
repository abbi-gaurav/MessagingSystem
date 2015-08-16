package com.asl.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Constants;
import com.asl.utils.OperationType;

public class RequestReplyMsgParams extends MsgParamsForExistingQueues{
	private static final Logger LOGGER = Logger.getLogger(RequestReplyMsgParams.class.getCanonicalName());
	
	public RequestReplyMsgParams(String reqQueueName, String replyQueueName) {
		super(reqQueueName);
		
		headers.put(Constants.REQUEST_LABEL, OperationType.RequestReply.toString());
		headers.put(Constants.REPLY_QUEUE_LABEL, replyQueueName);
		LOGGER.log(Level.FINER, "message params  are {0}", new Object[]{headers});
	}

	
}
