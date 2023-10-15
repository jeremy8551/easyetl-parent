
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

#db load from ${delfilepath} of del method p(1,2,3,4,5,6,7,8,9,10,11,12) insert into bhcp_finish(orgcode,task_name,task_file_path,file_data,create_date,finish_date,status,step_id,ERROR_TIME,ERROR_LOG,OPER_ID,OPER_NAME) indexing mode rebuild statistics use profile;

delete from bhcp_finish;

#db load from ${delfilepath} of del method p(4,3,2,1) replace into bhcp_finish(file_data,task_file_path,task_name,orgcode) indexing mode rebuild statistics use profile;

#db load from ${delfilepath} of del method p(4,3,2,1) modified by charset=gbk replace into bhcp_finish(file_data,task_file_path,task_name,orgcode) indexing mode rebuild statistics use profile prevent repeat operation;

#db load from ${delfilepath} of del method p(4,3,2,1) replace into bhcp_finish(file_data,task_file_path,task_name,orgcode) indexing mode rebuild statistics use profile;

db load from ${delfilepath} of del method p(2,4,5,6,7,8,9,10,11,12) merge into bhcp_finish(task_name,file_data,create_date,finish_date,status,step_id,ERROR_TIME,ERROR_LOG,OPER_ID,OPER_NAME) indexing mode rebuild statistics use profile;

db load from ${delfilepath} of del method p(1,2,3,4,5,6,7,8,9,10,11,12) insert into bhcp_finish(orgcode,task_name,task_file_path,file_data,create_date,finish_date,status,step_id,ERROR_TIME,ERROR_LOG,OPER_ID,OPER_NAME) for exception bhcp_finish_error indexing mode incremental statistics use profile;

exit 0