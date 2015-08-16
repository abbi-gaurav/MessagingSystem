remoteUserName=$1
serverMachine=$2
clientMachine=$3
dbMachine=$4

echo -ne "  Testing passwordless connection to the server machine and client machine... "
# Check if command can be run on server and client
success=$( ssh -o BatchMode=yes  $remoteUserName@$serverMachine echo ok 2>&1 )
if [ $success != "ok" ]
then
	echo "Passwordless login not successful for $remoteUserName on $serverMachine. Exiting..."
	exit -1
fi

success=$( ssh -o BatchMode=yes  $remoteUserName@$clientMachine echo ok 2>&1 )
if [ $success != "ok" ]
then
	echo "Passwordless login not successful for $remoteUserName on $clientMachine. Exiting..."
	exit -1
fi
echo "OK"

success=$( ssh -o BatchMode=yes  $remoteUserName@$dbMachine echo ok 2>&1 )
if [ $success != "ok" ]
then
	echo "Passwordless login not successful for $remoteUserName on $dbMachine. Exiting..."
	exit -1
fi
echo "OK"
