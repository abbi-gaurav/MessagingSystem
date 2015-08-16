package com.asl.client;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;

public class ListQueueParams extends MsgParams{
	public ListQueueParams() {
		this.headers.put(Constants.QUERY_TYPE_LABEL, QueryName.LIST_QUEUE.toString());
	}
}
