package com.server.impl.handlers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Utils;
import com.server.impl.database.DBManager;

public class IOUtils {
	public static final Logger LOGGER = Logger.getLogger(IOUtils.class.getCanonicalName());

	public static void initRead(AsynchronousSocketChannel socketChannel,
			ExecutorService processorTP, DBManager dbMgr, Long attachment) {
		ByteBuffer byteBuffer = Utils.getByteBuffer();
		ReadHandler readHandler = new ReadHandler(socketChannel, byteBuffer, processorTP, dbMgr);
		
		LOGGER.log(Level.FINE, "Started read handler");
		
		socketChannel.read(byteBuffer, attachment, readHandler);
	}

}
