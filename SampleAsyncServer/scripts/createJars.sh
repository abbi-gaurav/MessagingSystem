if [ $# -lt 2 ]
then
	echo "Usage <workspaceDir> <jarsDir>"
	exit 1
fi
echo "Creating jars in $2 directory from the workspace $1"
JARS_DIR="$2"
WS_DIR="$1"
PATH_SEP="/"
echo $JARS_DIR$PATH_SEP

jar cvf $JARS_DIR$PATH_SEP"client"$PATH_SEP"Client.jar" -C $WS_DIR$PATH_SEP"SampleAsyncClient/bin" .
jar cvf $JARS_DIR$PATH_SEP"client"$PATH_SEP"CmndLineTests.jar" -C $WS_DIR$PATH_SEP"CmndLineTests/bin" .

jar cvf $JARS_DIR$PATH_SEP"common"$PATH_SEP"Common.jar" -C $WS_DIR$PATH_SEP"Common/bin" .

jar cvf $JARS_DIR$PATH_SEP"console"$PATH_SEP"Console.jar" -C $WS_DIR$PATH_SEP"MgmtConsole/bin" .

jar cvf $JARS_DIR$PATH_SEP"server"$PATH_SEP"Middleware.jar" -C $WS_DIR$PATH_SEP"SampleAsyncServer/bin" .

