#!/bin/bash

if [ $# -lt 4 ]
then
        echo "Usage <serverDirectory> <experimentidStart> <experimentidEnd> <numRuns>"
		exit 1
fi


serverDirectory=$1
expidStart=$2
expidEnd=$3
numRun=$4


current=`dirname ${BASH_SOURCE[0]}` #directory containing script
out="$current/db.out"
tmp="$current/db.tmp"
touch "$out" "$tmp"
> "$out"
> "$tmp"

for (( count=$expidStart; count<=$expidEnd; count++  ))
do
	i="$serverDirectory/$count"
	echo -e "\nAnalysing $i"
	for f in `ls $i/perfDB* 2> /dev/null`
	do
       tail --lines=+5 $f >> "$tmp"
	done
	Rscript "$current/calculate_DB.R" "$tmp" "$out"
	> "$tmp"
done
rm "$tmp"

echo "plotting data"
Rscript "$current/plot_DB.R" $expidStart $expidEnd $numRun "$out" "$current"

echo "Analysis of the DB performances is finished"



