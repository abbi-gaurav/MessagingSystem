package com.asl.tester;

import com.asl.utils.DataConstants;
import com.asl.utils.DataConstants.MsgLength;
import com.asl.utils.QueryName;

public class ResultValues {
	public final QueryName taskType;
	public final long operationTime;
	public final int queueId;
	public final MsgLength msgLength;

	public ResultValues(QueryName taskType, long operationTime,  int queueId, DataConstants.MsgLength msgLength) {
		this.taskType = taskType;
		this.operationTime = operationTime;
		this.queueId = queueId;
		this.msgLength = msgLength;
	}
}