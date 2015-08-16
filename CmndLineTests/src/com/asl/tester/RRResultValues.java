package com.asl.tester;

import com.asl.utils.DataConstants.MsgLength;
import com.asl.utils.QueryName;

public class RRResultValues extends ResultValues{

	public final int secondryQueue;

	/**
	 * @param taskType
	 * @param operationTime
	 * @param primaryQueue Queue on which main operation happens
	 * @param msgLength
	 * @param secodryQueue Queue on which second operation happens
	 */
	public RRResultValues(QueryName taskType, long operationTime, int primaryQueue, MsgLength msgLength, int secodryQueue) {
		super(taskType, operationTime, primaryQueue, msgLength);
		this.secondryQueue = secodryQueue;
	}

}
