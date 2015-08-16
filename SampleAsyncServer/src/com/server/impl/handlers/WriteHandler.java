package com.server.impl.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Utils;
import com.server.impl.database.DBManager;

public class WriteHandler implements CompletionHandler<Integer, Long> {
	private static final Logger LOGGER = Logger.getLogger(WriteHandler.class.getCanonicalName());
	
	private final AsynchronousSocketChannel socketChannel;

	private final DBManager dbMgr;

	private final ExecutorService processorTp;

	private final ByteBuffer buffer;
	
	private long startTimeInMS = System.currentTimeMillis();
	
	public WriteHandler(AsynchronousSocketChannel socketChannel, DBManager db_manager, ExecutorService processorTP, ByteBuffer buffer) {
		this.socketChannel = socketChannel;
		this.dbMgr = db_manager;
		this.processorTp = processorTP;
		this.buffer = buffer;
	}

	@Override
	public void completed(Integer result, Long counter) {
		try {
			if(buffer.hasRemaining()){
				LOGGER.log(Level.FINE, "Not all written remaining {0}, calling next write socketchannel {1}", 
						new Object[]{buffer.remaining(), socketChannel});
				socketChannel.write(buffer, counter, new WriteHandler(socketChannel,dbMgr,processorTp, buffer));
			}else{ 
				if(socketChannel.isOpen()){
					LOGGER.log(Level.FINE, "Write callback completed for socketchannel {0}", new Object[]{socketChannel});
					IOUtils.initRead(socketChannel, processorTp, dbMgr, (counter+1));

				}
				System.out.println(socketChannel.toString()+";"+counter+";"+";Write;Timetaken;"+(System.currentTimeMillis()-startTimeInMS));
				startTimeInMS = 0;
			}
		} catch (Exception e) {
			failed(e, null);
		}
	}

	@Override
	public void failed(Throwable exc, Long attachment) {
		LOGGER.log(Level.SEVERE, "Failed for socket channel {0}", new Object[]{socketChannel});
		LOGGER.log(Level.SEVERE, exc.getMessage(), exc);
		Utils.close(socketChannel);
	}

}
