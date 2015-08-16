package com.asl.tester;

import com.asl.utils.QueryName;
import com.asl.utils.DataConstants.MsgLength;

public class OWResultValues extends ResultValues{
	public final long countHop;
	//public final type trackHop; 
	
	public OWResultValues(QueryName taskType, long operationTime, int primaryQueue, MsgLength msgLength, int nthHop) {
		super(taskType, operationTime, primaryQueue, msgLength);
		this.countHop = nthHop;
		//add trackHop
	}
}
