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

echo " Copying testRun.tar to server machine: $clientMachine ... "
# Copy tar to server machine
scp testRun.tar $remoteUserName@$clientMachine:/tmp


######################################
#
# Run Tests 
#
######################################

#explode tar file
echo "  exloding the tar to home dir of $remoteUserName "
ssh $remoteUserName@$clientMachine "tar -xvf /tmp/testRun.tar -C ~/" 

#create directory with experimetId
mkdir ~/testRun/logs/perfRuns/${experimetId}

# Run the clients
clientIds=`seq $noOfClients`
pids=""
for clientId in $clientIds
do
	echo "    Start client: $clientId"
	ssh $remoteUserName@$clientMachine "cd ~/testRun/scripts; java -Xmx1536m -Xms1536m -cp ../jars/client/CmndLineTests.jar:../jars/client/Client.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${homeDir}/testRun/logs/properties/client/logging.properties com.asl.tester.SingleClientContinuousRun $middlewareHost $middlewarePort $reqType -1 -1 $msgLength $runTimeInSeconds $experimentId > ~/testRun/logs/perfRuns/${experimentId}/out.client${clientId}" & 
	pids="$pids $!"
done

# Wait for the clients to finish
echo -ne "  Waiting for the clients to finish ... "
for f in $pids
do
	wait $f
done
echo "OK"
