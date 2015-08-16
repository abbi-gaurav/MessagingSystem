package com.asl.client;

import com.asl.utils.Constants;
import com.asl.utils.Utils;

public abstract class MsgParamsForExistingQueues extends MsgParams {
	
	public MsgParamsForExistingQueues(int queueId) {
		
		this(Integer.toString(queueId));
	}
	
	public MsgParamsForExistingQueues(String queueIdAsString){
		if(Utils.isNotBlank(queueIdAsString)){
			headers.put(Constants.QUEUE_ID_LABEL,queueIdAsString);
		}
	}
}
