package com.server.impl.exception;


public class ServerException extends Exception {
	private static final long serialVersionUID = -5657821543123214145L;

	public ServerException(Exception e) {
		super(e);
	}


}
