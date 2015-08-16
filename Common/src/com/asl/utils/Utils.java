package com.asl.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {
	private static final Logger LOGGER = Logger.getLogger(Utils.class.getCanonicalName());

	public static byte[] stringToBytes(String string) {
		return string.getBytes();
	}

	public static void addKeyValue(ByteBuffer buffer, String key, String value) {
		buffer.put(stringToBytes(key));
		buffer.put(stringToBytes(Constants.KEY_VALUE_DELIM));
		buffer.put(stringToBytes(value));
	}

	public static void writeHeaders(Map<String, String> headers, ByteBuffer buffer) {
		
		Iterator<Entry<String, String>> entrySet = headers.entrySet().iterator();
		while(entrySet.hasNext()){
			Entry<String, String> entry = entrySet.next();
			addKeyValue(buffer, entry.getKey(), entry.getValue());
			
			if(entrySet.hasNext()){
				buffer.put(stringToBytes(Constants.HEADERS_DELIM));
			}
		}
	}

	public static boolean isComplete(ByteBuffer buffer) {
		int position = buffer.position();
		return buffer.get(position-1) == -1;
	}

	/**
	 * take a buffer to which bytes have been written, the extract them to byte[]
	 * performs flip also
	 * @param bytesRead
	 * @param buffer
	 * @return
	 */
	public static byte[] bufferToBytes(Integer bytesRead, ByteBuffer buffer) {
		buffer.flip();
		byte[] bytes = new byte[bytesRead];
		buffer.get(bytes);
		return bytes;
	}

	public static String getQueueNamesString(String... queueNames) {
		if(queueNames.length == 1){
			return queueNames[0];
		}
		
		return Arrays.toString(queueNames);
	}
	
	public String[] queueNames(String queueNames){
		return queueNames.split(Constants.QUEUE_NAME_DELIM);
		
	}
	public static String createMessage(ArrayDeque<byte[]> previousByteSeq, byte[] lastBytes, int totalMsgLength) {
		if(previousByteSeq != null && previousByteSeq.size() > 0){
			int size = previousByteSeq.size();
			ByteBuffer buffer = getByteBuffer(totalMsgLength);
			for(int i=0;i<size;i++){
				buffer.put(previousByteSeq.remove());
			}
			buffer.put(lastBytes);
			
			byte[] totalMsgBytes = bufferToBytes(totalMsgLength, buffer);
			return new String(totalMsgBytes,0,totalMsgBytes.length-1);
		}else{
			return new String(lastBytes,0,lastBytes.length-1);
		}
	}

	/**
	 * exceptions are caught and logged
	 * @param socketChannel
	 */
	public static void close(AsynchronousSocketChannel socketChannel) {
		try {
			LOGGER.log(Level.FINE, "closing the channel {0}", new Object []{socketChannel});
			socketChannel.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "error in close: "+e.getMessage(),e);
		}
	}

	public static Integer[] parseIdArray(String idArrayAsString){
		String[] strings = idArrayAsString.replace("[", "").replace("]", "").split(", ");
	    Integer result[] = new Integer[strings.length];
	    for (int i = 0; i < result.length; i++) {
	      result[i] = Integer.parseInt(strings[i]);
	    }
	    return result;
	}

	public static void addBody(ByteBuffer buffer, String messageBody){
		buffer.put(stringToBytes(Constants.HEADERS_BODY_DELIM));
		byte[] bytes = messageBody.getBytes();
		buffer.put(bytes);
	}

	public static ByteBuffer getByteBuffer() {
		return ByteBuffer.allocate(Constants.BUFFER_SIZE);
	}
	
	public static ByteBuffer getByteBuffer(int size) {
		return size <= Constants.BUFFER_SIZE ? ByteBuffer.allocate(Constants.BUFFER_SIZE)
											: ByteBuffer.allocate(size);
	}
	
	public static void markEnd(ByteBuffer buffer) {
		//mark end of data
		LOGGER.log(Level.FINER, "marking end of data");
		buffer.put((byte)-1);
	}

	public static void verifyNotNullBlank(String string) {
		assert isNotBlank(string);
	}

	public static boolean isNotBlank(String string) {
		return string != null && string.trim().length() > 0;
	}
	
	public static boolean isBlank(String string) {
		return !isNotBlank(string);
	}
	
	public static String removeStartAndEndQuotes(String str, String quote){
		if(str.startsWith(quote) && str.endsWith(quote)){
			return str.substring(1, str.length()-1);
		}
		return str;
	}
	
	public static String[] convertDBResultSet(String result){
		if(result.startsWith("(")) result = result.substring(1);
		if(result.endsWith(")")) result = result.substring(0, result.length()-1);
		return result.split(",");
	}
}
