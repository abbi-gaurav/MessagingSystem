package com.asl.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;

public class CheckMsgFromParams extends MsgParams {
	private static final Logger LOGGER = Logger.getLogger(CheckMsgFromParams.class.getCanonicalName());
	public CheckMsgFromParams(int senderId) {
		this.headers.put(Constants.QUERY_TYPE_LABEL, QueryName.CHECK_MESSAGE_FROM.toString());
		this.headers.put(Constants.SENDER_ID_LABEL, Integer.toString(senderId));
		
		LOGGER.log(Level.FINER, "message params  are {0}", new Object[] { headers });
	}
}
