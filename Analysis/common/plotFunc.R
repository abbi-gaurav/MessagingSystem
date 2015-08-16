



add.error.bars <- function(X,upper,lower,width,col=par( )$fg,lwd=1){
  segments(X,lower,X,upper,col=col,lwd=lwd,lend=1);
  segments(X-width/2,lower,X+width/2,lower,col=col,lwd=lwd,lend=1);
  segments(X-width/2,upper,X+width/2,upper,col=col,lwd=lwd,lend=1);
}


create.plot.with.percentile <- function(AbsolutePathToPlot,experiments,Q50,maxQ50,minQ50,Q90,maxQ90,minQ90,Q99,maxQ99,minQ99,ylabel,xlabel,plotTitle){
png(AbsolutePathToPlot,width=1000,height=800)

points = Q50
minPoints = minQ50
maxPoints = maxQ50
minRange = min(minQ50)
maxRange = max(maxQ99) #we want the max possible value => percentile 99

plot(y=points,x=experiments,ylim=c(minRange,maxRange+0.5),ylab=ylabel,xlab=xlabel,type="line",col="red")
title(plotTitle)
add.error.bars(experiments,minPoints,maxPoints,width=0.2,col="red")

points = Q90
minPoints = minQ90
maxPoints = maxQ90

lines(y=points,x=experiments,col="blue")
add.error.bars(experiments,minPoints,maxPoints,width=0.2,col="blue")

points = Q99
minPoints = minQ99
maxPoints = maxQ99

lines(y=points,x=experiments,col="green")
add.error.bars(experiments,minPoints,maxPoints,width=0.2,col="green")

legend("top",c("99th Percentile","90th Percentile","50th Percentile"),lty=c(1,1,1),lwd=c(2.5,2.5),col=c("green","blue","red"))

dev.off()
}



