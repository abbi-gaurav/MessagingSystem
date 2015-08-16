
expidStart=as.numeric(commandArgs(T)[1])
expidEnd=as.numeric(commandArgs(T)[2])
numRun=as.numeric(commandArgs(T)[3])

outFile = commandArgs(T)[4]
current = commandArgs(T)[5]

colNames=c("thput","owthput","rrthput")

source(paste(current,"/../common/loadData.R",sep=""))

source(paste(current,"/../common/plotFunc.R",sep=""))

####################THROUGHPUT#######################

png(paste(current,"/throughput",expidStart,"-",expidEnd,".png",sep=""),width=1000,height=800)

minRange = min(min(lower_error_bars_rrthput),min(lower_error_bars_owthput))
maxRange = max(upper_error_bars_thput)

plot(y=points_thput,x=experiments,ylim=c(minRange,maxRange+0.5),ylab="Throughput (req/s)",xlab=paste("Experiments ids (n to n +",numRun,"test for the same parameters)"),type="line",col="darkgreen")
title("Throughput of the system")
add.error.bars(experiments,lower_error_bars_thput,upper_error_bars_thput,width=0.2,col="darkgreen")

lines(y=points_owthput,x=experiments,col="darkgoldenrod")
add.error.bars(experiments,lower_error_bars_owthput,upper_error_bars_owthput,width=0.2,col="darkgoldenrod")

lines(y=points_rrthput,x=experiments,col="darkorange")
add.error.bars(experiments,lower_error_bars_rrthput,upper_error_bars_rrthput,width=0.2,col="darkorange")

legend("top",c("Full Throughput","One Way","Request Response"),lty=c(1,1,1),lwd=c(2.5,2.5),col=c("darkgreen","darkgoldenrod","darkorange"))
#legend("top",c("Full Throughput"),lty=c(1),lwd=c(2.5,2.5),col=c("darkgreen"))

dev.off()

#########################RESPONSE TIME#################

outFile = commandArgs(T)[6]

colNames=c("rt50","rt90","rt99")

source(paste(current,"/../common/loadData.R",sep=""))


create.plot.with.percentile(paste(current,"/responseTime",expidStart,"-",expidEnd,".png",sep=""),
							experiments,
							points_rt50,upper_error_bars_rt50,lower_error_bars_rt50,
							points_rt90,upper_error_bars_rt90,lower_error_bars_rt90,
							points_rt99,upper_error_bars_rt99,lower_error_bars_rt99,
							"Time (ms)",paste("Experiments ids (n to n +",numRun,"test for the same parameters)"),
							"Response time for a client cycle (one post and one retrieve)")





