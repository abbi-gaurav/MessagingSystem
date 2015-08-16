package com.asl.test;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

import com.asl.client.io.IClientIO;
import com.asl.utils.Constants;
import com.asl.utils.MessageParser.MessageType;
import com.asl.utils.Status;

public class MockClientIO implements IClientIO{

	@Override
	public int write(ByteBuffer buffer) throws InterruptedException,
			ExecutionException {
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()-1];
		buffer.get(bytes);
		System.out.println(new String(bytes));
		return 0;
	}

	@Override
	public String read(ByteBuffer buffer) throws InterruptedException,
			ExecutionException {
		return MessageType.Response
				+Constants.KEY_VALUE_DELIM
				+Status.SUCCESS.toString();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	public AsynchronousSocketChannel getSocketChannel() {
		return null;
	}

	public int getPort() {
		return 0;
	}

}
