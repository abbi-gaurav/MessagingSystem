#!/bin/bash
if [ $# -lt 8 ]
then
	echo "Usage <Middleware Host Address> <Middleware Port> <Number of Clients> <NumOfRequestEachClient (-1 for infinite)> <QueueId(-1, will create one queue for each client)> <Message Length> <RequestType(default ( POST RETRIEVE_MESSAGE )or valid requestTypes) > <Client Ids (-1 will  create new clients)>" 
	echo "valid requests are:\n"
	echo "[NEW_CLIENT, NEW_QUEUE, DELETE_QUEUE, LIST_QUEUE, POST, BROADCAST, READ, LIST_QUEUE_WITH_MESSAGE, CHECK_MESSAGE_FROM, RETRIEVE_MESSAGE]\n"
	echo "valid message length values are"
	echo "[HundredCharacters, TwoHundredChracters, FiveHundredCharcaters, TwoKCharcaters, OneKCharacters, FifteenHundredCharcaters]\n"
	exit 1
fi
echo "Running tests on "

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

	java -Xmx1536m -Xms1536m -cp ../jars/client/CmndLineTests.jar:../jars/client/Client.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=$homeDir/logs/properties/client/logging.properties com.asl.tester.PerfTester $1 $2 $3 $4 $5 $6 $7 $8

