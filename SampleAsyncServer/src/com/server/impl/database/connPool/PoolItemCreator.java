package com.server.impl.database.connPool;

public interface PoolItemCreator<T>{
	public T create();
}
