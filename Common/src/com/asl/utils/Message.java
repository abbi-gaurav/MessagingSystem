package com.asl.utils;

import java.util.Map;

import com.asl.utils.MessageParser.MessageType;

public class Message {
	private final MessageType msgType;
	private final Map<String, String> headers;
	private final String body;

	public Message(MessageType msgType, Map<String, String> headerMap,String msgBody){
		this.msgType = msgType;
		this.headers = headerMap;
		this.body 	=  msgBody;
	}

	public String getValue(String key){
		return headers.get(key);
	}

	public MessageType getMsgType() {
		return msgType;
	}

	public String getBody() {
		return body;
	}
	
	public Map<String, String> getHeaders(){
		return headers;
	}
	public void addHeader(String key, String value){
		headers.put(key, value);
	}
	@Override
	public String toString() {
		return msgType.toString()
				+"--"+headers.toString()
				+(body == null ? "":"--"+body) ;
	}

}
