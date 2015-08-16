if [ $# -lt 10 ]
then
	echo "usage remoteUserName serverMachine middlewarePort ioTPSize processorTPSize dbConnPoolSize dbHost dbPort experimentId "
	exit 1
fi
remoteUserName=$1
serverMachine=$2
middlewarePort=$3 
ioTPSize=$4 
processorTPSize=$5
dbConnPoolSize=$6
dbHost=$7
dbPort=$8
experimentId=$9
serverMemory=${10}

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
# Run server 
#
######################################

#create directory with experimentId
if [ ! -d ~/testRun/logs/perfRuns/${experimentId} ]
then
	mkdir ~/testRun/logs/perfRuns/${experimentId}
fi

echo "  Starting the server"
echo "params passed are $middlewarePort $ioTPSize $processorTPSize $dbConnPoolSize $dbHost $dbPort "
/home/user26/jdk/jdk1.7.0_45/bin/java -server -Xmx${serverMemory}m -Xms${serverMemory}m -cp ../jars/server/Middleware.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${homeDir}/testRun/logs/properties/server/logging.properties com.server.impl.AsyncIOServer $middlewarePort $ioTPSize $processorTPSize $dbConnPoolSize $dbHost $dbPort $remoteUserName 2>&1 > ~/testRun/logs/perfRuns/${experimentId}/server.out &

# Wait for the server to start up
echo -ne "  Waiting for the middleware to start up..."
sleep 1
while [ `cat ~/testRun/logs/perfRuns/${experimentId}/server.out | grep 'Middleware started' | wc -l` != 1 ]
do
	sleep 1
done 
echo "OK"
