#!/bin/bash

if [ $# -lt 3 ]
then
	echo "Usage <type> <N> <port>"
	exit 1
fi
type_populate="$1"

if [ "$type_populate" = "QUEUE" ]; then
	echo "creating queues"
	~/postgres/bin/psql -p $3 -U $USER postgres -v n_queues=$2 -f /mnt/asl/user26/testRun/scripts/populate_queues.sql 
elif [ "$type_populate" = "CLIENT" ]; then
	if [ $# -lt 3 ]; then
		echo "Usage <type> <N> <port> <role>"
		exit 1
	fi
	echo "creating clients"
	~/postgres/bin/psql -p $3 -U $USER postgres -v n_clients=$2 -v role="'"$4"'" -f /mnt/asl/user26/testRun/scripts/populate_clients.sql
elif [ "$type_populate" = "MESSAGE" ]; then
	if [ $# -lt 8 ]; then
		echo "Usage <type> <N> <port> <from_id> <to_id> <context> <priority> <body> <queue>"
		exit 1
	fi
	~/postgres/bin/psql -p $3 -U $USER postgres -v n_messages=$2 -v mess="row($4,$5,$6,$7,'$8')" -v queues="'{"$9"}'" -f /mnt/asl/user26/testRun/scripts/populate_messages.sql
else
	echo "Wrong type must be QUEUE or CLIENT or MESSAGE"
fi


#~/postgres/bin/psql -U $USER postgres -v n_queues=100 -v n_clients=100 -v role='rw' -v n_messages=100 -v mess="row(2019,2019,3,9,'testBody')" -v queues="'{10011}'" -f ~/testRun/scripts/pop_db.sql

