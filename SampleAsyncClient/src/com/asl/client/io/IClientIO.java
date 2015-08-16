package com.asl.client.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public interface IClientIO {

	public abstract int write(ByteBuffer buffer) throws InterruptedException,
			ExecutionException, IOException;

	public abstract String read(ByteBuffer buffer) throws InterruptedException,
			ExecutionException, IOException;

	public abstract void close();

	public abstract AsynchronousSocketChannel getSocketChannel();

	public abstract int getPort();

}