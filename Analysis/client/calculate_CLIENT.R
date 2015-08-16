
dataFile=commandArgs(T)[1]

out=commandArgs(T)[2]

data = read.csv2(dataFile,header=F,col.names=c("type","nquery","time"))

measuredThput = round(sum(data$nquery,na.rm=T)/mean(data$time,na.rm=T),digit=3)

dataOW = data[data$type=="OW",]
measuredOWThput = round(sum(dataOW$nquery,na.rm=T)/mean(dataOW$time,na.rm=T),digit=3)

dataRR = data[data$type=="RR",]
measuredRRThput = round(sum(dataRR$nquery,na.rm=T)/mean(dataRR$time,na.rm=T),digit=3)

cat(paste(measuredThput,measuredOWThput,measuredRRThput,sep=";"),file=out,sep="\n",append=T)

print(paste("Throughput (req/s): ",measuredThput,sep=""))
print(paste("Throughput (req/s)  OW: ",measuredOWThput,sep=""))
print(paste("Throughput (req/s)  RR: ",measuredRRThput,sep=""))


dataFile=commandArgs(T)[3]
out=commandArgs(T)[4]
data = read.csv2(dataFile,header=F,col.names=c("time"))

rt50 = round(quantile(data$time, 0.5, na.rm=T),digit=3)
rt90 = round(quantile(data$time, 0.9, na.rm=T),digit=3)
rt99 = round(quantile(data$time, 0.99, na.rm=T),digit=3)

cat(paste(rt50,rt90,rt99,sep=";"),file=out,sep="\n",append=T)
print(paste("Response time (ms): Q50=",rt50,", Q90=",rt90,", Q99=",rt99,sep=""))



