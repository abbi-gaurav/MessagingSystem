package com.server.impl.processor;

import com.asl.utils.QueryName;

public class PerfRecord {
	private long start_time;
	private long end_time;
	private long duration;
	private QueryName type;
	private String queues_id;
	private int client_id;
	private String status;
	
	public PerfRecord(long s, long e, QueryName t, String q, int c, String st){
		start_time=s;
		end_time=e;
		duration=e-s;
		type=t;
		queues_id=q;
		client_id=c;
		status =st;
	}
	
	public long getStartTime(){
		return start_time;
	}
	public long getEndTime(){
		return end_time;
	}
	public long getDuration(){
		return duration;
	}
	public String getQueues(){
		return queues_id;
	}
	public QueryName getType(){
		return type;
	}
	public int getClientId(){
		return client_id;
	}
	public String getStatus(){
		return status;
	}
}
