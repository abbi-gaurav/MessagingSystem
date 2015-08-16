#!/bin/bash

if [ $# -lt 9 ];
then
	echo "usage ./create_clients_queues remoteUserName clientMachine middlewareHost middlewarePort reqType msgLength runTimeInSeconds experimentId noOfClients "
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
reqType=$5
msgLength=$6 
runTimeInSeconds=$7
experimentId=$8
noOfClients=$9

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
if [ ! -d ~/testRun/logs/perfRuns/${experimentId} ];
then
	mkdir ~/testRun/logs/perfRuns/${experimentId}
fi

# Run the clients
clientIds=`seq $noOfClients`
pids=""
for clientId in $clientIds
do
	echo "    Start client: $clientId"
	java -Xmx128m -Xms128m -cp ../jars/client/CmndLineTests.jar:../jars/client/Client.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${homeDir}/testRun/logs/properties/client/logging.properties com.asl.tester.SingleClientContinuousRun $middlewareHost $middlewarePort $reqType $clientId $clientId $msgLength $runTimeInSeconds $experimentId > ~/testRun/logs/perfRuns/${experimentId}/out.client${clientId} & 
	pids="$pids $!"
done

# Wait for the clients to finish
echo -ne "  Waiting for the clients to finish ... "
for f in $pids
do
	wait $f
done
echo "OK"
