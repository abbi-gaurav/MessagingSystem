
expidStart=as.numeric(commandArgs(T)[1])
expidEnd=as.numeric(commandArgs(T)[2])
numRun=as.numeric(commandArgs(T)[3])

outFile = commandArgs(T)[4]
current = commandArgs(T)[5]

colNames=c("time50","time90","time99","post50","post90","post99","retrieve50","retrieve90","retrieve99")

source(paste(current,"/../common/loadData.R",sep=""))

source(paste(current,"/../common/plotFunc.R",sep=""))


#########################DB OP TIME######################

create.plot.with.percentile(paste(current,"/DB_time",expidStart,"-",expidEnd,".png",sep=""),
							experiments,
							points_time50,upper_error_bars_time50,lower_error_bars_time50,
							points_time90,upper_error_bars_time90,lower_error_bars_time90,
							points_time99,upper_error_bars_time99,lower_error_bars_time99,
							"Time (ms)",paste("Experiments ids (n to n +",numRun,"test for the same parameters)"),
							"Time spent per operation in the DB")

#########################DB POST TIME######################

create.plot.with.percentile(paste(current,"/DB_post_time",expidStart,"-",expidEnd,".png",sep=""),
							experiments,
							points_post50,upper_error_bars_post50,lower_error_bars_post50,
							points_post90,upper_error_bars_post90,lower_error_bars_post90,
							points_post99,upper_error_bars_post99,lower_error_bars_post99,
							"Time (ms)",paste("Experiments ids (n to n +",numRun,"test for the same parameters)"),
							"Time spent posting a message to the DB")

#########################DB RETRIEVE TIME######################

create.plot.with.percentile(paste(current,"/DB_retrieve_time",expidStart,"-",expidEnd,".png",sep=""),
							experiments,
							points_retrieve50,upper_error_bars_retrieve50,lower_error_bars_retrieve50,
							points_retrieve90,upper_error_bars_retrieve90,lower_error_bars_retrieve90,
							points_retrieve99,upper_error_bars_retrieve99,lower_error_bars_retrieve99,
							"Time (ms)",paste("Experiments ids (n to n +",numRun,"test for the same parameters)"),
							"Time spent retrieving a message from the DB")










