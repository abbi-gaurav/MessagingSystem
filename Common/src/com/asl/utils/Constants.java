package com.asl.utils;

public class Constants {
	public static final int BUFFER_SIZE 			= 3*1024;

//	public static final String OPERATION 			= "Operation";
	
	public static final String KEY_VALUE_DELIM 		= ":";
	public static final String HEADERS_DELIM 		= ";";
	public static final String HEADERS_BODY_DELIM 	= "#";
	public static final String QUEUE_NAME_DELIM 	= "!!";
	
//	public static final String QUEUE_NAME_LABEL 	= "QueueName";
	public static final String IS_PEEK	 			= "isPeek";
	public static final String RECEIVER_LABEL 		= "receiver";
	public static final String PRIORITY_LABEL 		= "priority";
	public static final String REPLY_QUEUE_LABEL 	= "ReplyQueue";
	public static final String CLIENT_ID_LABEL 		= "ClientId";
	public static final String QUEUE_ID_LABEL 		= "QueueId";
	public static final String SENDER_ID_LABEL 		= "SenderId";
	public static final String REQUEST_LABEL 		= "Request";
	public static final String CONTEXT_LABEL 		= "Context";
	public static final String QUERY_TYPE_LABEL 	= "QueryType";
	public static final String CLIENT_ROLE_LABEL	= "ClientRole";
	public static final String DELETED_QUEUES_LABEL	= "QueuesToBeDeleted";
//	public static final String POST_IN_QUEUES_LABEL	= "PostInQueues";
	public static final String MSG_ORDER_BY			= "MsgOrderBy";
	public static final String DEST_ID_LABEL		= "DestinationId";
	public static final String BODY_LABEL			= "Body";

	public static final String MESSAGES_LABEL 		= "Messages";
	

	public static final String NEXT 				= "NEXT";

	public static final String QUIT 				= "QUIT";

	public static final String MESSAGES_COUNT_LABEL 		= "Message Count";
	public static final String QUEUES_PRINT_FORMAT_HEADER 	= "%32s%32s";
	public static final String[] QUEUES_PRINT_FORMAT_HEADERS = new String[]{Constants.QUEUE_ID_LABEL, Constants.MESSAGES_LABEL};
	
	public static final String MESSAGES_FORMAT_HEADER 		= "%10s%10s%10s%45s%10s";
	public static final String[] MSGS_FORMAT_HEADERS = new String[]{Constants.MESSAGE_ID_LABEL,
		Constants.FROM_LABEL,Constants.TO_LABEL, Constants.TIME_STAMP,Constants.PRIORITY_LABEL};
	
	public static final String MESSAGE_DETAIL_FORMAT_HEADER = "%25s%10s%10s%10s%10s%40s%40s%20s";
	public static final String[] MSG_DETAIL_FORMAT_HEADERS = new String[]{Constants.MESSAGE_ID_LABEL,Constants.FROM_LABEL,Constants.TO_LABEL, 
																	"Unknown",Constants.PRIORITY_LABEL, Constants.TIME_STAMP,Constants.MESSAGE_BODY,Constants.MESSAGE_COUNTER}; 
	public static final String MESSAGE_BODY 		=  "MESSAGE_BODY";

	public static final String CLEAR 				= "clear";

	public static final String CLS 					= "cls";

	public static final String WINDOW 				= "Window";

	public static final String OS_NAME 				= "os.name";

	public static final String ENTER_QUIT_TO_EXIT 	= "Enter <QUIT> to exit";

	public static final String BACK 				= "BACK";

	public static final String ALL_QUEUES_IDENTIFIER = "[-1]";

	public static final String TO_ID_LABEL 			= "ToClient";

	public static final String MESSAGE_ID_LABEL 	= "MessageId";

	public static final String NONE = "none";

	public static final String SAMPLE_MESSAGE_BODY = "Sample Message body";

	public static final String TIME_TAKEN = "Time Taken(ms)";

	public static final String SUCC_REQ = "Success Count";

	public static final String NUM_REQ = "Total Request";

	public static final String TOTAL_TIME_TAKEN = "Total Time taken(ms):=		";

	public static final String FROM_LABEL = "FROM";

	public static final String TO_LABEL = "TO";

	public static final String TIME_STAMP = "TIMESTAMP";

	public static final String MESSAGE_COUNTER = "MsgCounter";

	public static final String PERF_RESULT_TXT_FILE = "perfResult.txt";

	public static final String LOGS_DIR = "logs";

	public static final String PATH_SEPERATOR = System.getProperty("file.separator");

	public static final String USER_HOME = System.getProperty("user.home");

	public static final String SAMPLE_REPLY = "Sample Reply";

	public static final String REPLIED_MSG_ID = "RepliedMsgId";

	public static final String PERF_RUNS_DIR = "perfRuns";

	public static final String MINUS_ONE = "-1";

	public static final String SPACE = " ";

	public static final String TEST_RUN_DIR = "testRun";

	public static final String STATUS = "Status";

	public static final String PERF_DB_MEASURES = "perfDB";

	public static final String SEND_QUEUE_ID_LABEL = "SendToQueueId";

	public static final String RCEIVER_QUEUE_ID_LABEL = "ReceiverQueue";
	
	public static final String HOP_COUNT_LABEL = "HOP_COUNT";
}
