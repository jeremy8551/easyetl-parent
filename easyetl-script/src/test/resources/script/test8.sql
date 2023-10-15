
# 定义 step 命令回调函数
declare global command callback for step begin 
  echo "execute $1"
end


# 定义 echo 命令回调函数
declare global command callback for echo begin end

undeclare global command callback for echo


set _t_test_1=0
function _test() {
set _t_test_1= $_t_test_1 + 1
}

_test
if $_t_test_1 != 1 then 
  echo $_t_test_1 错误
	exit 10
fi

# 非正确退出处理
declare continue handler for exitcode == 33 begin
  echo "exit with no 0 is $exitcode!"
end


handler


set _t_test_2 = 2
function _test1() {
  set _t_test_2= $_t_test_2 + $1 + $2 + $3
  return 33
}


_test1 1 2 3
set res=$?
if $res != 33 then
    echo $res != 33 错误
	exit 11
fi


undeclare handler for exitcode == 33;


exit 0