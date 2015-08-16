package com.server.impl.processor.task;

import java.util.HashMap;
import java.util.Map;

import com.asl.utils.Constants;
import com.asl.utils.Message;
import com.asl.utils.QueryName;
import com.asl.utils.ReceiveBy;
import com.asl.utils.Status;
import com.asl.utils.MessageParser.MessageType;
import com.asl.utils.Utils;
import com.server.impl.exception.TaskBuildException;
import com.server.impl.processor.MessageFields;

public class TaskBuilder {

	public static Task buildTask(Message m) throws TaskBuildException {
		Map<String, String> headers = m.getHeaders();

		checkCanBuildTask(headers);

		switch (QueryName.valueOf(headers.get(Constants.QUERY_TYPE_LABEL))) {
		case NEW_CLIENT:
			return new NewClient(headers.get(Constants.CLIENT_ROLE_LABEL));
		case NEW_QUEUE:
			return new NewQueue(Integer.parseInt(headers
					.get(Constants.CLIENT_ID_LABEL)));
		case DELETE_QUEUE:
			return new DeleteQueue(Integer.parseInt(headers
					.get(Constants.CLIENT_ID_LABEL)),
					Utils.parseIdArray(headers
							.get(Constants.QUEUE_ID_LABEL)));
		case LIST_QUEUE:
			return new ListQueue(Integer.parseInt(headers
					.get(Constants.CLIENT_ID_LABEL)));
		case LIST_QUEUE_WITH_MESSAGE:
			return new ListQueueWithMessage(Integer.parseInt(headers
					.get(Constants.CLIENT_ID_LABEL)));
		case POST:
		case BROADCAST:// same as post, queues array will contain -1 for
						// broadcast
			return buildPostTask(headers, getMessageFields(headers, m.getBody()));
		case READ:
			return new Read(Integer.parseInt(headers
					.get(Constants.QUEUE_ID_LABEL)), Integer.parseInt(headers
					.get(Constants.CLIENT_ID_LABEL)));
		case CHECK_MESSAGE_FROM:
			return new CheckMessageFrom(Integer.parseInt(headers
					.get(Constants.SENDER_ID_LABEL)), Integer.parseInt(headers
					.get(Constants.CLIENT_ID_LABEL)));
		case RETRIEVE_MESSAGE:
			return new RetrieveMessage(Integer.parseInt(headers
					.get(Constants.QUEUE_ID_LABEL)), Integer.parseInt(headers
					.get(Constants.CLIENT_ID_LABEL)), ReceiveBy.valueOf(headers
					.get(Constants.MSG_ORDER_BY)));
		default:
			// System.err.println("ERROR no query exist with this name: "+queryName);
			return null; // this case shouldn't happen.
		}
	}

	// all mess are expected to contain the query type, the client id (no value
	// if new client) and the type of the message
	public static void checkCanBuildTask(Map<String, String> headerMap)
			throws TaskBuildException {
		QueryName queryType = QueryName.valueOf(headerMap.get(Constants.QUERY_TYPE_LABEL));
		
		if (!(queryType == QueryName.NEW_CLIENT || headerMap.containsKey(Constants.CLIENT_ID_LABEL)))
			throw new TaskBuildException("No Message Type ("
					+ !headerMap.containsKey(MessageType.Response.toString())
					+ ") " + "or/and Query Type ("
					+ !headerMap.containsKey(Constants.QUERY_TYPE_LABEL) + ") "
					+ "or/and Client id ("
					+ !headerMap.containsKey(Constants.CLIENT_ID_LABEL) + ")");
		boolean paramMissing = false;
		String errorMess = "ERROR in " + queryType + " query: ";
		switch (queryType) {
		case NEW_CLIENT:
			if (!headerMap.containsKey(Constants.CLIENT_ROLE_LABEL)) {
				errorMess += "no client role specified";
				paramMissing = true;
			}
			break;
		case NEW_QUEUE:
			break;
		case DELETE_QUEUE:
			if (!headerMap.containsKey(Constants.QUEUE_ID_LABEL)) {
				errorMess += "no queues id specified";
				paramMissing = true;
			}
			break;
		case LIST_QUEUE:
			break;
		case LIST_QUEUE_WITH_MESSAGE:
			break;
		case POST:
		case BROADCAST:
			if (!MessageFields.canCreateMessageField(headerMap)) {
				errorMess += "missing/wrong arguments to post/broadcast message";
				paramMissing = true;
			}
			break;
		case READ:
			if (!headerMap.containsKey(Constants.QUEUE_ID_LABEL)) {
				errorMess += "missing arguments to read message";
				paramMissing = true;
			}
			break;
		case CHECK_MESSAGE_FROM:
			if (!headerMap.containsKey(Constants.SENDER_ID_LABEL)) {
				errorMess += "missing arguments to check message from specific sender";
				paramMissing = true;
			}
			break;
		case RETRIEVE_MESSAGE:
			if (!headerMap.containsKey(Constants.QUEUE_ID_LABEL)
					|| !headerMap.containsKey(Constants.MSG_ORDER_BY)) {
				errorMess += "missing arguments to retrieve message";
				paramMissing = true;
			}
			break;
		default:
			throw new TaskBuildException(
					"Specified query type isn't supported: " + queryType);
		}

		if (paramMissing)
			throw new TaskBuildException(errorMess);
	}

	public static MessageFields getMessageFields(Map<String, String> headers,
			String body) {
		boolean one_way = Utils.isBlank(headers.get(Constants.CONTEXT_LABEL)) || 
								Constants.NONE.equals(headers.get(Constants.CONTEXT_LABEL));
		boolean to_all = !headers.containsKey(Constants.DEST_ID_LABEL);
		int from_id = Integer.parseInt(headers.get(Constants.CLIENT_ID_LABEL)),
			priority = Integer.parseInt(headers.get(Constants.PRIORITY_LABEL));
		if(to_all){
			if(one_way){
				return new MessageFields(from_id, priority, body); 
			}
			else{
				int c = Integer.parseInt(headers.get(Constants.CONTEXT_LABEL));
				return new MessageFields(from_id, priority, body,c);
			}
		}
		else{
			if(!one_way){
				//FIXME this is wrong
				int c = Integer.parseInt(headers.get(Constants.CONTEXT_LABEL));
				if(!to_all) {
					int to_id = Integer.parseInt(headers.get(Constants.DEST_ID_LABEL));
					return new MessageFields(from_id, priority, body,c, to_id); 
				}else{
					return new MessageFields(from_id, priority, body,c); 
				}
			}
			else
				return new MessageFields(from_id, priority, body);
		}
	}
	
	public static Post buildPostTask(Map<String, String> headers, MessageFields m){
		boolean broadcast = !headers.containsKey(Constants.QUEUE_ID_LABEL);
		if(broadcast)
			return new Post(m);
		else
			return new Post(m,	Utils.parseIdArray(headers.get(Constants.QUEUE_ID_LABEL)));
	}

	public static void buildTaskFailedErrorAnswer(
			HashMap<String, String> headers) {
		headers.put(MessageType.Response.toString(), Status.FAILURE.toString());
		headers.put(Constants.BODY_LABEL, "Query Incomplete");
	}

}
