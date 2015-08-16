#!/bin/bash
if [ $# -lt 1 ]
then
	echo "./readVars.sh sourceFile"
	exit -1
fi

source $1
echo "serverMachine=$serverMachine clientMachine=$clientMachine numClients=$numClients userName=$userName startExperimentId=$startExperimentId runTimeInSecs=$runTimeInSecs ioTPSize=$ioTPSize processorTpSize=$processorTpSize dbConnPoolSize=$dbConnPoolSize dbMachine=$dbMachine dbPort=$dbPort msgLength=$msgLength newSetup=$newSetup numRuns=$numRuns isLast=$isLast resDirectory=$resDirectory middlewarePort=$middlewarePort serverMemory=$serverMemory recordsInMsgQueue=$recordsInMsgQueue setupDB=$setupDB"

