#!/bin/bash
if [ $# -lt 3 ]
then
	echo "usage ./runConsole.sh dbHost dbPort dbUserName "
	exit 1
fi 

java -cp ../jars/server/Middleware.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar:../jars/console/Console.jar com.asl.console.MgmtConsole $1 $2 $3
