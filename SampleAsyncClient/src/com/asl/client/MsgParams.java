package com.asl.client;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.asl.utils.Utils;

public class MsgParams {

	final Map<String, String> headers;

	public MsgParams() {
		this.headers = new HashMap<>(5);
	}

	protected void addHeaders(ByteBuffer buffer) {
		Utils.writeHeaders(headers, buffer);
	}

}