package com.asl.tester;

import com.asl.utils.QueryName;

public class Result {

	private final long[] operationExecutionTimes;
	private final int succCount;
	private final QueryName taskType;
	private final long startTimeMS;
	private final long endTimeMS;
	private final  int clientId;
	private final int queueId;

	public Result(long[] operationExecutionTimes, int succCount, QueryName taskType, int clientId, long startMS, long endMS, int queueId) {
		this.operationExecutionTimes = operationExecutionTimes;
		
		this.succCount = succCount;
		this.taskType = taskType;
		this.startTimeMS = startMS;
		this.endTimeMS = endMS;
		
		this.clientId = clientId;
		this.queueId = queueId;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return operationExecutionTimes.length;
	}

	/**
	 * @return the succCount
	 */
	public int getSuccCount() {
		return succCount;
	}

	/**
	 * @return the taskType
	 */
	public QueryName getTaskType() {
		return taskType;
	}

	/**
	 * @return the startTimeMS
	 */
	public long getStartTimeMS() {
		return startTimeMS;
	}

	/**
	 * @return the endTimeMS
	 */
	public long getEndTimeMS() {
		return endTimeMS;
	}

	/**
	 * @return the clientId
	 */
	public int getClientId() {
		return clientId;
	}

	/**
	 * @return the operationExecutionTimes
	 */
	public long[] getOperationExecutionTimes() {
		return operationExecutionTimes;
	}

	/**
	 * @return the queueId
	 */
	public int getQueueId() {
		return queueId;
	}

}
