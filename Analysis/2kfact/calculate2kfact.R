
# manually update file to add a factor for varying param

#load this data, make sure the thput isn't interpreted as factor

#fit linear model:

lm(data$thput~(data$memory+data$body)^2)
