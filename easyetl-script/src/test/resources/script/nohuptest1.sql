
set looptimes=$totalLoops / 2

echo scriptName is ${scriptName} $totalLoops $looptimes

set counter=1
while $counter <= $looptimes loop
  progress
  #echo counter is $counter
  set counter = $counter + 1
end loop

exit 0