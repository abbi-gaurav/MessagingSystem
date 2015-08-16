#!/bin/bash

if [ $# -lt 5 ]
then
        echo "Usage <serverDirectory> <clientDirectory> <experimentidStart> <experimentidEnd> <numRuns>"
        exit 1
fi

serverDirectory=$1
clientDirectory=$2
expidStart=$3
expidEnd=$4
numRun=$5

current=`dirname ${BASH_SOURCE[0]}` #directory containing script

"$current/removeNonPrintableChars.sh" "$serverDirectory" "txt"

#mkdir "$current/results/$expidStart-$expidEnd"

echo "Analysis of the DB data"
"$current/db/dbQueryTime.sh" "$serverDirectory" "$expidStart" "$expidEnd" "$numRun"
mv "$current/db/"*.png "$current/results/$expidStart-$expidEnd"
mv "$current/db/"*.out "$current/results/$expidStart-$expidEnd"

echo "Analysis of the SERVER data"
"$current/server/serverActionTime.sh" "$serverDirectory" "$expidStart" "$expidEnd" "$numRun"
mv "$current/server/"*.png "$current/results/$expidStart-$expidEnd"
mv "$current/server/"*.out "$current/results/$expidStart-$expidEnd"

echo "Analysis of the CLIENT data"
"$current/client/clientAnalysis.sh" "$clientDirectory" "$expidStart" "$expidEnd" "$numRun"
mv "$current/client/"*.png "$current/results/$expidStart-$expidEnd"
mv "$current/client/"*.out "$current/results/$expidStart-$expidEnd"




