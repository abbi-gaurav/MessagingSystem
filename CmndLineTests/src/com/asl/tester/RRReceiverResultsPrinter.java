package com.asl.tester;

import java.util.concurrent.BlockingQueue;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;

public class RRReceiverResultsPrinter extends ResultPrinter<RRResultValues>{

	public RRReceiverResultsPrinter(int clientId, BlockingQueue<RRResultValues> queue, String expmntId, QueryName reqquestType, int port)
			throws Exception {
		super(clientId, queue, expmntId, reqquestType, port);
	}
	
	@Override
	void printResultRow(RRResultValues resultValue) {
		ps.format("%s;%d;%d;%d", resultValue.taskType.toString(),
				resultValue.queueId,resultValue.secondryQueue, resultValue.operationTime);
	}

	@Override
	void printHeader() {
		ps.format("%20s%20s%20s%32s", 
				Constants.QUERY_TYPE_LABEL,Constants.RCEIVER_QUEUE_ID_LABEL,Constants.REPLY_QUEUE_LABEL,Constants.TIME_TAKEN);
	}
}
