for f in `ls 13NovConfig/`
do
	echo "Running tests with configuration $f"
	./readVars.sh 13NovConfig/$f
	./multiServerTests.sh 13NovConfig/$f
done
