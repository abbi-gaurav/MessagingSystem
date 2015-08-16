#!/bin/bash

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

	java -Xmx1536m -Xms1536m -cp ../jars/client/CmndLineTests.jar:../jars/client/Client.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=$homeDir/logs/properties/client/logging.properties com.asl.tester.SingleClientContinuousRun $1 $2 $3 $4 $5 $6 $7 $8

