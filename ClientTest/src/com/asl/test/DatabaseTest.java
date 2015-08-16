package com.asl.test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

import com.asl.utils.Constants;
import com.server.impl.database.DBManager;
import com.server.impl.processor.MessageFields;
import com.server.impl.processor.task.CheckMessageFrom;
import com.server.impl.processor.task.Post;
import com.server.impl.processor.task.Read;
import com.server.impl.processor.task.RetrieveMessage;
import com.server.impl.processor.task.Task;

public class DatabaseTest {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		System.out.println("Hello World!");
		System.out.println(Arrays.toString(new Integer[]{}));
        DBManager db = new DBManager(5,"localhost",5432,"postgres");
        MessageFields m = new MessageFields(1,2,9,"Hey man! mess "+System.currentTimeMillis());
        Task t[] = {//new NewClient("r"), new NewQueue(2), new DeleteQueue(3,new Integer[]{1,7,12,15}),
        		/*new ListQueue(3), new ListQueueWithMessage(2),*/ new Post(m,new Integer[]{8}),
        		new Read(8,1), new CheckMessageFrom(2,1), new RetrieveMessage(8, 1)};
        for (Task task : t) {
			HashMap<String, String> headers = new HashMap<String, String>();
			task.doTask(db,headers);
			System.out.println("TASK TYPE: "+task.getType());
			System.out.println(task.getClass().getCanonicalName()+": "+(headers.containsKey(Constants.BODY_LABEL) ? headers.get(Constants.BODY_LABEL) : "no body"));
        }
        
        
        db.terminate();

	}

}
