package com.asl.client;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;

public class DeleteQueueMsgParams extends MsgParamsForExistingQueues{
	private static final Logger LOGGER = Logger.getLogger(CreateQueueMsgParams.class.getCanonicalName());
	
	public DeleteQueueMsgParams(int... queueIds) {
		super(Arrays.toString(queueIds));
		headers.put(Constants.QUERY_TYPE_LABEL, QueryName.DELETE_QUEUE.toString());
		
		LOGGER.log(Level.FINER, "message params  are {0}", new Object[] { headers });
	}
}
