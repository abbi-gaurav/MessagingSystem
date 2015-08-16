#!/bin/bash

if [ $# -lt 3 ]
then
        echo "Usage <experimentidStart> <experimentidEnd> <numRuns>"
        exit 1
fi

expidStart=$1
expidEnd=$2
numRun=$3

cp "Analysis/results/$expidStart-$expidEnd/"*.out "Analysis/client/"

Rscript "Analysis/client/plot_CLIENT.R" $expidStart $expidEnd $numRun "Analysis/client/thput.out" "Analysis/client/" "Analysis/client/rt.out"

mv "Analysis/client/"*.png "Analysis/results/$expidStart-$expidEnd/"
rm "Analysis/client/"*.out
