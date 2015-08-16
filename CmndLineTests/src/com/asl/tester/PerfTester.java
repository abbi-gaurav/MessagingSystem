package com.asl.tester;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.asl.client.exception.ClientException;
import com.asl.client.io.ClientIO;
import com.asl.client.io.IClientIO;
import com.asl.utils.Constants;
import com.asl.utils.DataConstants;
import com.asl.utils.DataConstants.MsgLength;
import com.asl.utils.QueryName;
import com.asl.utils.ValidationException;

public class PerfTester {
	private final ExecutorService fixedThreadPool;
	private final QueryName[] reqTypes;
	private final int numReqEachClient;
	private final int numClients;
	private final int[] queueIds;
	private final MsgLength msgLength;
	private final TestDelegator[] testDelegators;
	
	public PerfTester(int numClients, int count, String remoteHost,int port, QueryName[] reqTypes,int[] queueIds,  MsgLength msgLength, int[] clientIds) 
	throws IOException, ClientException, ValidationException {
		fixedThreadPool = Executors.newFixedThreadPool(numClients);
		this.numReqEachClient = count;
		
		Arrays.sort(reqTypes);
		this.reqTypes = reqTypes;
		
		this.numClients = numClients;
		this.msgLength = msgLength;
		
		if(create(queueIds)){
			this.queueIds = new int[numClients];
		}else{
			this.queueIds = queueIds;
		}
		
		InetAddress remoteAddress = InetAddress.getByName(remoteHost);
		
		testDelegators = new TestDelegator[numClients];
		for(int i=0;i<numClients;i++){
			IClientIO clientIO = new ClientIO(remoteAddress, port);
			testDelegators[i] = create(clientIds) ? new TestDelegator(clientIO)
												   :new TestDelegator(clientIO, clientIds.length == 1?clientIds[0]:clientIds[i] );
			if(create(queueIds)){
				this.queueIds[i] = testDelegators[i].doCreateQueue();
			}
		}
	}

	private boolean create(int[] ids) {
		return ids == null || ids.length < 1 || (ids.length == 1 && ids[0] == -1);
	}
	
	public static void main(String[] args) throws InterruptedException, ClientException, ValidationException, IOException {
		if(args.length < 6){
			printHelp();
			System.exit(1);
		}
		PerfTester tester = null;
		try {
			String remoteHost = args[0];
			int port = Integer.parseInt(args[1]);
			int numClients = Integer.parseInt(args[2]);
			int numRequestsEachClient = Integer.parseInt(args[3]);
			int[] queueIds = stringToIntegersArr(args[4],Constants.SPACE);
			
			DataConstants.MsgLength msgLength = MsgLength.valueOf(args[5]);
			
			QueryName[] requestTypes = {QueryName.POST,QueryName.RETRIEVE_MESSAGE};
			
			if(args.length > 6 && !args[6].equals("default")){
				requestTypes = setQueries(args[6]);
			}
			int[] clientIds = null; 
			
			if(args.length > 7){
				clientIds = stringToIntegersArr(args[7], Constants.SPACE);
				
				assert (clientIds.length == queueIds.length);
			}
			tester = new PerfTester(numClients, numRequestsEachClient, remoteHost, port, requestTypes,queueIds,msgLength, clientIds);
		} catch (Exception e) {
			printHelp();
			e.printStackTrace();
			return;
		}
		tester.test();
	}

	public static int[] stringToIntegersArr(String input, String delimiter) {
		String[] ids = input.split(delimiter);
		int [] idsInt = new int[ids.length];
		for (int i=0;i<ids.length;i++) {
			idsInt[i] = Integer.parseInt(ids[i]); 
		}
		return idsInt;
	}

	public static QueryName[] setQueries(String str) {
		String[] queries = str.split(" ");
		QueryName[] requestTypes = new QueryName[queries.length];
		
		for(int i=0;i< queries.length;i++){
			requestTypes[i] = QueryName.valueOf(queries[i]);
		}
		
		return requestTypes;
	}

	private void printResults(TestWorker[] workers,QueryName reqType) throws FileNotFoundException {
		System.out.format("%20s%20s%20s%20s%20s%32s", 
				Constants.CLIENT_ID_LABEL,Constants.QUERY_TYPE_LABEL,Constants.QUEUE_ID_LABEL,
				Constants.NUM_REQ,Constants.SUCC_REQ,Constants.TIME_TAKEN);
		long totalTimeTaken = 0;

		try(PrintStream ps = new PrintStream(Constants.USER_HOME+Constants.PATH_SEPERATOR
												+Constants.TEST_RUN_DIR+Constants.PATH_SEPERATOR
												+Constants.LOGS_DIR+Constants.PATH_SEPERATOR
												+Constants.PERF_RUNS_DIR+Constants.PATH_SEPERATOR
												+reqType+""
												+msgLength+""
												+""+System.currentTimeMillis()
												+Constants.PERF_RESULT_TXT_FILE)){
			ps.println("Test date:::"+new Date());
			for(int i=0;i<numClients;i++){
				Result result = workers[i].getResult();
				System.out.println();
				long timeTaken = result.getEndTimeMS() - result.getStartTimeMS();
				totalTimeTaken += timeTaken;
				System.out.format("%20d%20s%20d%20d%20d%32d", result.getClientId(), result.getTaskType().toString(),
																result.getQueueId(),result.getCount(), 
																result.getSuccCount(), timeTaken);
				ps.println(result.getClientId()+"\t"+Arrays.toString(result.getOperationExecutionTimes()));
			}
			System.out.println();
			System.out.println(Constants.TOTAL_TIME_TAKEN+totalTimeTaken);
		}
	}

	private void test() throws ClientException, ValidationException, InterruptedException, IOException {
		try {
			//one time
			for(QueryName reqType:reqTypes){
				TestWorker[] workers = new TestWorker[numClients];
				CountDownLatch latch = new CountDownLatch(numClients);
				for(int i=0;i<numClients;i++){
					TestWorker worker = new TestWorker(testDelegators[i]
														, numReqEachClient
														, reqType
														, queueIds.length > 1 ? queueIds[i]:queueIds[0]
														,latch
														,msgLength.getStr());
					workers[i] = worker;
					fixedThreadPool.submit(worker);
				}
				latch.await();
				printResults(workers,reqType);
			}
		} finally{
			for(TestDelegator delegator:testDelegators){
				delegator.close();
			}
			fixedThreadPool.shutdown();
		}
	}

	private static void printHelp() {
		System.out.println("usage:");
		System.out.println("<MiddlewareRemoteHostAddress> <PORT> <NumOfClients> "
				+ "<NumOfRequestEachClient (-1 for infinite)>"
				+ "<QueueId(-1, will create one queue for each client)>"
				+"<Message Length>"
				+ " <RequestType>(optional, will do post and receive if not specified)");

		System.out.println("valid Length options are:");
		System.out.println(Arrays.toString(MsgLength.values()));
		
		System.out.println("valid request types are:");
		System.out.println(Arrays.toString(QueryName.values()));
	}
}
