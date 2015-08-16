package com.asl.test;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.asl.client.exception.ClientException;
import com.asl.tester.TestDelegator;
import com.asl.utils.ValidationException;

@RunWith(value = Parameterized.class)
public class ClientTests {
	private static final int DUMMY_SENDER = 205;

	private static final int PRIORITY = 9;

	private static final String msgBody = "this is a message body";

	private final int[] queueNames;

	private final TestDelegator delegator;
	
	public ClientTests(int clientId , int[] queueNames) throws ClientException, ValidationException {
		this.delegator = new TestDelegator(new MockClientIO());
		this.queueNames = queueNames;
	}
	
	@Parameters
	public static Collection<Object[]> getParameters(){
		Object[][] arr = new Object[][]{{160, new int[]{1,2,34,56}}};
		return Arrays.asList(arr);
	}
	
	@Test
	public void testSendMessage() throws ClientException, ValidationException{
		System.out.println(delegator.doSend(false,PRIORITY, msgBody, queueNames));
	}
	
	@Test
	public void testReceive() throws ClientException, ValidationException{
		System.out.println(delegator.doRecv(false,queueNames[0]));
	}
	
	@Test
	public void testCreateQueue() throws ClientException, ValidationException{
		System.out.println(delegator.doCreateQueue());
	}
	
	@Test
	public void testDeleteQueue() throws ClientException, ValidationException{
		System.out.println(delegator.doDeleteQueue(queueNames[0]));
	}
	
	@Test
	public void testListQueue() throws ClientException, ValidationException{
		System.out.println(delegator.doListQueue());
	}
	

	@Test
	public void testListWithMessageQueue() throws ClientException, ValidationException{
		System.out.println(delegator.doListWithMsgQueue());
	}
	

	@Test
	public void testBroadCast() throws ClientException, ValidationException{
		System.out.println(delegator.doSend(true,PRIORITY,msgBody));
	}
	
	@Test
	public void testcheckMsgFrom() throws ClientException, ValidationException{
		System.out.println(delegator.doCheckMsgFrom(DUMMY_SENDER));
	}
	
	@Test
	public void testRead() throws ClientException, ValidationException{
		System.out.println(delegator.doRecv(true,queueNames[0]));
	}
	
}
