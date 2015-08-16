package com.asl.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;
import com.asl.utils.ReceiveBy;
import com.asl.utils.Utils;

public class ReceiveMsgParams extends MsgParamsForExistingQueues{
	private static final Logger LOGGER = Logger.getLogger(ReceiveMsgParams.class.getCanonicalName());
	private final String rrBody;
	

	public ReceiveMsgParams(int queueId, boolean isPeek) {
		this(queueId, isPeek,ReceiveBy.Priority,null,-1);
	}
	
	public ReceiveMsgParams(int queueId,  boolean isPeek, ReceiveBy receiverBy, String requestReplyResponse, int replyQueue) {
		super(queueId);
		headers.put(Constants.QUERY_TYPE_LABEL, isPeek?QueryName.READ.toString():QueryName.RETRIEVE_MESSAGE.toString());
		headers.put(Constants.MSG_ORDER_BY, receiverBy.toString());
		if(replyQueue != -1){
			headers.put(Constants.REPLY_QUEUE_LABEL, replyQueue+"");
		}
		this.rrBody = requestReplyResponse == null ? Constants.SAMPLE_REPLY:requestReplyResponse;
		
		LOGGER.log(Level.FINER, "message params  are {0}", new Object[] { headers });
	}

	public String getRrBody() {
		return rrBody;
	}

	public int getReplyQueue() {
		String replyQueueStr = headers.get(Constants.REPLY_QUEUE_LABEL);
		return Utils.isNotBlank(replyQueueStr) ? Integer.parseInt(replyQueueStr):-1;
	}
}
