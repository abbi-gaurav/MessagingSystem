#!/bin/bash

if [ $# -lt 2 ]
then
        echo "Usage <dataDirectory> <file extension>"
		exit 1
fi


dataDirectory=$1
fileExtension=$2


path=`dirname ${BASH_SOURCE[0]}` #directory containing script

for dir in `ls "$dataDirectory/"`
do
	i="$path/$dataDirectory/$dir"
	for file in `ls "$i/"*.$fileExtension 2> /dev/null`
	do
		tr -cd '\11\12\15\40-\176' < "$file" > "$i/tmp"
		mv "$i/tmp" "$file"
		echo "Cleaned up $file"
	done
done
echo "Done"

