
dataFile=commandArgs(T)[1]

temp=commandArgs(T)[2]

data = read.csv2(dataFile,header=F,colClasses=c(NA,'NULL','NULL',NA),col.names=c("type","","","time"),skip=3,nrow=length(readLines(dataFile))-8)

data[data$type=="ONE_WAY",2] = data[data$type=="ONE_WAY",2]-100

cat(data$time,file=temp,sep="\n",append=T)
