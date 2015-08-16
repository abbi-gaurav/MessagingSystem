#!/bin/bash
if [ $# -lt 6 ]
then
	echo "Usage <Middleware Port> <IO Thread Pool Size> <Procesor Thread Pool Size> <DB Connection Pool Size> <db host> <db port>"
	exit 1
fi
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

echo "starting middleware on port $1 with IO Thread Pool of size $2, Procesor Thread Pool of size $3 and DB Connection Pool of sie $4 db remote host $5 db remote port $6"
java -server -Xmx1536m -Xms1536m -cp ../jars/server/Middleware.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file="$homeDir"/logs/properties/server/logging.properties com.server.impl.AsyncIOServer $1 $2 $3 $4 $5 $6
