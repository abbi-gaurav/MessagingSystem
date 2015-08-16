package com.server.impl.database.connPool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionCreator implements PoolItemCreator<Connection>{
	private final String dbName;
	private final String dbHost;
	private final String dbPort;
	private final String dbUser;
	private final String dbPassword;
	
	/**
	 * @param dbName
	 * @param dbHost
	 * @param dbPort
	 * @param dbUser
	 * @param dbPassword
	 */
	public DBConnectionCreator(String dbName, String dbHost, String dbPort,
			String dbUser, String dbPassword) {
		this.dbName = dbName;
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
	}

	@Override
	public Connection create() {
		try {
			return DriverManager.getConnection("jdbc:postgresql://"
												+dbHost
												+":"+dbPort
												+"/"+dbName, dbUser, dbPassword);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
