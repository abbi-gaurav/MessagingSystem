#!/bin/bash


#clean potential old install
rm -rf ~/postgres

#go to postgres source directory and install
cd /mnt/asl/user26/postgres/postgresql-9.3.0
echo -e "\nCONFIGURING POSTGRES INSTALL\n"
./configure --prefix=$HOME/postgres/
echo -e "\nDONE\nBUILDING POSTGRES\n"
make
echo -e "\nDONE\nINSTALLING POSTGRES\n"
make install
echo -e "\nDONE\n"

cd $HOME
#create postgresdb
echo -e "\nCREATING POSTGRES DB\n"
~/postgres/bin/initdb -D ~/postgres/data/
#copy the 2 modified conf files in postgres/data
echo -e "\nDONE\nCOPYING CONF FILES\n"
cp /mnt/asl/user26/configFiles/*.conf ~/postgres/data/
#start server on separate screen
echo -e "\nDONE\nSTARTING DB SERVER\n"
screen -S dbServer -d -m ./postgres/bin/postgres -D ./postgres/data
sleep 3	#otherwise goes to next steps and try to connect before server is started
echo -e "\nDONE\n"

#import db scripts
echo -e "\nCREATING PROJECT DB\n"
~/postgres/bin/psql -p $1 -U $USER postgres -f /mnt/asl/user26/testRun/scripts/init_db.sql
~/postgres/bin/psql -p $1 -U $USER postgres -f /mnt/asl/user26/testRun/scripts/load_stored_procedures.sql
echo -e "\nDONE\n"


