package com.asl.client.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.client.exception.ClientException;
import com.asl.utils.Utils;

public class ClientIO implements IClientIO {
	private static final Logger LOGGER = Logger.getLogger(ClientIO.class.getCanonicalName());
	
	private final int port;
	private final AsynchronousSocketChannel socketChannel;

	public ClientIO(InetAddress middleware, int port) throws ClientException{
		try {
			this.port = port;
			this.socketChannel = AsynchronousSocketChannel.open();

			socketChannel.connect(new InetSocketAddress(middleware,this.port)).get();
			
			LOGGER.log(Level.INFO, "Connected to middleware {0}", new Object[]{socketChannel.getRemoteAddress()});
		} catch (InterruptedException | ExecutionException |IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new ClientException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.asl.client.io.IClientIO#write(java.nio.ByteBuffer)
	 */
	@Override
	public int write(ByteBuffer buffer) throws InterruptedException, ExecutionException{
		buffer.flip();  
				
		LOGGER.log(Level.FINER,"Before write buffer position-{0}, limit-{1}, capacity-{2}, remaining{3}", 
										new Object[]{ buffer.position(), buffer.limit(), buffer.capacity(),buffer.remaining()});
		int bytesWritten = socketChannel.write(buffer).get();
		
		LOGGER.log(Level.FINER,"Before write buffer position-{0}, limit-{1}, capacity-{2}, remaining{3}", 
										new Object[]{ buffer.position(), buffer.limit(), buffer.capacity(),buffer.remaining()});
		
		
		if(buffer.hasRemaining()){
			socketChannel.write(buffer).get();
			LOGGER.log(Level.FINER, "Not all written yet");
		}else{
			LOGGER.log(Level.FINER, "all written, clearing the buffer");
			buffer.clear();
		}
		
		
		return bytesWritten;
	}
	
	/* (non-Javadoc)
	 * @see com.asl.client.io.IClientIO#read(java.nio.ByteBuffer)
	 */
	@Override
	public String read(ByteBuffer buffer) throws InterruptedException, ExecutionException{
		int msgLength = 0;
		ArrayDeque<byte[]> byteSequences = null;
		while(true){
			int bytesRead = socketChannel.read(buffer).get();
			byte[] bytes = Utils.bufferToBytes(bytesRead, buffer);
			msgLength += bytes.length;
			
			LOGGER.log(Level.FINE, "Read the bytes {0}", new Object[]{bytes.length});
			boolean complete = Utils.isComplete(buffer);
			buffer.clear();
			
			if(!complete){
				if(byteSequences == null){
					byteSequences = new ArrayDeque<>(2);
				}
				byteSequences.add(bytes);
				LOGGER.log(Level.FINER, "Not all read yet");
			}else{
				String messageString = Utils.createMessage(byteSequences, bytes, msgLength);
				LOGGER.log(Level.FINER, "Read the full message {0} of length {1}",new Object[]{messageString,msgLength});
				return messageString;
			}
		}
	}
	
	@Override
	public void close() {
		Utils.close(socketChannel);
	}

	/**
	 * @return the socketChannel
	 */
	@Override
	public AsynchronousSocketChannel getSocketChannel() {
		return socketChannel;
	}

	/**
	 * @return the port
	 */
	@Override
	public int getPort() {
		return port;
	}
}
