#!/bin/bash

if [ $# -lt 4 ];
then
	echo "usage ./create_clients_queues numClients numQueues middlewareHost middlewarePort"
	exit -1
fi

/home/user26/jdk/jdk1.7.0_45/bin/java -Xmx256m -Xms256m -cp ../jars/client/CmndLineTests.jar:../jars/client/Client.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${homeDir}/testRun/logs/properties/client/logging.properties com.asl.tester.CreateClientQueues $1 $2 $3 $4 
