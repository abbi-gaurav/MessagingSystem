#!/bin/bash

if [ $# -lt 4 ]
then
        echo "Usage <clientDirectory> <experimentidStart> <experimentidEnd> <numRuns>"
        exit 1
fi

function record {
	nbRequest=`tail "$1" | grep -m 1 "Total number of requests server" | grep -o "[0-9]\+$"`
	runTime=`tail "$1" | grep -m 1 "Approx test run time in seconds" | grep -o "[0-9]\+$"`
	echo "$2;$nbRequest;$runTime" >> "$3"
	Rscript "$current/aggregate_CLIENT.R" "$1" "$4"
	echo -n "."
}

clientDirectory=$1
expidStart=$2
expidEnd=$3
numRun=$4


current=`dirname ${BASH_SOURCE[0]}` #directory containing script
outThput="$current/thput.out"
tempThput="$current/thput.tmp"
outRt="$current/rt.out"
tempRt="$current/rt.tmp"
touch "$outThput" "$tempThput" "$outRt" "$tempRt"
> "$outThput"
> "$tempThput"
> "$outRt"
> "$tempRt"

for (( count=$expidStart; count<=$expidEnd; count++  ))
do
	i="$clientDirectory/$count"
	echo -e "\nAnalysing $i"
	echo -n "POST clients analysis: "
	for f in `ls $i/*POST*.txt 2> /dev/null`
	do
        record "$f" "RR" "$tempThput" "$tempRt"
	done
	echo -ne " Done\nRETRIEVE clients analysis: "
	for f in `ls $i/*RET*.txt 2> /dev/null`
	do
      	record "$f" "RR" "$tempThput" "$tempRt"
	done
	echo -ne " Done\nONE_WAY clients analysis: "
	for f in `ls $i/*ONE*.txt 2> /dev/null`
	do
        record "$f" "OW" "$tempThput" "$tempRt"
	done
	echo " Done"
	Rscript "$current/calculate_CLIENT.R" "$tempThput" "$outThput" "$tempRt" "$outRt"
	> "$tempThput"
	> "$tempRt"
done
rm "$tempThput" "$tempRt" 

echo "ploting data"
Rscript "$current/plot_CLIENT.R" $expidStart $expidEnd $numRun "$outThput" "$current" "$outRt"

echo "Analysis of the DB performances is finished"



