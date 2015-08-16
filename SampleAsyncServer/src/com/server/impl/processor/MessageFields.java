package com.server.impl.processor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.asl.utils.Constants;
import com.server.impl.exception.IncompleteRecordException;

public class MessageFields {
	private int id=-1;
	private int counter=-1;
	private String time="-1";
	private int to_id;
	private int from_id;
	private int context = -1;
	private int priority;
	private String body;
	private boolean one_way;
	private boolean to_all;
	
	public MessageFields(int tid,int fid,int p,String b,int c){
		this(tid,fid,p,b);
		context =c;
		one_way=false;
	}
	public MessageFields(int tid,int fid,int p,String b){
		//check if p between 1 and 10 done in canCreateMessageField method
		//TODO: check if b less than 2000 char
		this(fid,p,b);
		to_id =tid;
		to_all=false;
	}
	public MessageFields(int fid,int p,String b,int c){
		this(fid,p,b);
		context=c;
		one_way=false;
	}
	
	public MessageFields(int fid, int p,String b,int c, int toId){
		this(fid,p,b);
		context=c;
		one_way=false;
		to_all = false;
		to_id = toId;
	}
	
	public MessageFields(int fid,int p,String b){
		from_id=fid;
		priority=p;
		body=b;
		to_all=true;
		one_way=true;
	}
	
	public MessageFields(int tid,int fid,int p,String b,int c,int i,int co,String t){
		this(fid,p,b);
		id=i;
		counter=co;
		time=t;
		to_all = tid == -1;
		one_way = c == -1;
		context = c;
		to_id = tid;
	}
	
	public void prepareStmt(PreparedStatement stmt, int offset) throws SQLException{
		if(!to_all)
			stmt.setInt(1+offset, to_id);
		else
			stmt.setNull(1+offset, java.sql.Types.INTEGER);
		stmt.setInt(2+offset, from_id);
		if(!one_way)
			stmt.setInt(3+offset, context);
		else
			stmt.setNull(3+offset, java.sql.Types.INTEGER);
		stmt.setInt(4+offset, priority);
		stmt.setString(5+offset, body);
	}
	public int getFromId() {
		return from_id;
	}

	public int getId() {
		return id;
	}
	
	@Override
	public String toString(){
		String m1 = to_all ? "to all clients" : "to client "+to_id,
			m2 = one_way ? "no context (one way)" : "context "+context,
			m3 = id!=-1 ? ""+id : "",
			m4 = counter!=-1 ? "is available in "+counter+" queue(s)" : "",
			m5 = !time.equals("-1") ? "arrived at "+time : "";
		return "Message "+m3+" from client "+from_id+" "+m1+" with priority "+priority+" and "+m2+" and body "+body+" "+m4+" "+m5+".";
	}
	
	public static MessageFields readMessage(String mess) throws SQLException{
		mess = mess.substring(mess.indexOf('(')+1);
		mess = mess.substring(0,mess.lastIndexOf(")"));
		String[] m = mess.split(",",-1);	//include empty string ie null values
		//check if can build message from string
		if(m[0].isEmpty()||m[1].isEmpty()||m[4].isEmpty()||m[5].isEmpty()||m[7].isEmpty())
			try {	//TODO: log exception
				throw new IncompleteRecordException("Incomplete record: "+mess);
			} catch (IncompleteRecordException e) {
				return null;
			}
		//message = id, from_id, to_id, context, priority, time, body, counter
		int id,fid,tid,c,p,co;
		String t,b;
		
		id = Integer.parseInt(m[0]);
		fid = Integer.parseInt(m[1]);
		tid = m[2].isEmpty() ? -1 : Integer.parseInt(m[2]);
		c = m[3].isEmpty() ? -1 : Integer.parseInt(m[3]);
		p = Integer.parseInt(m[4]);
		t = m[5].substring(1,m[5].length()-1);	//remove first and last "
		b = m[6].substring(1,m[6].length()-1);	//remove first and last "
		
		co = Integer.parseInt(m[7]);
		
		return new MessageFields(tid,fid,p,b,c,id,co,t);
	}
	
	public static boolean canCreateMessageField(Map<String, String> headerMap){
		int p = Integer.parseInt(headerMap.get(Constants.PRIORITY_LABEL));
		return headerMap.containsKey(Constants.CLIENT_ID_LABEL)
				&&headerMap.containsKey(Constants.PRIORITY_LABEL)&& p>=1 && p<=10;
	}
	public String getBody() {
		return body;
	}
	
	public boolean isOneWay(){
		return one_way;
	}
	
	public int getContext() {
		return context;
	}
}
