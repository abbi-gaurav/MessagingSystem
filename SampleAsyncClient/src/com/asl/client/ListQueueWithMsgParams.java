package com.asl.client;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;

public class ListQueueWithMsgParams extends MsgParams{
	public ListQueueWithMsgParams() {
		this.headers.put(Constants.QUERY_TYPE_LABEL, QueryName.LIST_QUEUE_WITH_MESSAGE.toString());
	}
}
