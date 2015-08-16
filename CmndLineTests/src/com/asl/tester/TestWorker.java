package com.asl.tester;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.client.exception.ClientException;
import com.asl.utils.Message;
import com.asl.utils.QueryName;
import com.asl.utils.ValidationException;

public class TestWorker implements Callable<Result>{
	private static final Logger LOGGER = Logger.getLogger(TestWorker.class.getCanonicalName());
	
	private final TestDelegator testdelegator;
	private final int runCount;
	private QueryName taskType;
	private int succCount;

	private final int queueId;

	private final CountDownLatch latch;

	private Result result;

	private final String msgBody;
	
	public TestWorker(TestDelegator testDelegator, int runCount, QueryName taskType,
						int queueId, CountDownLatch latch, String msgBody) throws ClientException, ValidationException {
		this.testdelegator = testDelegator;
		
		assert (testdelegator.getClientId() != -1);
		
		if(queueId == -1) {
			this.queueId = testDelegator.doCreateQueue();
		}else{
			this.queueId = queueId;
		}
		
		this.runCount = runCount;
		this.taskType = taskType;
		this.latch = latch;
		this.msgBody = msgBody;
	}
	
	@Override
	public Result call() throws Exception {
		long startms = System.currentTimeMillis();
		long endMS = 0;
		long[] operationTime = new long[runCount];
		for(int i=0; i<runCount;i++){
			operationTime[i] = doOperation();
			endMS = System.currentTimeMillis();
		}
		result = new Result(operationTime, succCount, taskType, testdelegator.getClientId(), startms, endMS, queueId);
		latch.countDown();
		return result;
	}

	private long doOperation() {
		long operationStart = System.currentTimeMillis();
		try{
			switch (taskType) {
			case NEW_QUEUE:
				testdelegator.doCreateQueue();
				break;
			case BROADCAST:
				testdelegator.doSend(true, 9, msgBody);
				break;
			case POST:
				testdelegator.doSend(false, 9, msgBody, queueId);
				break;
			case RETRIEVE_MESSAGE:
				Message msg = testdelegator.doRecv(false, queueId);
				assert (msg.getBody().equals(this.msgBody));
				break;
			case READ:
				testdelegator.doRecv(true, queueId);
				break;
			case LIST_QUEUE:
				testdelegator.doListQueue();
				break;
			case DELETE_QUEUE:
				testdelegator.doDeleteQueue(queueId);
				break;
			default:
				break;
			}
		}catch(Exception |AssertionError e){
			LOGGER.log(Level.SEVERE, "Request failed for client id: "+testdelegator.getClientId(), e);
			return -1;
		}
		
		succCount++;
		return System.currentTimeMillis() - operationStart;
	}

	/**
	 * @return the result
	 */
	public Result getResult() {
		return result;
	}
	
	
	public static void main(String[] args) {
		QueryName[] values = QueryName.values();
		System.out.println(Arrays.toString(values));
		Arrays.sort(values);
		
		System.out.println(Arrays.toString(values));
	}

}
