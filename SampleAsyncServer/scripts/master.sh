function usage() {
        echo "Usage: serverMachine<address> clientMachine<address> noOfClients<int> remoteUserName<username> experimentId<id> clientRunTime<seconds> ioTPSieze<int> processorTpSize<int> dbConnPool<int> dbHost<address> dbPort<int> reqType<string> msgLength<string>" 
        exit -1
}
if [ $# -lt 13 ]
then
	usage()
fi

serverMachine=$1
clientMachine=$2
noOfClients=$3
remoteUserName=$4
experimentId=$5
clientRunTime=$6
ioTPSize=$7
processorTpSize=$8
dbConnPool=$9
dbHost=${10}
dbPort=${11}
reqType=${12}
msgLength=${13}

#db set up

# server setup
./server_setup_run.sh $remoteUserName $serverMachine 2500 $ioTPSize $processorTpSize $dbConnPool $dbHost $dbPort

#test run
./client_setup_run.sh $remoteUserName $clientMachine $serverMachine 2500 $reqType $msgLength $clientRunTime $experimentId $noOfClients

#kill all
echo "  Sending shut down signal to server"
# Send a shut down signal to the server
# Note: server.jar catches SIGHUP signals and terminates gracefully
ssh $remoteUserName@$serverMachine "killall java"

echo -ne "  Waiting for the server to shut down... "
# Wait for the server to gracefully shut down
#while [ `ssh $remoteUserName@$serverMachine "cat ~/testRun/logs/perfRun/server.out | grep 'Server shutting down' | wc -l"` != 1 ]
#do
#	sleep 1
#done 
echo "OK"

########################################
#
# Copy and process logs and plot graphs
#
########################################

# Copy log files from the clients
if [ ! -d /mnt/asl/user26/results  ];
then
	mkdir /mnt/asl/user26/results
fi
mkdir -p /mnt/asl/user26/results/${experimentId} /mnt/asl/user26/results/${experimentId}/server /mnt/asl/user26/results/${experimentId}/client

echo "  Copying log files from client and server machine... "
scp -r $remoteUserName@$clientMachine:~/testRun/logs/perfRuns/${experimentId}/ /mnt/asl/user26/results/${experimentId}/client/
scp -r $remoteUserName@$serverMachine:~/testRun/logs/perfRuns/${experimetId}/ /mnt/asl/user26/results/${experimentId}/server/

# Cleanup
echo -ne "  Cleaning up files on client and server machines... "
ssh $remoteUserName@$clientMachine "rm -rf ~/testRun/"
ssh $remoteUserName@$serverMachine "rm -rf ~/testRun/"
echo "OK"
