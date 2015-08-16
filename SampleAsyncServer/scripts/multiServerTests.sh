#!/bin/bash
MNT_DIR="/mnt/asl/user26/"
if [ $# -lt 1 ]
then
	echo "usage ./cmnds_db_server_client.sh configFile"
	exit -1	
fi

#Load all variables: TODO uncomment next line + previous lines become unecessary
source $1
echo "serverMachine=$serverMachine clientMachine1=$clientMachine1  userName=$userName startExperimentId=$startExperimentId runTimeInSecs=$runTimeInSecs ioTPSize=$ioTPSize processorTpSize=$processorTpSize dbConnPoolSize=$dbConnPoolSize dbMachine=$dbMachine dbPort=$dbPort msgLength=$msgLength newSetup=$newSetup numRuns=$numRuns isLast=$isLast resDirectory=$resDirectory baseMiddlewarePort=$baseMiddlewarePort serverMemory=$serverMemory recordsInMsgQueue=$recordsInMsgQueue setupDB=$setupDB numServers=$numServers"

if [ "$setupDB" = "true" ] && [ "$isLocal" = "false" ]; then
	#db machine:
	echo "doing db set up and start"
	ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${dbMachine} "cp ${MNT_DIR}testRun/scripts/buildInstallPostgresql.sh ~;cd ~;chmod u+x buildInstallPostgresql.sh;./buildInstallPostgresql.sh $dbPort;cp ${MNT_DIR}testRun/scripts/populate_db.sh ~;"
fi

#new client creations
if [[ ${newClients} = 0 ]];
then
	echo "this run will not create any new clients for total number of clients "  
else
	echo "this run will create ${newClients} new clients for total number of clients "  
	echo "creating queues and clients "
	if [ "$isLocal" = "true"  ]; then
		./populate_db.sh 'CLIENT' ${newClients} 5432 'rw';
		./populate_db.sh 'QUEUE' ${newClients} 5432; 

	else
		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${dbMachine} "cd; ~/populate_db.sh 'CLIENT' ${newClients} $dbPort 'rw';"
		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${dbMachine} "cd; ~/populate_db.sh 'QUEUE' ${newClients} $dbPort;" 
	fi
fi

#starts initial setup
if [ "$newSetup" = "true" ]; then
	mkdir ${MNT_DIR}results/${resDirectory}/
	ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "killall java;"
	for (( serverCount=1; serverCount<=${numServers}; serverCount++  ))
	do
		#server machine:
		#set up
		middlewarePort=`expr $baseMiddlewarePort + $serverCount - 1` 
		echo "doing server setup and start"
		if [ "$isLocal" =  "true" ]; then
			echo "no set up here"
		else
			ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "[[ ! -d ~/testRun/ ]] && cp -r /mnt/asl/${userName}/testRun/ ~/; [[ ! -d ~/jdk/ ]] && cp -r /mnt/asl/${userName}/jdk/ ~/;"
		fi
		#starting server
		echo "starting the server on port ${middlewarePort}"
		echo "${userName} ${serverMachine} ${middlewarePort} ${ioTPSize} ${processorTPSize} ${dbConnPoolSize} ${dbMachine} ${dbPort} ${startExperimentId} ${serverMemory}"
		
		if [ "$isLocal" = "true" ]; then
			[[ ! -d ~/testRun/logs/perfRuns/${startExperimentId}/ ]] && mkdir ~/testRun/logs/perfRuns/${startExperimentId}/;java -server -Xmx${serverMemory}m -Xms${serverMemory}m -cp ../jars/server/Middleware.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${HOME}/testRun/logs/properties/server/logging.properties com.server.impl.AsyncIOServer $middlewarePort $ioTPSize $processorTPSize $dbConnPoolSize localhost 5432 gabbi 2>&1 > ~/testRun/logs/perfRuns/${startExperimentId}/server_${middlewarePort}_${startExperimentId}.out &
		else
			ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "[[ ! -d ~/testRun/logs/perfRuns/${startExperimentId}/ ]] && mkdir ~/testRun/logs/perfRuns/${startExperimentId}/;cd ~/testRun/scripts;/home/user26/jdk/jdk1.7.0_45/bin/java -server -Xmx${serverMemory}m -Xms${serverMemory}m -cp ../jars/server/Middleware.jar:../jars/common/Common.jar:../jars/jdbc/postgresql-9.2-1003.jdbc4.jar -Djava.util.logging.config.file=${HOME}/testRun/logs/properties/server/logging.properties com.server.impl.AsyncIOServer $middlewarePort $ioTPSize $processorTPSize $dbConnPoolSize $dbMachine $dbPort $userName 2>&1 > ~/testRun/logs/perfRuns/${startExperimentId}/server_${middlewarePort}_${startExperimentId}.out;" &
		fi	
		echo -ne "  Waiting for the server to start up..."
		sleep 1
		if [ "$isLocal" = "true"  ]; then
			while [ `cat ~/testRun/logs/perfRuns/${startExperimentId}/server_${middlewarePort}_${startExperimentId}.out | grep 'Middleware started' | wc -l` != 1 ]
			do
				sleep 1
			done
		else
			while [ `ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "cat ~/testRun/logs/perfRuns/${startExperimentId}/server_${middlewarePort}_${startExperimentId}.out | grep 'Middleware started' | wc -l"` != 1 ]
			do
				sleep 1
			done 
			echo "OK"
		fi
	done
	
	#client machine:
	#client set up start
	if [ "$isLocal" = "true"  ]; then
		echo "no set up here" 	
	else
		echo "doing client setup on client machine ${clientMachine1}"
		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine1} "cp -r ${MNT_DIR}testRun/ ~/; [[ ! -d ~/jdk/ ]] && cp -r /mnt/asl/${userName}/jdk/ ~/;"
		echo "doing client setup on client machine ${clientMachine2}"
		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine2} "cp -r ${MNT_DIR}testRun/ ~/; [[ ! -d ~/jdk/ ]] && cp -r /mnt/asl/${userName}/jdk/ ~/;"
	fi
	#client set up start
fi
# ends initial set up

for (( count=1; count<=${numRuns}; count++  ))
do
	clientMachine1Pids=""
	clientMachine2Pids=""
	half=2
	numRRClientsPerMachine=`expr $RRClients / $half`
	numOWClientsPerMachine=`expr $OneWayClients / $half`

	experimentId=`expr $startExperimentId + $count - 1` 

	#starting client
	for (( serverCount=1; serverCount<=${numServers}; serverCount++  ))
	do
		middlewarePort=`expr $baseMiddlewarePort + $serverCount - 1` 

		if [ "$isLocal" = "true" ]; then
			echo "Running client locally with experiment id ${experimentId} from dir ${PWD}"
			./client_request_reply.sh ${userName} "localhost" "localhost" ${middlewarePort} ${msgLength} ${runTimeInSecs} ${experimentId} ${numRRClientsPerMachine} ${numOWClientsPerMachine};
		else
			echo "Running client with experiment id ${experimentId}"
			ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine1} "cd ~/testRun/scripts;./client_request_reply.sh ${userName} ${clientMachine1} ${serverMachine} ${middlewarePort} ${msgLength} ${runTimeInSecs} ${experimentId} ${numRRClientsPerMachine} ${numOWClientsPerMachine};"

			ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine2} "cd ~/testRun/scripts;./client_request_reply.sh ${userName} ${clientMachine2} ${serverMachine} ${middlewarePort} ${msgLength} ${runTimeInSecs} ${experimentId} ${numRRClientsPerMachine} ${numOWClientsPerMachine};"
		fi
	done

	# Wait for the clients to finish
	if [ "$isLocal" = "true"  ]; then
		sleep 5
		echo -ne "  Waiting for the clients to finish ... "
		while [ `grep -l "Approx test run time in seconds:" ~/testRun/logs/perfRuns/${experimentId}/*|wc -l` -lt ${numOWClientsPerMachine} ]
		do
			sleep 1
		done
		echo "OK" 	
	else
		echo -ne "  Waiting for the clients to finish ... "
		while [ `ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine1} "grep -l 'Approx test run time in seconds:' ~/testRun/logs/perfRuns/${experimentId}/*|wc -l"` -lt ${numOWClientsPerMachine} ]
	
		do
			sleep 1
		done
	
		while [ `ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine2} "grep -l 'Approx test run time in seconds:' ~/testRun/logs/perfRuns/${experimentId}/*|wc -l"` -lt ${numOWClientsPerMachine} ]
		do
			sleep 1
		done
		echo "OK"
	fi
	#after tests
	#on client

	#mark the end of this run in the serverOut and perfDb file
	for (( serverCount=1; serverCount<=${numServers}; serverCount++  ))
	do
		middlewarePort=`expr $baseMiddlewarePort + $serverCount - 1` 
		if [ "$isLocal" = "true" ]; then
			serverOut=`find ~/testRun/logs -name server_${middlewarePort}_${startExperimentId}.out|sort|tail -1`
			perfDB=`find ~/testRun/logs -name perfDB*.txt|sort|tail -1`
			echo $serverOut
			echo $perfDB

			endMsg="######################################finished--experimentId--${startExperimentId}######################################"
			echo ${endMsg} >> ${serverOut};echo ${endMsg} >> ${perfDB};[[ ! -d ~/testRun/logs/perfRuns/${experimentId} ]] && mkdir ~/testRun/logs/perfRuns/${experimentId};cat ${serverOut} >> ~/testRun/logs/perfRuns/${experimentId}/serverTraces.txt;echo '  ' > ${serverOut};cat ${perfDB} >> ~/testRun/logs/perfRuns/${experimentId}/perfTraces.txt; echo '  ' > ${perfDB}
			echo "marked end of this run in the serve trace files" 	
		else
			echo "writing db and server traces to separate files"
			serverOut=`ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "find ~/testRun/logs -name server_${middlewarePort}_${startExperimentId}.out"`
			echo $serverOut
			echo $startExperimentId
			endMsg="######################################finished--experimentId--${startExperimentId}######################################"
			
			ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "[[ ! -d /home/user26/testRun/logs/perfRuns/$startExperimentId ]] && mkdir /home/user26/testRun/logs/perfRuns/$startExperimentId;cat $serverOut >> /home/user26/testRun/logs/perfRuns/$startExperimentId/serverTraces${middlewarePort}_${count}.txt;echo '  ' > $serverOut;ls -l /home/user26/testRun/logs/perfRuns/$startExperimentId/serverTraces${middlewarePort}_${count}.txt"
			echo "marked end of this run in the serve trace files"
			
			
		fi
	done
	
	perfDB=`ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "find ~/testRun/logs -name perfDB*.txt;"`
	echo $perfDB
	for perfDBFile in $perfDB 
	do
		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "[[ ! -d /home/user26/testRun/logs/perfRuns/$startExperimentId ]] && mkdir /home/user26/testRun/logs/perfRuns/$startExperimentId;cat $perfDBFile >> ${perfDBFile}_trc_$count;mv ${perfDBFile}_trc_$count /home/user26/testRun/logs/perfRuns/${startExperimentId}/ ;echo '  ' > $perfDBFile;"
		echo "marked end of this run in the perf db trace files"
	done

done

if [ "$isLast" = "true" ]; then
	if [ "$isLocal" = "false" ];then
		end=`expr $startExperimentId + $numRuns - 1`
		echo "collecting all the logs from client "
		if [ ! -d ${MNT_DIR}results/${resDirectory} ];
		then
			mkdir ${MNT_DIR}results/${resDirectory}
		fi

		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine1} "env GZIP=-9 tar -cvzf ${MNT_DIR}results/${resDirectory}/client1_${resDirectory}_${end}.tar.gz ~/testRun/logs/;"
		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine2} "env GZIP=-9 tar -cvzf ${MNT_DIR}results/${resDirectory}/client2_${resDirectory}_${end}.tar.gz ~/testRun/logs/;"
		#on server
		echo "collecting all the logs from server "
		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "env GZIP=-9 tar -cvzf ${MNT_DIR}results/${resDirectory}/server_${resDirectory}_${end}.tar.gz ~/testRun/logs/;"
		
		#remove files
		echo "clean up "
		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine1} "rm -rf ~/testRun;"
		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${clientMachine2} "rm -rf ~/testRun;"
		
		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "killall java;"
		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${serverMachine} "rm -rf ~/testRun;"
			dbPid=`ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${dbMachine} "head -1 ~/postgres/data/postmaster.pid;"`
		ssh -i ${MNT_DIR}configFiles/id_rsa -oStrictHostKeyChecking=no ${userName}@${dbMachine} "screen -S dbServer -X quit;kill -INT $dbPid"
	fi
fi

echo "done here"
