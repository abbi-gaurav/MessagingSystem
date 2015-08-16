package com.server.impl.database.connPool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Pool<T extends AutoCloseable> {
	private static final Logger LOGGER = Logger.getLogger(Pool.class.getCanonicalName());
	private final BlockingQueue<T> pool;
	
	public Pool(BlockingQueue<T> objects) {
		pool = objects;
	}
	
	public T borrow() throws InterruptedException{
		return pool.take();
	}
	
	public void giveBack(T poolItem) throws InterruptedException{
		pool.put(poolItem);
	}

	public static <I extends AutoCloseable> Pool<I> configure(int size,PoolItemCreator<I> creator) {
		BlockingQueue<I> list = new ArrayBlockingQueue<I>(size);
		for(int i = 0; i< size; i++){
			list.add(creator.create());
		}
		
		return new Pool<I>(list);
	}
	
	public void close(){
		for(T item:pool){
			try {
				item.close();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
}
