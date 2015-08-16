package com.asl.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;

public class CreateQueueMsgParams extends MsgParams {
	private static final Logger LOGGER = Logger.getLogger(CreateQueueMsgParams.class.getCanonicalName());
	
	public CreateQueueMsgParams() {
		headers.put(Constants.QUERY_TYPE_LABEL, QueryName.NEW_QUEUE.toString());
		LOGGER.log(Level.FINER, "message params  are {0}", new Object[] { headers });
	}
}
