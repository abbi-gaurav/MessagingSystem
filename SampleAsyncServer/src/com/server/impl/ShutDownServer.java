package com.server.impl;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShutDownServer {
	private static final Logger LOGGER = Logger.getLogger(ShutDownServer.class.getCanonicalName());
	
	public static void main(String[] args) {
		CountDownLatch awaitshutdown = AsyncIOServer.awaitshutDown;
		if(awaitshutdown != null && awaitshutdown.getCount() != 0){
			LOGGER.log(Level.INFO, "Shutdown the server");
			awaitshutdown.countDown();
		}
	}
}
