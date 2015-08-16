
dataFile=commandArgs(T)[1]

out=commandArgs(T)[2]

data = read.csv2(dataFile,header=F,colClasses=c(NA,'NULL','NULL',NA,'NULL'),col.names=c("type","","","time",""))

t50 = round(quantile(data$time, 0.5, na.rm=T),digit=3)
t90 = round(quantile(data$time, 0.9, na.rm=T),digit=3)
t99 = round(quantile(data$time, 0.99, na.rm=T),digit=3)

dataPOST = data[data$type=="POST",]
p50 = round(quantile(dataPOST$time, 0.5, na.rm=T),digit=3)
p90 = round(quantile(dataPOST$time, 0.9, na.rm=T),digit=3)
p99 = round(quantile(dataPOST$time, 0.99, na.rm=T),digit=3)

dataRETRIEVE = data[data$type=="RETRIEVE_MESSAGE",]
m50 = round(quantile(dataRETRIEVE$time, 0.5, na.rm=T),digit=3)
m90 = round(quantile(dataRETRIEVE$time, 0.9, na.rm=T),digit=3)
m99 = round(quantile(dataRETRIEVE$time, 0.99, na.rm=T),digit=3)

cat(paste(t50,t90,t99,p50,p90,p99,m50,m90,m99,sep=";"),file=out,sep="\n",append=T)
print(paste("DB operation time (ms): Q50=",p50,", Q90=",p90,", Q99=",p99,sep=""))
print(paste("DB POST time (ms): Q50=",p50,", Q90=",p90,", Q99=",p99,sep=""))
print(paste("DB RETRIEVE time (ms): Q50=",m50,", Q90=",m90,", Q99=",m99,sep=""))







