package com.server.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.server.impl.database.DBManager;
import com.server.impl.handlers.AcceptionHandler;

public class AsyncIOServer {
	private static final Logger LOGGER = Logger.getLogger(AsyncIOServer.class.getCanonicalName());
	static final CountDownLatch awaitshutDown = new CountDownLatch(1);
	
	private final AsynchronousServerSocketChannel listener;
	private final AcceptionHandler acceptHandler;

	private final ExecutorService processorTP;
	
	private final DBManager db_manager;

	public AsyncIOServer(int port, ExecutorService ioThreadPool,  ExecutorService proceeorTP, DBManager db_m) throws IOException {
		AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withThreadPool(ioThreadPool);
		
		this.processorTP = proceeorTP;
		this.db_manager = db_m;
		this.listener = AsynchronousServerSocketChannel.open(channelGroup).bind(new InetSocketAddress(port));
		this.acceptHandler = new AcceptionHandler(listener,processorTP,db_manager);
		
		LOGGER.log(Level.INFO, "Started middleware on address {0}", new Object[]{this.listener.getLocalAddress()});
		System.out.println("Middleware started");
		listener.accept(null, acceptHandler);
	}
	
	
	public static void main(String[] args) throws NumberFormatException, IOException, InterruptedException, SQLException {
		ExecutorService ioThreadPool = Executors.newFixedThreadPool(Integer.parseInt(args[1]));
		ExecutorService processorTP = Executors.newFixedThreadPool(Integer.parseInt(args[2]));
		DBManager db_m = new DBManager(Integer.parseInt(args[3]),args[4],Integer.parseInt(args[5]),args[6]);
		AsyncIOServer server = new AsyncIOServer(Integer.parseInt(args[0]), ioThreadPool, processorTP, db_m);
		
		awaitshutDown.await();
		
		server.shutdown();
	}

	private void shutdown() throws IOException {
		LOGGER.log(Level.INFO, "Stopping middleware on address {0}", new Object[]{this.listener.getLocalAddress()});
		this.listener.close();
		this.processorTP.shutdown();
	}

}
