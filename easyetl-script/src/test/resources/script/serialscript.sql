
# 测试跨脚本方法
test111

echo 串行脚本信息输出

set test=1
export set test1 = 2

set totalLoops = 100000
declare progress use out print "串行脚本已执行 ${process}%, 总共${totalRecord}个记录${leftTime}" total $totalLoops times
set counter=1
while $counter <= $totalLoops loop
  progress
  #echo counter is $counter
  set counter = $counter + 1
end loop

exit 0