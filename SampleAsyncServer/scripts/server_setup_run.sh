if [ $# -lt 8 ]
then
	echo "usage remoteUserName serverMachine middlewarePort ioTPSize processorTPSize dbConnPoolSize dbHost dbPort"
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

echo " Copying testRun.tar to server machine: $serverMachine ... "
# Copy tar to server machine
scp testRun.tar $remoteUserName@$serverMachine:/tmp


######################################
#
# Run server 
#
######################################

#explode tar file
echo "  exloding the tar to home dir of $remoteUserName "
ssh $remoteUserName@$serverMachine "tar -xvf /tmp/testRun.tar -C ~/" 

#create directory with experimetId
mkdir ~/testRun/logs/perfRuns/${experimetId}

echo "  Starting the server"
ssh $remoteUserName@$serverMachine "cd ~/testRun/scripts; java -server -Xmx1536m -Xms1536m -cp ../jars/server/Middleware.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${homeDir}/testRun/logs/properties/server/logging.properties com.server.impl.AsyncIOServer $middlewarePort $ioTPSize $processorTPSize $dbConnPoolSize $dbHost $dbPort 2>&1 > ~/testRun/logs/perfRuns/${experimetId}/server.out"

# Wait for the server to start up
echo -ne "  Waiting for the middleware to start up..."
sleep 1
while [ `ssh $remoteUserName@$serverMachine "cat ~/testRun/logs/perfRuns/${experimetId}/server.out | grep 'Middleware started' | wc -l"` != 1 ]
do
	sleep 1
done 
echo "OK"
