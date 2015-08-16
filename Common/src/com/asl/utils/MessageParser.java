package com.asl.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageParser {
	private static final Logger LOGGER = Logger.getLogger(MessageParser.class.getCanonicalName());
	
	public enum MessageType {
		Request, Response
	}

	public static final Message parse(String messageStr, MessageType msgType) throws ValidationException{
		try {
			Utils.verifyNotNullBlank(messageStr);
			
			//split body and headers
			String[] headerBody = messageStr.split(Constants.HEADERS_BODY_DELIM);
			assert(headerBody.length <= 2);
			
			//split headers
			String[] headers = headerBody[0].split(Constants.HEADERS_DELIM);
			//convert to hashmap
			//parse header key values
			Map<String, String> headerMap = parseHeaders(headers);
			
			validateCommon(headerMap);
			
			if(msgType == MessageType.Request){
				validateRequestHeaders(headerMap);
			}else {
				validateResponseHeaders(headerMap);
			}
			
			Message message = new Message(msgType, headerMap, (headerBody.length == 2 ? headerBody[1] : null));
			
			LOGGER.log(Level.FINE, "Message created by server is {0}", new Object[]{message});
			return message;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "error in parsing message {0} of length{1}",new Object[]{messageStr, messageStr.length()});
			throw e;
		}
	}

	private static void validateResponseHeaders(Map<String, String> headerMap) throws ValidationException {
		if(!headerMap.containsKey(MessageType.Response.toString())) throw new ValidationException("No Message Type");
	}

	private static void validateRequestHeaders(Map<String, String> headerMap) throws ValidationException {
		if(!headerMap.containsKey(Constants.QUERY_TYPE_LABEL)) throw new ValidationException("No Message Type");
	}

	private static void validateCommon(Map<String, String> headerMap) throws ValidationException {
//		if(!headerMap.containsKey(Constants.QUEUE_NAME_LABEL)) throw new ValidationException("Queue name not provided");
		
//		if(!headerMap.containsKey(Constants.CLIENT_ID_LABEL)) throw new ValidationException("Client id not provided");
		
	}

	private static Map<String, String> parseHeaders(String[] headers) {
		try {
			Map<String, String> headersMap = new HashMap<String, String>();
			for(String header:headers){
				String[] keyValue = header.split(Constants.KEY_VALUE_DELIM);
				assert(keyValue.length == 2);
				
				headersMap.put(keyValue[0], keyValue[1]);
			}
			
			return headersMap;
		} catch(ArrayIndexOutOfBoundsException e) {
			LOGGER.log(Level.SEVERE, "error in parsing headers {0}", new Object[]{Arrays.toString(headers)});
			throw e;
		}
	}
	
	public static void main(String[] args) throws ValidationException {
		parse("Response:FAILURE#", MessageType.Response);
	}
	
}
