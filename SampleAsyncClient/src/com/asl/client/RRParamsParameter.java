package com.asl.client;

public class RRParamsParameter {
	private final int context;
	private final int replyQueue;

	public RRParamsParameter(int context, int replyQueue) {
		this.context = context;
		this.replyQueue = replyQueue;
	}

	public int getContext() {
		return context;
	}

	public int getReplyQueue() {
		return replyQueue;
	}
}