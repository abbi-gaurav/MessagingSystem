

data = read.csv2(outFile,header=F,col.names=colNames,stringsAsFactors=F)

n = 1:nrow(data)

for(i in colNames){
  #next line is changing the data to a type we can work with
  perExp = split(as.numeric(unlist(data[i],use.name=F)),ceiling(n/numRun))
  assign(paste('points_', i, sep=''), unlist(lapply(perExp,mean),use.name=F))
  std_deviation = unlist(lapply(perExp,sd),use.name=F)
  assign(paste('lower_error_bars_', i, sep=''), eval(as.symbol(paste('points_', i, sep='')))-std_deviation)
  assign(paste('upper_error_bars_', i, sep=''), eval(as.symbol(paste('points_', i, sep='')))+std_deviation)
  #assign(paste('lower_error_bars_', i, sep=''), unlist(lapply(perExp,min),use.name=F))
  #assign(paste('upper_error_bars_', i, sep=''), unlist(lapply(perExp,max),use.name=F))
}

experiments=seq(expidStart,expidStart+(length(perExp)-1)*numRun,by=numRun)


