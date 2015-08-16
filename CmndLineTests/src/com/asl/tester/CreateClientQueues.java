package com.asl.tester;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.asl.client.exception.ClientException;
import com.asl.client.io.ClientIO;
import com.asl.utils.ValidationException;

public class CreateClientQueues {

	public static void main(String[] args) throws UnknownHostException, ClientException, ValidationException {
		int numberClients = Integer.parseInt(args[0]);
		int numberQueues = Integer.parseInt(args[1]);
		String middlewareHost = args[2];
		int middlewarePort = Integer.parseInt(args[3]);

		for(int i=0; i<numberClients;i++){
			try(TestDelegator delegator = new TestDelegator(new ClientIO(InetAddress.getByName(middlewareHost), middlewarePort))){
				System.out.println("client created: "+delegator.getClientId());

				if(i == (numberClients -1)){
					for(int j=0;j<numberQueues;j++){
						int queueId = delegator.doCreateQueue();
						System.out.println("queue create: "+queueId);
					}
				}
			}
		}
	}
}
