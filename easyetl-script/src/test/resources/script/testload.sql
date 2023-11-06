echo 当前目录 `pwd`, 当前时间 `date`

set

# 复制文件
set delfilepath="${HOME}/bhc_finish.del"
rm ${delfilepath}
cp classpath:/bhc_finish.del ${HOME}

declare global test0001 catalog configuration use host ${databaseHost} driver $databaseDriverName url "${databaseUrl}" username ${username} password $password sshuser ${databaseSSHUser} sshuserpw ${databaseSSHUserPw} ssh 22
db connect to test0001

-- 测试数据装载功能
quiet drop table bhcp_finish;
CREATE TABLE bhcp_finish (
    ORGCODE CHAR(20),
    task_name CHAR(60) NOT NULL,
    task_file_path VARCHAR(512),
    file_data DATE NOT NULL,
    CREATE_DATE TIMESTAMP,
    FINISH_DATE TIMESTAMP,
    status CHAR(1),
    step_id VARCHAR(4000),
    error_time TIMESTAMP,
    error_log CLOB,
    oper_id CHAR(20),
    oper_name VARCHAR(60),
    PRIMARY KEY (task_name,file_data)
);
commit;
create index bhcpfinishidx01 on bhcp_finish(ORGCODE,error_time);
commit;

db load from ${delfilepath} of del method p(1,2,3,4,5,6,7,8,9,10,11,12) insert into bhcp_finish(orgcode,task_name,task_file_path,file_data,create_date,finish_date,status,step_id,ERROR_TIME,ERROR_LOG,OPER_ID,OPER_NAME) indexing mode rebuild statistics use profile;
commit;


#db load from ${delfilepath} of del method p(4,3,2,1) replace into bhcp_finish(file_data,task_file_path,task_name,orgcode) indexing mode rebuild statistics use profile;
#db load from ${delfilepath} of del method p(4,3,2,1) modified by charset=gbk replace into bhcp_finish(file_data,task_file_path,task_name,orgcode) indexing mode rebuild statistics use profile prevent repeat operation;
#db load from ${delfilepath} of del method p(4,3,2,1) replace into bhcp_finish(file_data,task_file_path,task_name,orgcode) indexing mode rebuild statistics use profile;

db load from ${delfilepath} of del method p(2,4,5,6,7,8,9,10,11,12) merge into bhcp_finish(task_name,file_data,create_date,finish_date,status,step_id,ERROR_TIME,ERROR_LOG,OPER_ID,OPER_NAME);

delete from bhcp_finish;
commit;

db load from ${delfilepath} of del method p(1,2,3,4,5,6,7,8,9,10,11,12) insert into bhcp_finish(orgcode,task_name,task_file_path,file_data,create_date,finish_date,status,step_id,ERROR_TIME,ERROR_LOG,OPER_ID,OPER_NAME) for exception bhcp_finish_error indexing mode incremental statistics use profile;


echo 测试导出数据文件功能
quiet "drop table v12_test_tmp";
create table v12_test_tmp (
	branch_id char(6) not null,
	branch_name varchar(150),
	branch_type char(18),
	branch_no char(12),
	status char(1),
	primary key(branch_id)
);
commit;
ddl V12_TEST_TMP;


DECLARE sname Statement WITH insert into v12_test_tmp (branch_id, branch_name, branch_type, branch_no, status) values (?, ?, ?, ?, ?) ;
declare progress use out print '插入数据库记录 ${process}%, 一共${totalRecord}笔记录 ${leftTime}' total 100000 times
set tcount=1
while $tcount <= 100123 loop
  set c1 = "$tcount"
  set c2 = "机构$tcount"
  set c3 = "机构类型$tcount"
  set c4 = "编号$tcount"
  set c5 = "0"

  FETCH c1, c2, c3, c4, c5 insert sname;
  progress

  set tcount = $tcount + 1
end loop
commit
undeclare sname Statement

set count = select count(*) from v12_test_tmp with ur;
echo 笔数 $count

rm $temp/v12_test_tmp.txt

declare exportTaskId progress use out print "${taskId}正在执行 ${process}%, 总共${totalRecord}个记录${leftTime}" total $count times

db export to $temp/v12_test_tmp{}.del of del modified by progress=exportTaskId chardel=* charhide=0 escapes=1 writebuf=200 maxrows=30041 title message=$temp/v12_test_tmp.txt select * from v12_test_tmp with ur;

cat $temp/v12_test_tmp.txt
echo ""
echo ""

# 总行数
set count=0
# 总字节数
set bytes=0
set trows=0;
set tlen=0;
while read line do
    set array = line.split('=');
    if array.length() == 2 && array[0].trim() == "数据输出信息" then
       set str = array.length() + ", " + array[0].trim() + ", " + array[1].trim()
       set files=array[1].split(',');

       for filepath in ${files} loop
          set str=`wc $filepath`
          echo $str

          set ts=str.split()
          set count = count + ts[0].trim().int()
          set bytes = bytes + ts[2].trim().int()

          head -n 5 $filepath
          echo ""
       end loop

       echo 文件总行数: $count 总字节数: $bytes
    elseif array.length() == 2 && array[0].trim() == "卸载数据的总行数" then
       set trows=array[1].trim().int()
    elseif array.length() == 2 && array[0].trim() == "卸载数据的总字节数" then
       set tlen=array[1].trim().int()
    else
    fi
done < $temp/v12_test_tmp.txt

if $trows != $count || $tlen != $bytes then
   echo "$trows != $count || $tlen != $bytes"
   exit 1011
fi


container to execute tasks in parallel using thread=2 begin
  db export to $temp/v12_test_tmp_t1.del of del modified by sleep=1000 select * from v12_test_tmp with ur;
  db export to $temp/v12_test_tmp_t2.del of del modified by sleep=2000 select * from v12_test_tmp with ur;
  db export to $temp/v12_test_tmp_t3.del of del modified by sleep=3000 select * from v12_test_tmp with ur;
  db export to $temp/v12_test_tmp_t4.del of del modified by sleep=4000 select * from v12_test_tmp with ur;
  db export to $temp/v12_test_tmp_t5.del of del modified by sleep=2000 select * from v12_test_tmp with ur;
  db export to $temp/v12_test_tmp_t6.del of del modified by sleep=1000 select * from v12_test_tmp with ur;
end
echo $?

-- 测试数据装载功能
quiet drop table bhcp_finish;
CREATE TABLE bhcp_finish (
    ORGCODE CHAR(20),
    task_name CHAR(60) NOT NULL,
    task_file_path VARCHAR(512),
    file_data DATE NOT NULL,
    CREATE_DATE TIMESTAMP,
    FINISH_DATE TIMESTAMP,
    status CHAR(1),
    step_id VARCHAR(4000),
    error_time TIMESTAMP,
    error_log CLOB,
    oper_id CHAR(20),
    oper_name VARCHAR(60),
    PRIMARY KEY (task_name,file_data)
);
commit;

db load from ${delfilepath} of del method p(1,2,3,4,5,6,7,8,9,10,11,12) merge into bhcp_finish(orgcode,task_name,task_file_path,file_data,create_date,finish_date,status,step_id,ERROR_TIME,ERROR_LOG,OPER_ID,OPER_NAME) indexing mode rebuild statistics use profile;
commit;


db connect reset


exit 0