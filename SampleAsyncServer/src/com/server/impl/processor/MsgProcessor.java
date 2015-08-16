package com.server.impl.processor;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Constants;
import com.asl.utils.Message;
import com.asl.utils.MessageParser;
import com.asl.utils.MessageParser.MessageType;
import com.asl.utils.ValidationException;
import com.server.impl.exception.TaskBuildException;
import com.server.impl.handlers.Bridge;
import com.server.impl.processor.task.Task;
import com.server.impl.processor.task.TaskBuilder;


public class MsgProcessor implements Runnable{
	private static final Logger LOGGER = Logger.getLogger(MsgProcessor.class.getCanonicalName());

	private final String message;
	private final Bridge bridge;

	public MsgProcessor(String message, Bridge bridge) {
		this.message = message;
		this.bridge = bridge;
	}
	
	@Override
	public void run() {
		//TODO message processing logic
		//parse request
		//proceess and do db operations
		//call bridge.invokewrite
		try {
			Message msgObject = MessageParser.parse(message, MessageType.Request);

			HashMap<String, String> headers = new HashMap<String, String>();
			
			try {
				long startTime = System.currentTimeMillis();
				Task t = TaskBuilder.buildTask(msgObject);
				t.doTask(bridge.getDBManager(), headers);
				ProcessorPerfMeasurer.getPerfRecorder().add(new PerfRecord(startTime,System.currentTimeMillis(),t.getType(),t.getQueuesId(),t.getClientId(),headers.get(MessageType.Response.toString())));
			} catch (TaskBuildException e) {
				LOGGER.log(Level.SEVERE, "Problem building task: "+e.getMessage(),e);
				TaskBuilder.buildTaskFailedErrorAnswer(headers);
			}
			String body ="";
			if(headers.containsKey(Constants.BODY_LABEL)){
				body = headers.get(Constants.BODY_LABEL);
				headers.remove(Constants.BODY_LABEL);
			}
				
			LOGGER.log(Level.FINE, "processing done for message {0}, returning response back", new Object[]{message});
			
			bridge.invokeWrite(headers, body);
		} catch (ValidationException e) {
			LOGGER.log(Level.SEVERE, "error in processing: "+e.getMessage(),e);
		}
	}

}
