package com.server.impl.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Utils;
import com.server.impl.database.DBManager;

public class ReadHandler implements CompletionHandler<Integer, Long>{
	private static final Logger LOGGER = Logger.getLogger(ReadHandler.class.getCanonicalName());

	private final AsynchronousSocketChannel socketChannel;
	private final ByteBuffer buffer;
	private final ExecutorService processorTP;
	private final DBManager db_manager;
	private ArrayDeque<byte[]> byteSequences;
	private int msgLength;
	private long startTimeInMs = System.currentTimeMillis();
	
	public ReadHandler(AsynchronousSocketChannel channel, ByteBuffer byteBuffer, ExecutorService processorTP, DBManager db_m) {
		this.socketChannel = channel;
		this.buffer = byteBuffer;
		this.processorTP = processorTP;
		db_manager = db_m;
	}

	@Override
	public void completed(Integer bytesRead, Long counter) {
		try {
			if(bytesRead == -1){
				//do processing and write back
				LOGGER.log(Level.FINE, "EOF reached for channel", new Object[]{socketChannel});
				//close now
				Utils.close(socketChannel);
			}else{
				byte[] bytes = Utils.bufferToBytes(bytesRead, buffer);
				msgLength += bytes.length;
				
				LOGGER.log(Level.FINE, "Read the bytes {0} and message length is {1} for socket channel {2}", 
																new Object[]{bytes.length,msgLength, socketChannel});
				
				boolean isComplete = Utils.isComplete(this.buffer);
				buffer.clear();
				
				afterRead(isComplete, bytes, counter);
			}
		} catch (Exception e) {
			failed(e, null);
		}
	}

	private void afterRead(boolean isComplete, byte[] bytes, long counter) {
		if(!isComplete){
			LOGGER.log(Level.FINE, "Not all read yet for channel {0}", new Object[]{socketChannel});
			if(byteSequences == null){
				byteSequences = new ArrayDeque<>(2);
			}
			byteSequences.add(bytes);
			socketChannel.read(buffer, counter, this);
		}else{
			LOGGER.log(Level.FINE, "all read yet for channel {0},invoking the processor", new Object[]{socketChannel});
			new Bridge(socketChannel, Utils.createMessage(byteSequences,bytes, msgLength), processorTP, db_manager, counter).invokeProcessor();
			
			System.out.println(socketChannel.toString()+";"+counter+";"+";Read;Timetaken;"+(System.currentTimeMillis()-startTimeInMs));
			
			startTimeInMs = 0;
			msgLength = 0;
		}
	}

	@Override
	public void failed(Throwable exc, Long attachment) {
		LOGGER.log(Level.SEVERE, "Failed for socket channel {0}", new Object[]{socketChannel});
		LOGGER.log(Level.SEVERE, exc.getMessage(), exc);
		Utils.close(socketChannel);
	}
}
