if [ $# -lt 4 ]
then
        echo "Usage: ./jarsSetup.sh workspaceDir jarsOpDir userName mntMachine"
	exit -1
fi
workspaceDir=$1
jarsOpDir=$2
userName=$3
mntMachine=$4

./createJars.sh ${workspaceDir} ${jarsOpDir};
ssh ${userName}@${mntMachine} "rm -rf /mnt/asl/${userName}/testRun/jars/";
scp -r ${jarsOpDir}/ ${userName}@${mntMachine}:/mnt/asl/${userName}/testRun
