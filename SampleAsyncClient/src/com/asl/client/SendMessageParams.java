package com.asl.client;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;

/**
 * @author gabbi
 *
 */
public class SendMessageParams extends MsgParamsForExistingQueues{
	private static final Logger LOGGER = Logger.getLogger(SendMessageParams.class.getCanonicalName());

	public SendMessageParams(int priority, int[] queueIds) {
		this( -1, null, priority, queueIds);
	}
	
	public SendMessageParams(int receiver,int priority, int[] queueIds) {
		this( receiver, null, priority, queueIds);
	}
	
	public SendMessageParams(int receiver, RRParamsParameter rrParams, int priority, int... queueIds){
		super(isQueueIdsProvided(queueIds) ? Arrays.toString(queueIds) : "");
		
		headers.put(Constants.QUERY_TYPE_LABEL, isQueueIdsProvided(queueIds) ? QueryName.POST.toString():QueryName.BROADCAST.toString());
		
		headers.put(Constants.PRIORITY_LABEL, Integer.toString(priority));
		
		if(receiver != -1){
			headers.put(Constants.DEST_ID_LABEL, ""+receiver);
		}
		
		if(rrParams != null){
			headers.put(Constants.CONTEXT_LABEL, ""+rrParams.getContext());
			
			int replyQueue = rrParams.getReplyQueue();
			if(replyQueue != -1){
				headers.put(Constants.REPLY_QUEUE_LABEL, Integer.toString(replyQueue));
			}
		}
		
		LOGGER.log(Level.FINER, "message params  are {0}", new Object[]{headers});
	}

	private static boolean isQueueIdsProvided(int... queueIds) {
		return queueIds != null && queueIds.length > 0;
	}

	public String getContext() {
		return headers.get(Constants.CONTEXT_LABEL);
	}
	
	public String getReplyQueue(){
		return headers.get(Constants.REPLY_QUEUE_LABEL);
	}
}
