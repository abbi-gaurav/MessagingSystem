
dataFile=commandArgs(T)[1]

out=commandArgs(T)[2]

data = read.csv2(dataFile,header=F,colClasses=c('NULL','NULL','NULL',NA,'NULL',NA),col.names=c("","","","type","","time"))

t50 = round(quantile(data$time, 0.5, na.rm=T),digit=3)
t90 = round(quantile(data$time, 0.9, na.rm=T),digit=3)
t99 = round(quantile(data$time, 0.99, na.rm=T),digit=3)

dataREAD = data[data$type=="Read",]
r50 = round(quantile(dataREAD$time, 0.5, na.rm=T),digit=3)
r90 = round(quantile(dataREAD$time, 0.9, na.rm=T),digit=3)
r99 = round(quantile(dataREAD$time, 0.99, na.rm=T),digit=3)

dataWRITE = data[data$type=="Write",]
w50 = round(quantile(dataWRITE$time, 0.5, na.rm=T),digit=3)
w90 = round(quantile(dataWRITE$time, 0.9, na.rm=T),digit=3)
w99 = round(quantile(dataWRITE$time, 0.99, na.rm=T),digit=3)

dataPROC = data[data$type=="Processing",]
p50 = round(quantile(dataPROC$time, 0.5, na.rm=T),digit=3)
p90 = round(quantile(dataPROC$time, 0.9, na.rm=T),digit=3)
p99 = round(quantile(dataPROC$time, 0.99, na.rm=T),digit=3)

cat(paste(t50,t90,t99,r50,r90,r99,w50,w90,w99,p50,p90,p99,sep=";"),file=out,sep="\n",append=T)
print(paste("Server operation time (ms): Q50=",t50,", Q90=",t90,", Q99=",t99,sep=""))
print(paste("Server READ time (ms): Q50=",r50,", Q90=",r90,", Q99=",r99,sep=""))
print(paste("Server WRITE time (ms): Q50=",w50,", Q90=",w90,", Q99=",w99,sep=""))
print(paste("Server PROC time (ms): Q50=",p50,", Q90=",p90,", Q99=",p99,sep=""))









