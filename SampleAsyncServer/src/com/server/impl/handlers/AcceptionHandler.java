package com.server.impl.handlers;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Utils;
import com.server.impl.database.DBManager;

public class AcceptionHandler implements CompletionHandler<AsynchronousSocketChannel, Void>{
	private static final Logger LOGGER = Logger.getLogger(AcceptionHandler.class.getCanonicalName());
	
	private final AsynchronousServerSocketChannel serverChannel;
	private final ExecutorService processorTP;
	private final DBManager db_manager;

	private long counter;

	public AcceptionHandler(AsynchronousServerSocketChannel serverChannel, ExecutorService processorTP, DBManager dbMgr) {
		this.serverChannel = serverChannel;
		this.processorTP = processorTP;
		this.db_manager = dbMgr;
	}
	
	@Override
	public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
		LOGGER.log(Level.FINE, "Accept completed and got socketchannel {0}", new Object[]{socketChannel});
		serverChannel.accept(null, this);
		
		setOptions(socketChannel);
		
		try {
			IOUtils.initRead(socketChannel, processorTP, db_manager, ++counter);
		} catch (Exception e) {
			failed(e, null);
			Utils.close(socketChannel);
		}
	}

	private void setOptions(AsynchronousSocketChannel socketChannel) {
		try {
			socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "set options failed socket channel {0}", new Object[]{socketChannel});
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		}
	}

	@Override
	public void failed(Throwable exc, Void attachment) {
		LOGGER.log(Level.SEVERE, "Failed for socket channel {0}", new Object[]{serverChannel});
		LOGGER.log(Level.SEVERE, exc.getMessage(), exc);
	}

}
