package com.server.impl.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Constants;
import com.asl.utils.Utils;
import com.server.impl.database.DBManager;
import com.server.impl.processor.MsgProcessor;

public class Bridge {
	private static final Logger LOGGER = Logger.getLogger(Bridge.class.getCanonicalName());
	
	private final AsynchronousSocketChannel socketChannel;
	private final ExecutorService processorTP;
	private final DBManager dbMgr;
	private final String message;
	private long startTimeInMS;

	private final long counter;
	
	public Bridge(AsynchronousSocketChannel socketChannel, String message, ExecutorService processorTP, DBManager dbManager, long counter) {
		this.socketChannel = socketChannel;
		this.message = message;
		this.processorTP = processorTP;
		dbMgr = dbManager;
		this.counter = counter;
	}
	
	public void invokeProcessor(){
		LOGGER.log(Level.FINE, "invoking processing of message for socket channel {0}",new Object[]{socketChannel});
		startTimeInMS = System.currentTimeMillis();
		
		processorTP.execute(new MsgProcessor(message, this));
	}
	
	public void invokeWrite(Map<String, String> headers, String messageBody){
		try{
			LOGGER.log(Level.FINE, "invoking writing of message for socket channel {0}, headers{1} and body{2}",new Object[]{socketChannel, headers, messageBody});
			System.out.println(socketChannel.toString()+";"+counter+";"+";Processing;Timetaken;"+(System.currentTimeMillis()-startTimeInMS));
			
			int totalMsgLength = calculateLength(headers,messageBody);
			ByteBuffer buffer = createBufferAndWriteHeaders(headers, totalMsgLength);
			
			if(Utils.isNotBlank(messageBody)){
				addBody(messageBody, buffer);
			}
			
			Utils.markEnd(buffer);
			
			buffer.flip();
			
			socketChannel.write(buffer, counter, new WriteHandler(socketChannel,dbMgr,processorTP, buffer));
			LOGGER.log(Level.FINE, "done invoking writing of message for socket channel {0}",new Object[]{socketChannel});
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, "Error during write: "+e.getMessage(), e);
		}
	}

	private int calculateLength(Map<String, String> headers, String messageBody) {
		int length = 0;
		for(Entry<String, String> entry:headers.entrySet()){
			length += entry.getKey().length();
			length += entry.getValue().length();
		}
		
		if(Utils.isNotBlank(messageBody)){
			length += messageBody.length();
		}
		return length;
	}

	private void addBody(String messageBody, ByteBuffer buffer) {
		buffer.put(Utils.stringToBytes(Constants.HEADERS_BODY_DELIM));
		byte[] bodyBytes = Utils.stringToBytes(messageBody);
		buffer.put(bodyBytes);
		
		LOGGER.log(Level.FINE, "writing body of size {0}", new Object[]{bodyBytes.length});
	}

	private ByteBuffer createBufferAndWriteHeaders(Map<String, String> headers, int totalMsgLength) {
		LOGGER.log(Level.FINE, "Writing headers {0}", new Object[]{headers});
		
		ByteBuffer buffer = Utils.getByteBuffer();
		Utils.writeHeaders(headers, buffer);
		return buffer;
	}

	public DBManager getDBManager() {
		return dbMgr;
	}
	
	
}
