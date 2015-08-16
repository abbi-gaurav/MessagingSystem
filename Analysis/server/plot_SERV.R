
expidStart=as.numeric(commandArgs(T)[1])
expidEnd=as.numeric(commandArgs(T)[2])
numRun=as.numeric(commandArgs(T)[3])

outFile = commandArgs(T)[4]
current = commandArgs(T)[5]

colNames=c("time50","time90","time99","read50","read90","read99","write50","write90","write99","proc50","proc90","proc99")

source(paste(current,"/../common/loadData.R",sep=""))

source(paste(current,"/../common/plotFunc.R",sep=""))


####################SERVER OP TIME############################

create.plot.with.percentile(paste(current,"/SERV_time",expidStart,"-",expidEnd,".png",sep=""),
							experiments,
							points_time50,upper_error_bars_time50,lower_error_bars_time50,
							points_time90,upper_error_bars_time90,lower_error_bars_time90,
							points_time99,upper_error_bars_time99,lower_error_bars_time99,
							"Time (ms)",paste("Experiments ids (n to n +",numRun,"test for the same parameters)"),
							"Time spent per operation in the Server")

#########################SERVER READ TIME######################

create.plot.with.percentile(paste(current,"/SERV_read_time",expidStart,"-",expidEnd,".png",sep=""),
							experiments,
							points_read50,upper_error_bars_read50,lower_error_bars_read50,
							points_read90,upper_error_bars_read90,lower_error_bars_read90,
							points_read99,upper_error_bars_read99,lower_error_bars_read99,
							"Time (ms)",paste("Experiments ids (n to n +",numRun,"test for the same parameters)"),
							"Time spent reading an incoming request at the server")

#########################SERVER WRITE TIME######################

create.plot.with.percentile(paste(current,"/SERV_write_time",expidStart,"-",expidEnd,".png",sep=""),
							experiments,
							points_write50,upper_error_bars_write50,lower_error_bars_write50,
							points_write90,upper_error_bars_write90,lower_error_bars_write90,
							points_write99,upper_error_bars_write99,lower_error_bars_write99,
							"Time (ms)",paste("Experiments ids (n to n +",numRun,"test for the same parameters)"),
							"Time spent writing the answer of a request at the server")

#########################SERVER PROCESSING TIME######################

create.plot.with.percentile(paste(current,"/SERV_proc_time",expidStart,"-",expidEnd,".png",sep=""),
							experiments,
							points_proc50,upper_error_bars_proc50,lower_error_bars_proc50,
							points_proc90,upper_error_bars_proc90,lower_error_bars_proc90,
							points_proc99,upper_error_bars_proc99,lower_error_bars_proc99,
							"Time (ms)",paste("Experiments ids (n to n +",numRun,"test for the same parameters)"),
							"Time spent processing a request at the server")










