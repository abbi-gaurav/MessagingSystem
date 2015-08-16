package com.asl.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.ClientRole;
import com.asl.utils.Constants;
import com.asl.utils.QueryName;

public class CreateClientParams extends MsgParams{
	private static final Logger LOGGER = Logger.getLogger(CreateClientParams.class.getCanonicalName());
	
	public CreateClientParams() {
		this.headers.put(Constants.QUERY_TYPE_LABEL, QueryName.NEW_CLIENT.toString());
		this.headers.put(Constants.CLIENT_ROLE_LABEL, ClientRole.rw.toString());
		LOGGER.log(Level.FINER, "message params  are {0}", new Object[] { headers });
	}
}
