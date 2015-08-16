package com.asl.tester;

import java.util.concurrent.BlockingQueue;

import com.asl.utils.Constants;
import com.asl.utils.QueryName;

public class OWResultPrinter extends ResultPrinter<OWResultValues> {
	
	public OWResultPrinter(int clientId, BlockingQueue<OWResultValues> queue, String expmntId, QueryName reqquestType, int port)
			throws Exception {
		super(clientId, queue, expmntId, reqquestType, port);
	}
	
	@Override
	void printResultRow(OWResultValues resultValue) {
		ps.format("%s;%d;%d", resultValue.taskType.toString(),
				resultValue.countHop, resultValue.operationTime);
	}

	@Override
	void printHeader() {
		ps.format("%20s%20s%32s", 
				Constants.QUERY_TYPE_LABEL,Constants.HOP_COUNT_LABEL,Constants.TIME_TAKEN);
	}

}
