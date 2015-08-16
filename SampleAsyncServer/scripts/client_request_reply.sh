#!/bin/bash
if [ $# -lt 9  ]
then
	echo "./client_request_reply.sh remoteUserName clientMachine middlewareHost middlewarePort msgLength runTimeInSeconds experimentId noOfRRClients noOfOWClients"
	echo "valid requests are:\n"
	echo "[NEW_CLIENT, NEW_QUEUE, DELETE_QUEUE, LIST_QUEUE, POST, BROADCAST, READ, LIST_QUEUE_WITH_MESSAGE, CHECK_MESSAGE_FROM, RETRIEVE_MESSAGE]\n"
	echo "valid message length values are"
	echo "[HundredCharacters, TwoHundredChracters, FiveHundredCharcaters, TwoKCharcaters, OneKCharacters, FifteenHundredCharcaters]\n"

	exit 1

fi
remoteUserName=$1
clientMachine=$2
middlewareHost=$3
middlewarePort=$4 
msgLength=$5 
runTimeInSeconds=$6
experimentId=$7
noOfRRClients=$8
noOfOWClients=$9

homeDir=""
if [ "$USER_HOME" ]
then
	echo "user home"
	homeDir=$USER_HOME
elif [ "$HOME" ]
then
	echo "home"
	homeDir=$HOME 
else
	echo "home dir not set"
	exit 1
fi

######################################
#
# Run Tests 
#
######################################

#create directory with experimentId
echo "expid -- $experimentId"
if [ ! -d ~/testRun/logs/perfRuns/${experimentId} ]
then
	mkdir ~/testRun/logs/perfRuns/${experimentId}
fi
half=2
peer2peer=`expr $noOfRRClients / $half`
poolBased=`expr $noOfRRClients / $half`

# Run the peer-2-peer clients
peers=`seq $peer2peer`
pids=""
#request reply scenario
for (( clientId=1; clientId<=$peer2peer; clientId++  ))
do
	client1=$clientId
	client2=`expr $clientId + 1`
	contextId=$client1
	reqQueue=$client1
	replyQueue=$client2
	echo "port number is ${middlewarePort}"
	echo "    Starting peers  $client1-$client2"
	/home/user26/jdk/jdk1.7.0_45/bin/java -Xmx32m -Xms32m -cp ../jars/client/CmndLineTests.jar:../jars/client/Client.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${homeDir}/testRun/logs/properties/client/logging.properties com.asl.tester.RequestReplySender $client1 $reqQueue $replyQueue $middlewareHost $middlewarePort $msgLength $runTimeInSeconds $contextId $client2 $experimentId > ~/testRun/logs/perfRuns/${experimentId}/out.client${client1} & 
	pids="$pids $!"
	
	/home/user26/jdk/jdk1.7.0_45/bin/java -Xmx32m -Xms32m -cp ../jars/client/CmndLineTests.jar:../jars/client/Client.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${homeDir}/testRun/logs/properties/client/logging.properties com.asl.tester.RequestReplyReceiver $client2 $reqQueue $replyQueue $middlewareHost $middlewarePort $msgLength $runTimeInSeconds $experimentId > ~/testRun/logs/perfRuns/${experimentId}/out.client${client2} & 
	pids="$pids $!"
	clientId=`expr $clientId + 1`
done
#pool baser services
poolReqQueue=`expr $peer2peer + 1`
poolRespQueue=`expr $peer2peer + 2`
for (( poolClientId=`expr $peer2peer + 1`; poolClientId<=$noOfRRClients; poolClientId++  ))
do
	poolReqClient=$poolClientId
	poolRespClient=`expr $poolClientId + 1`
	poolContextId=$poolReqClient
	echo "starting pool request client $poolReqClient and pool server client $poolRespClient"
	/home/user26/jdk/jdk1.7.0_45/bin/java -Xmx32m -Xms32m -cp ../jars/client/CmndLineTests.jar:../jars/client/Client.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${homeDir}/testRun/logs/properties/client/logging.properties com.asl.tester.RequestReplySender $poolReqClient $poolReqQueue $poolRespQueue $middlewareHost $middlewarePort $msgLength $runTimeInSeconds $poolContextId $poolRespClient $experimentId > ~/testRun/logs/perfRuns/${experimentId}/out.client${poolReqClient} & 
	pids="$pids $!"
	
	/home/user26/jdk/jdk1.7.0_45/bin/java -Xmx32m -Xms32m -cp ../jars/client/CmndLineTests.jar:../jars/client/Client.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${homeDir}/testRun/logs/properties/client/logging.properties com.asl.tester.RequestReplyReceiver $poolRespClient $poolReqQueue $poolRespQueue $middlewareHost $middlewarePort $msgLength $runTimeInSeconds $experimentId > ~/testRun/logs/perfRuns/${experimentId}/out.client${poolRespClient} & 
	pids="$pids $!"
	poolClientId=`expr $poolClientId + 1`
done
#One Way clients
OWClientQueue=`expr $poolRespQueue + 1`
clientRangeStart=`expr $noOfRRClients + 1`
clientRangeEnd=`expr $clientRangeStart - 1 + $noOfOWClients`
for (( OWClientId=$clientRangeStart; OWClientId<=$clientRangeEnd; OWClientId++  ))
do
	echo "starting one way client --$OWClientId--$OWClientQueue--$clientRangeStart--$clientRangeEnd--$middlewareHost--$middlewarePort--$msgLength--$runTimeInSeconds--$experimentId"
	/home/user26/jdk/jdk1.7.0_45/bin/java -Xmx32m -Xms32m -cp ../jars/client/CmndLineTests.jar:../jars/client/Client.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${homeDir}/testRun/logs/properties/client/logging.properties com.asl.tester.OneWayClient $OWClientId $OWClientQueue $clientRangeStart $clientRangeEnd $middlewareHost $middlewarePort $msgLength $runTimeInSeconds $experimentId > ~/testRun/logs/perfRuns/${experimentId}/out.client${OWClientId} & 
	pids="$pids $!"
done

# Wait for the clients to finish
echo $pids >> ~/testRun/logs/client/pids${middlewarePort}.txt
