#!/bin/bash
function usage() {
        echo "Usage: serverMachine<address> clientMachine<address> noOfClients<int> userName<username> startExperimentId<id> runTimeInSecs<seconds> ioTPSieze<int> processorTpSize<int> dbConnPoolSize<int> dbMachine<address> dbPort<int> msgLength<string> resDirectory" 
	echo "valid requests are:\n"
	echo "[NEW_CLIENT, NEW_QUEUE, DELETE_QUEUE, LIST_QUEUE, POST, BROADCAST, READ, LIST_QUEUE_WITH_MESSAGE, CHECK_MESSAGE_FROM, RETRIEVE_MESSAGE]\n"
	echo "valid message length values are"
	echo "[HundredCharacters, TwoHundredChracters, FiveHundredCharcaters, TwoKCharcaters, OneKCharacters, FifteenHundredCharcaters]\n"

        exit -1
}

if [ $# -lt 1 ]
then
	echo "usage ./cmnds_db_server_client.sh configFile"
	exit -1	
fi

#Load all variables: TODO uncomment next line + previous lines become unecessary
source $1
echo "serverMachine=$serverMachine clientMachine=$clientMachine RRClients=$RRClients OWClients=$OWClients userName=$userName startExperimentId=$startExperimentId runTimeInSecs=$runTimeInSecs ioTPSize=$ioTPSize processorTPSize=$processorTPSize dbConnPoolSize=$dbConnPoolSize dbMachine=$dbMachine dbPort=$dbPort msgLength=$msgLength newSetup=$newSetup numRuns=$numRuns isLast=$isLast resDirectory=$resDirectory middlewarePort=$middlewarePort serverMemory=$serverMemory setupDB=$setupDB addDBqueues=$addDBqueues addDBclients=$addDBclients addDBmessages=$addDBmessages"

############################################DB SETUP#####################################################
if [ "$setupDB" = "true" ]; then
	#db machine:
	echo "doing db set up and start"
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${dbMachine} "cp /mnt/asl/${userName}/testRun/scripts/buildInstallPostgresql.sh ~;cd ~;chmod u+x buildInstallPostgresql.sh;./buildInstallPostgresql.sh $dbPort;cp /mnt/asl/${userName}/testRun/scripts/populate_db.sh ~;"
fi

if [ $addDBqueues -ne 0 ]; then
	echo "adding queues ${addDBqueues}"
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${dbMachine} "cd; ~/populate_db.sh 'QUEUE' ${addDBqueues} $dbPort;" 
fi

if [ $addDBclients -ne 0 ]; then
	echo "adding clients ${addDBclients}"
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${dbMachine} "cd; ~/populate_db.sh 'CLIENT' ${addDBclients} $dbPort 'rw';"
fi

if [ $addDBmessages -ne 0 ]; then
	echo "adding messages ${addDBmessages}"
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${dbMachine} "cd; ~/populate_db.sh 'MESSAGE' ${addDBmessages} $dbPort 1 1 -1 1 'DUMMY_MESSAGE' 1;"
fi

############################################SERVER AND CLIENTS SETUP#####################################################
#starts initial setup
if [ "$newSetup" = "true" ]; then
	#server machine:
	#set up
	echo "doing server setup and start"
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "killall java;[[ ! -d /mnt/asl/${userName}/results/${resDirectory}/ ]] && mkdir /mnt/asl/${userName}/results/${resDirectory}/;cp -r /mnt/asl/${userName}/testRun/ ~/; [[ ! -d ~/jdk/ ]] && cp -r /mnt/asl/${userName}/jdk/ ~/;"

	#starting server
	echo "starting the server"
	echo "${userName} ${serverMachine} ${middlewarePort} ${ioTPSize} ${processorTPSize} ${dbConnPoolSize} ${dbMachine} ${dbPort} ${startExperimentId} ${serverMemory}"
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "[[ ! -d ~/testRun/logs/perfRuns/${startExperimentId}/ ]] && mkdir ~/testRun/logs/perfRuns/${startExperimentId}/;cd ~/testRun/scripts;/home/user26/jdk/jdk1.7.0_45/bin/java -server -Xmx${serverMemory}m -Xms${serverMemory}m -cp ../jars/server/Middleware.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${HOME}/testRun/logs/properties/server/logging.properties com.server.impl.AsyncIOServer $middlewarePort $ioTPSize $processorTPSize $dbConnPoolSize $dbMachine $dbPort $userName 2>&1 > ~/testRun/logs/perfRuns/${startExperimentId}/server.out;" &
	
	echo -ne "  Waiting for the server to start up..."
	sleep 1
        while [ `ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "cat ~/testRun/logs/perfRuns/${startExperimentId}/server.out | grep 'Middleware started' | wc -l"` != 1 ]
	do
        	sleep 1
	done 
	echo "OK"
	
	#client machine:
	#client set up
	echo "doing client setup"
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine} "cp -r /mnt/asl/${userName}/testRun/ ~/; [[ ! -d ~/jdk/ ]] && cp -r /mnt/asl/${userName}/jdk/ ~/;"
fi
# ends initial set up


############################################START CLIENTS#####################################################

for (( count=1; count<=${numRuns}; count++  ))
do
	experimentId=`expr $startExperimentId + $count - 1` 
	#starting client
	echo "Running client with experiment id ${experimentId}"
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine} "cd ~/testRun/scripts;./client_request_reply.sh ${userName} ${clientMachine} ${serverMachine} ${middlewarePort} ${msgLength} ${runTimeInSecs} ${experimentId} ${RRClients} ${OWClients};"
	#after tests
	#on client

	#mark the end of this run in the serverOut and perfDb file
	serverOut=`ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "find ~/testRun/logs -name server.out|sort|tail -1"`
	perfDB=`ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "find ~/testRun/logs -name perfDB*.txt|sort|tail -1"`
	echo $serverOut
	echo $perfDB

	endMsg="Finished--experimentId--${experimentId}"
	# the following command empties a file:  > filename
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "echo ${endMsg};[[ ! -d ~/testRun/logs/perfRuns/${experimentId} ]] && mkdir ~/testRun/logs/perfRuns/${experimentId};cat ${serverOut} >> ~/testRun/logs/perfRuns/${experimentId}/serverTraces.txt; > ${serverOut};cat ${perfDB} >> ~/testRun/logs/perfRuns/${experimentId}/perfTraces.txt; > ${perfDB}"
	echo "marked end of this run in the serve trace files"

done

############################################ZIP RESULTS AND CLEAN UP#####################################################
if [ "$isLast" = "true"  ]; then
	end=`expr $startExperimentId + $numRuns - 1`
	echo "collecting all the logs from client "
	
	if [[ ! -d /mnt/asl/${userName}/results/${resDirectory}/ ]]; then
		mkdir /mnt/asl/${userName}/results/${resDirectory}/;
	fi	
	
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine} "env GZIP=-9 tar -cvzf /mnt/asl/${userName}/results/${resDirectory}/client_${resDirectory}_${end}.tar.gz ~/testRun/logs/;"
	#on server
	echo "collecting all the logs from server "
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "env GZIP=-9 tar -cvzf /mnt/asl/${userName}/results/${resDirectory}/server_${resDirectory}_${end}.tar.gz ~/testRun/logs/;"
	
	#remove files
	echo "clean up "
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine} "rm -rf ~/testRun;"
	
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "killall java;"
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "rm -rf ~/testRun;"
	dbPid=`ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${dbMachine} "head -1 ~/postgres/data/postmaster.pid;"`
	ssh -i /mnt/asl/user26/configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${dbMachine} "screen -S dbServer -X quit;kill -INT $dbPid"
fi
