[TOC]


# 简介

这是一个使用纯 Java 语言编写的脚本引擎工具，可以用来执行SQL脚本、装载数据文件到数据库表中、从数据库表中卸载数据、对数据文件执行剥离增量、登陆远程服务器执行shell命令与脚本等，可以自定义脚本命令。

这是一个通用脚本引擎框架，是一个运行在 Java 虚拟机上的脚本命令解释器。它的功能是动态解释执行脚本命令或脚本文件，将脚本命令译成 JAVA 语言代码来完成一系列的功能。

通用脚本引擎在运行命令与脚本文件时不需要预编译步骤，可以更简单地动态解释脚本命令与脚本文件，并运行命令。

脚本引擎按命令在脚本语句中从左到右的先后顺序逐个读取并运行，会依据每个命令的返回值（整数）来判断命令运行成功还是发生错误： 

如果命令的返回值是 0 时，表示命令运行成功，脚本引擎继续读取下一个命令并运行！ 

如果命令的返回值是非 0 时，表示命令运行错误，并且脚本引擎会终止运行并退出！

通用脚本引擎框架支持多线程并发执行脚本语句。




# 使用方法



## 引入依赖

在**Maven**工程的 `POM` 中增加：

```xml
<dependency>
  <groupId>{0}</groupId>
  <artifactId>{1}</artifactId>
  <version>{2}</version>
</dependency>
```



## 使用示例

```java
public class Main {
    public static void main(String[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
        try {
            ScriptEngine engine = manager.getEngineByExtension("etl");
            engine.eval("echo hello world!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```



##  SpringBoot场景启动器

单独提供了与 **SpringBoot** 框架集成的场景启动器。

场景启动器用 **SpringBoot** 中 `application.propertes` 与 `application.yaml` 配置文件（包括分环境配置文件）中的属性作为环境变量使用。

在脚本引擎中可以直接使用 **SpringBoot** 配置文件中的属性。

可以通过 **Spring** 注入机制，来得到一个脚本引擎 `ScriptEngine` 实例对象。

### 引入依赖

```xml
<dependency>
    <groupId>{0}</groupId>
    <artifactId>{3}</artifactId>
    <version>{2}</version>
</dependency>
```

### 示例

```java
@Controller
public class HelloController {

    @Autowired
    private ScriptEngine engine; // 注入脚本引擎实例，request范围有效

    @RequestMapping("/help")
    @ResponseBody
    public String help() throws ScriptException, IOException {
      engine.eval("echo hello world!");
			return "success";
    }
}
```



## ETL示例

在 `resources` 目录下新建脚本文件 `script/test_etl.sql`

```sql
# 设置变量值
set databaseDriverName="com.ibm.db2.jcc.DB2Driver"
set databaseUrl="jdbc:db2://127.0.0.1:50000/sample"
set username="db2inst1"
set password="db2inst1"

# 打印所有内置变量
set

# 建立数据库连接信息
declare DBID catalog configuration use driver $databaseDriverName url "${databaseUrl}" username ${username} password $password

# 连接数据库
db connect to DBID

# quiet命令会忽略DROP语句的错误
quiet drop table v_test_tab;

# 建表
CREATE TABLE v_test_tab (
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

INSERT INTO v_test_tab
(ORGCODE, TASK_NAME, TASK_FILE_PATH, FILE_DATA, CREATE_DATE, FINISH_DATE, STATUS, STEP_ID, ERROR_TIME, ERROR_LOG, OPER_ID, OPER_NAME)
VALUES('0', '1', '/was/sql', '2021-02-03', '2021-08-09 23:54:26.928000', NULL, '1', '使用sftp登录测试系统服务器', '2021-08-09 23:47:02.197000', '设置脚本引擎异常处理逻辑', '', '');

INSERT INTO v_test_tab
(ORGCODE, TASK_NAME, TASK_FILE_PATH, FILE_DATA, CREATE_DATE, FINISH_DATE, STATUS, STEP_ID, ERROR_TIME, ERROR_LOG, OPER_ID, OPER_NAME)
VALUES('1', '2', '/was/test', '2021-02-03', '2021-08-09 23:54:26.928000', NULL, '1', '使用sftp登录测试系统服务器', '2021-08-09 23:47:02.197000', '使用sftp登录测试系统服务器', '', '');
commit;

# 创建索引
quiet drop index vtesttabidx01;
create index vtesttabidx01 on v_test_tab(ORGCODE,error_time);
commit;

# 将表中数据卸载到文件中
db export to $temp/v_test_tab.del of del select * from v_test_tab;

# 将数据文件装载到指定数据库表中
db load from $temp/v_test_tab.del of del replace into v_test_tab;

# 返回0表示脚本执行成功
exit 0
```

运行脚本文件：

```JAVA
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Assert;
import org.junit.Test;

public class ScriptEngineTest {
    @Test
    public void test() {
        ScriptEngineManager e = new ScriptEngineManager();
        try {
            ScriptEngine engine = e.getEngineByExtension("etl");
            engine.eval(". classpath:/script/test_etl.sql");
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        }
    }
}
```



## 存储过程示例

可以作为存储过程使用，首先建立脚本文件 `test_procedure.sql`，内容如下所示： 

```sql
# 设置变量值
set databaseDriverName="com.ibm.db2.jcc.DB2Driver"
set databaseUrl="jdbc:db2://127.0.0.1:50000/sample"
set username="db2inst1"
set password="db2inst1"

# 打印所有内置变量
set

# 建立数据库连接信息
declare DBID catalog configuration use driver $databaseDriverName url "${databaseUrl}" username ${username} password $password

# 连接数据库
db connect to DBID

# 建立异常捕获逻辑
declare continue global handler for errorcode == -601 begin
  echo 执行命令 ${errorscript} 发生错误, 对象已存在不能重复建立 ${errorcode} ..
end

# 打印所有异常捕获逻辑
handler

# 创建数据库表
CREATE TABLE SMP_TEST (
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

COMMENT ON TABLE SMP_TEST IS '接口文件记导入录表';
COMMENT ON COLUMN SMP_TEST.ORGCODE IS '归属机构号';
COMMENT ON COLUMN SMP_TEST.task_name IS '任务名';
COMMENT ON COLUMN SMP_TEST.task_file_path IS '数据文件所在绝对路径';
COMMENT ON COLUMN SMP_TEST.file_data IS '归属数据日期';
COMMENT ON COLUMN SMP_TEST.CREATE_DATE IS '运行起始时间';
COMMENT ON COLUMN SMP_TEST.FINISH_DATE IS '运行终止时间';
COMMENT ON COLUMN SMP_TEST.status IS '加载状态';
COMMENT ON COLUMN SMP_TEST.step_id IS '报错步骤编号';
COMMENT ON COLUMN SMP_TEST.error_time IS '报错时间';
COMMENT ON COLUMN SMP_TEST.error_log IS '报错日志';
COMMENT ON COLUMN SMP_TEST.oper_id IS '操作员id';
COMMENT ON COLUMN SMP_TEST.oper_name IS '操作员名';

commit;
exit 0
```

运行脚本文件：

```java
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Assert;
import org.junit.Test;

public class ScriptEngineTest {
    @Test
    public void test() {
        ScriptEngineManager e = new ScriptEngineManager();
        try {
            ScriptEngine engine = e.getEngineByExtension("etl");
            engine.eval(". classpath:/script/test_produce.sql");
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        }
    }
}
```



## 脚本示例

```sql
# 设置变量值
set databaseDriverName="com.ibm.db2.jcc.DB2Driver"
set databaseUrl="jdbc:db2://127.0.0.1:50000/sample"
set username="db2inst1"
set password="db2inst1"

# 打印所有内置变量
set

# 建立数据库连接信息
declare DBID catalog configuration use driver $databaseDriverName url "${databaseUrl}" username ${username} password $password

# 连接数据库
db connect to DBID

# 重新建表
quiet drop table v_test_tmp;
create table v_test_tmp (
	branch_id char(6) not null, -- 机构编号
	branch_name varchar(150), -- 机构名称
	branch_type char(18), -- 机构类型
	branch_no char(12), -- 机构序号
	status char(1), -- 状态
	primary key(branch_id)
);
commit;

# 设置总记录数
set v_test_tmp_records=10123

# 批量插入
DECLARE s1 Statement WITH insert into v_test_tmp (branch_id, branch_name, branch_type, branch_no, status) values (?, ?, ?, ?, ?) ;

# 建立进度输出
declare progress use out print '插入数据库记录 ${process}%, 一共${totalRecord}笔记录 ${leftTime}' total ${v_test_tmp_records} times

# 逐条插入数据
set i=1
while $i <= $v_test_tmp_records loop
  set c1 = "$i"
  set c2 = "机构$i"
  set c3 = "机构类型$i"
  set c4 = "编号$i"
  set c5 = "0"

  # 设置SQL参数
  FETCH c1, c2, c3, c4, c5 insert s1;

  # 进度输出
  progress

  # 自动加一
  set i = $i + 1
end loop

# 提交事物
commit;

# 关闭批量插入
undeclare s1 Statement

exit 0
```



## shell示例

```shell
$ ssh ${admin}@${host}:22?password=${adminPw} && export LANG=zh_CN.GBK && db2 connect to ${databaseName} user ${username} using ${password} \
&& db2 "load client from /dev/null of del replace into v10_test_tmp " \
&& db2 "load client from `pwd`/v_test_tmp.del of del replace into v_test_tmp " \
&& db2 connect reset;
```



## sftp示例

```shell
sftp ${ftpuser}@${ftphost}:22?password=${ftppass}
  set ftphome=`pwd`
  ls ${ftphome}
  set remotetestdir="${ftphome}/test"
  rm ${remotetestdir}
  mkdir ${remotetestdir}
  cd ${remotetestdir}
  put `pwd -l`/test.sql
  ls
  exists ${remotetestdir}/test.sql
  isfile ${remotetestdir}/test.sql
  mkdir ${ftphome}/test
  rm ${ftphome}/test
  get ${remotetestdir}/test.sql ${temp}
  exists -l ${temp}/test.sql
bye
```



## ftp示例

```shell
ftp ${ftpuser}@${ftphost}:21?password=${ftppass}
  set ftphome=`pwd`
  set remotetestdir="${ftphome}/rpt1"
  pwd
  rm ${remotetestdir}
  mkdir ${remotetestdir}
  exists ${remotetestdir}/
  ls ${remotetestdir}
  cd ${remotetestdir}
  put $temp/test.sql ${remotetestdir}
  ls ${remotetestdir}
  exists ${remotetestdir}/test.sql
  isfile ${remotetestdir}/test.sql
  mkdir ${ftphome}/test
  isDirectory ${ftphome}/test
  rm ${ftphome}/test
  get ${remotetestdir}/test.sql ${temp}
  exists -l ${temp}/test.sql
bye
```



# 内置变量

脚本引擎内部预定义了一些变量，如下：

## {4}

如果正在执行脚本文件，{4} 表示脚本文件所在目录的绝对路径。



## {5}

如果正在执行脚本文件, {5} 表示脚本文件的名字



## {6}

如果正在执行脚本文件, {7} 表示脚本文件的字符集编码



## {8}

如果正在执行脚本文件, {9} 表示脚本文件中的行间分隔符



## {10}

如果脚本引擎发生异常, {10} 表示异常详细信息



## {11}

如果脚本引擎发生异常, {11} 表示报错的语句



## {12}

如果脚本引擎发生数据库错误, {12} 表示数据库厂商定义的错误码



## {13}

如果脚本引擎发生数据库错误, {13} 表示数据库厂商定义的SQL状态码



## {14}

表示最近一次执行语句的返回值



## {15}

表示最近一次执行sql语句影响的数据记录数 



## {16}

表示当前是否处于 jump 语句中. 如果 jump 变量等于 true 表示脚本引擎正处于 jump 语句中



## {17}

表示当前步骤变量值，即最近一个 step 命令的参数值



## {18}

表示临时文件所在目录



## {19}

如果正在执行脚本文件, 表示当前脚本文件的绝对路径



## {20}

局部变量名，用于存储当前数据库编目名



# 内置命令

脚本引擎框架默认提供了一套内置的基础命令（如 echo、set、help 等命令），这些基础命令无需设置或配置就可以直接使用。

也可以通过设置 JVM 参数 -D{23}={24} 禁用基础命令

基础命令可按功能划分为：

基本命令：echo，pwd，export，exit，default，grep 等。

声明类命令：以 declare 开头的命令

逻辑控制类命令：if，while，for，break，continue，step，jump 等

数据库类命令：select，insert，delete，update，merge, db 等

网络类命令：os，ftp，sftp 等

文件类命令：cd，touch，rm，mkdir，cat，head，tail，tar，zip，gunzip，rar 等

日期类命令：date

线程类命令：container，sleep，nohup，wait 等

管道符操作：|

命令代换符操作：``



## 注释

以符号 `#` 开始的内容都是注释内容。

```shell
echo test # test
# 注释内容
  # 注释内容
```



## echo

标准输出语句。

### 语法

```shell
$ echo [-n] string[;]

[选项]
-n      # 选项用于表示输出信息后不追加回车换行符
\       # 转义字符（反斜杠）可以对回车换行符进行转义
```

### 示例

表示打开信息输出：

```shell
$ echo on
```



表示关闭信息输出：

```shell
$ echo off
```



输出字符串：

```shell
$ echo hello world!
```



输出双引号中的字符串，会替换占位符 `${name}`

```shell
$ echo "hello ${name}!"
```



输出字符串常量，单引号内表示字符串常量，不会对占位符、命令替换符进行解析

```shell
$ echo 'hello `pwd` ${name}'
```



输出标准信息与错误信息到日志文件

```shell
$ echo "${variable}" 1>`pwd`/echo.log 2>`pwd`/echo_err.log
```



输出标准信息与错误信息到日志文件

```shell
$ echo "${variable}" 1>`pwd`/echo.log 2>&1
```



## error

使用错误输出流输出信息。

### 语法

```shell
$ error 输出消息信息[;]
```



## set

设置局部变量。

### 语法

```shell
$ set varname=value[;]
```

### 示例

在变量的赋值表达式中可以引用已有的全局变量、局部变量、全局JDBC配置信息、脚本引擎内部变量、环境变量。

```shell
$ set varname=${count} + 1;
```



可以对数值型变量使用**加减乘除**运算

```shell
$ set varname=1+1;
```



可以对字符型变量进行加法操作

```shell
$ set varname='1'+'2';
```



可以使用变量方法（详 variablemethod 命令）修改变量值

```shell
$ set varname=varname.substr(1, 2).length();
```



设置字符串常量

```shell
$ set varname='content';
```



设置字符型变量

```shell
$ set varname="content";
```



设置数值型变量

```shell
$ set varname=0;
```



可以保存 SQL 语句查询结果

```shell
$ set varname=select count(*) from tablename;
```



## variablemethod

执行变量方法

### 语法

```shell
$ 变量名.变量方法 | 变量名[位置信息]
```

### 示例

```shell
set testline=`wc -l $temp/test.log`
set testline=testline.split()[0]
echo $testline
```

### 内置方法

{21}



## pipe

脚本引擎支持管道符操作

### 语法

```shell
$ 命令一 | 命令二 | .. | 命令N
```

### 示例

```shell
$ cat `pwd`/text | tail -n 1
```



## sub

脚本引擎支持命令替换操作

### 语法

```shell
$ `命令`
```

### 示例

可以在赋值语句中使用：

```shell
$ set currentstr=`echo 20210101 | date "yyyy-MM-dd"`
```

可以在布尔表达式中使用：

```shell
if `cat $temp/headtest.log | tail -n 1` != 5 then
  exit 1
fi
```

可以在表达式中使用：

```shell
$ set loadtask_stdout="现在是" +`date -d ${tmp_enddate} yyyy年MM月dd日`+", 进行日期检查!"
```

可以在输出语句中使用：

```shell
$ echo `currentdate.format(yyyy-MM-dd)` != '2020-01-01'
```

可以在字符串中使用: 

```shell
if "`date -d 20201213 'yyyy-MM-dd' +0day`" != "2020-12-13" then
  echo "`date -d 20201213 'yyyy-MM-dd' +0day`" != "2020-12-13"
  exit 1
fi
```



## function

自定义方法

### 语法

```shell
function 方法名() {
..
}
```

在方法体中使用 `$1` 表示外部输入的参数1， `$0` 表示方法名，`$#` 表示输入参数个数，使用 `return` 关键字可以退出方法体。

需要注意在方法体中不能使用 `step` 与`jump` 语句。

### 示例

```shell
function testfunc() {
  echo "execute testfunc() $1 $2"
  return 1
}
testfunc "1" "2"
```

### 保留方法

```shell
# 执行 step 命令的处理逻辑
$ function step() {echo $1;}

# 执行 echo 命令的处理逻辑
$ function echo() {echo $1;}

# 执行错误的处理逻辑
$ function error() {echo $1;}
```



## executefunction

执行用户自定义方法

### 语法

```shell
$ functionName [参数]...[;]
```

### 示例

```shell
$ function test() {echo $1;}
$ test “hello world!”
```



## export

设置全局变量，设置全局用户自定义方法

### 语法

```shell
$ export set name = value [;]
$ export function 自定义方法名 [;]
```

子脚本会自动继承父脚本中定义的全局变量与全局用户自定义方法

### 示例

```shell
export set myname='this is good '
export set myname = 'this is good ' ;
```



## step

建立步骤标记

### 语法

```shell
$ step 步骤名[;]
```

标记当前执行位置，配合 `jump` 命令，可以实现跳转到标记位置处开始执行的效果。

### 示例

```shell
set jumpvar="string1" 
jump ${jumpvar} 

... 

step string1 

delete from ... ; 
insert into ... ; 
commit; 
... 
step string2; 
...
```

配合 jump 与 function() error() {} 命令使用可以实现从脚本上一次执行报错位置开始向下执行的效果，跳过已执行部分, 如下:

```shell
# script start 

function error() { 
# 记录报错时 step 位置信息, 用于下一次从报错处开始执行 
insert into table ... 
... 
} 


# 查询上一次执行位置信息 
set jumpvar=select ... ; 

# 跳转到上一次执行位置 
jump ${jumpvar} 

... 

step string1 

delete from ... ; 
insert into ... ; 
commit; 
... 
step string2; 
...
```



## jump

跳转到 `step` 命令位置后继续向下执行命令

配合 `step` 命令可以跳转到指定 `step` 标记处继续向下执行脚本，详见 `step` 命令使用说明。

在找到 `step` 命令前会根据命令的 `enableJump()` 方法的返回值判断是否不执行（越过）命令。

在脚本文件中可以使用内置变量 `jump` 判断当前脚本引擎是否处于 `jump` 命令状态。

### 语法

```shell
$ jump 步骤名[;]
```

### 示例

```shell
# script start 
jump stepMessage; 

... 

if "$jump" == "true" then 
... 
else 
... 
fi 

...

step stepMessage 
... 
```



## exit

退出当前正在执行的语句

### 语法

```shell
$ exit 返回值
```

命令的返回值只能是整数，返回零表示执行正确，返回非零表示执行错误。

### 示例

```shell
$ exit 0;
$ exit -1;
$ exit 1
```



## sql

执行SQL语句

SQL语句必须以 `;` 符号结束标志。

需要在SQL语句中把字符 `$` 替换成 `&ads;`

内置变量名 `updateRows` 表示SQL语句影响记录的笔数。

在脚本文件中可以使用SQL注释`--`与`/** */`

### 语法

```shell
$ [sql] [ select .. | insert .. | delete .. | update .. | merge .. | alter .. | create .. | drop .. ];
```

SQL语句结尾处必须用 `;` 符号表示结束

需要在SQL语句中把字符 `$` 替换成 `&ads;`

内置变量名 `updateRows` 表示SQL语句影响记录的笔数, 如：echo SQL共更新 ${updateRows} 条记录! 

### 示例

```shell
update tableName set field='a' where ...;
echo SQL共更新 ${updateRows} 条记录!
```





## declarecatalog

定义数据库编目信息

### 语法

```shell
$ declare [global] 数据库编目名 catalog configuration use driver 数据库驱动类名 url 数据库JDBC的URL路径 username 用户名 password 密码 [;]
$ declare [global] 数据库编目名 catalog configuration use file JDBC配置文件绝对路径 [;]
```

global是可选选项，表示数据库编目配置信息是全局可被子脚本继承使用

数据库驱动类名是必填选项，二端可用单引号或双引号包住

数据库JDBC的URL路径是必填选项，二端可用单引号或双引号包住

用户名是必填选项，二端可用单引号或双引号包住

密码是必填选项，二端可用单引号或双引号包住

JDBC配置文件中必须要有 driverClassName 属性，url 属性，username 属性，password 属性。

host 属性是可选选项表示数据库服务器的主机名或IP地址

admin 与 adminPw 属性是可选选项表示数据库示例用户名和密码

sshUser，sshUserPw 与 sshPort 属性是可选选项分别表示数据库服务器SSH协议端口，用户名，密码。

### 示例 

```shell
$ declare global name catalog configuration use file /home/udsf/jdbc.properties;

$ declare global name catalog configuration use driver com.ibm.db2.jcc.DB2Driver url 'jdbc:db2://127.0.0.1:50000/databaseName' username admin password admin;
```



## dbconnect

### 建立连接

```shell
$ db connect to 数据库编目名[;]
```

### 关闭连接

```shell
$ db connect reset[;]
```



## dbexport

从数据库中卸载数据到指定位置

### 语法

```shell
$ db export to 卸载位置 of 数据类型 [ modified by 参数名=参数值 参数名=参数值 参数名 ] select * from table;
```

默认支持的卸载位置说明: 

| 说明                         | 位置信息                                                     |
| ---------------------------- | ------------------------------------------------------------ |
| 将数据卸载到本地操作系统上   | 本地操作系统文件绝对路径                                     |
| 将数据卸载到远程sftp服务器上 | sftp://用户名@远程服务器host:端口?password=登陆密码/数据文件存储路径 |
| 将数据卸载到远程 ftp服务器上 | ftp://用户名@远程服务器host:端口?password=登陆密码/数据文件存储路径 |
| 将数据通过http响应下载给用户 | http://download/HttpServletRequest对象的变量名/HttpServletResponse对象的变量名/下载文件名（需要提前将 HttpServletRequest 对象与 HttpServletResponse 对象保存到脚本引擎变量中，变量分别是: httpServletRequest, httpServletResponse） |

### 自定义卸载位置

卸载位置的用户自定义格式: bean://类型值/xxx/xxx

卸载位置的用户自定义JAVA类的样例: 

```shell
@{1}(name = "name")
public class UserDefineWriter implements {4} {
..
}
```

```shell
db export to bean://kind/mode/major/minor/../.. of txt select * from table;
```

已实现的用户自定义卸载位置:

{6}

默认支持的数据类型说明:

{5}

实现用户自定义数据类型：

必须实现 {7} 接口上的方法

如: 自定义 csv 数据类型：

```shell
@{1}(name = "csv")
public class CsvExtractStyle implements {0} {
..
public CsvExtractStyle() ..
..
}
```

### 参数说明

```properties
charset:        表示数据卸载后的字符集，默认使用 JVM 的 file.encoding 参数作为默认值 
codepage:       表示数据卸载后的字符集，默认使用 JVM 的 file.encoding 参数作为默认值（与 charset 属性冲突） 
rowdel:         表示行间分隔符，使用回车或换行符需要转义，如: \\r \\n 
coldel:         表示文件中字段间的分隔符 
escape:         表示文件中字符串中的转义字符 
chardel:        表示字符串型的字段二端的限定符 
column:         表示文件中每行记录的字段个数（如果记录的字段数不等于这个值时会抛出异常） 
colname:        表示文件中字段名，格式是：位置信息:字段名，如: 1:客户名,2:客户编号 如果已设置 table 属性则可以使用表中字段名如：username:客户名,2:userage 
catalog:        表示数据库编目编号，用于设置从哪个数据源中卸载数据，默认使用脚本引擎当前数据库编目 
message:        消息文件绝对路径参数, 可以保存卸数数据任务的运行消息，如果文件路径无法访问时默认将消息文件保存到用户跟目录下的 $HOME/messagefile/export 目录下。
listener:       任务生命周期监听器集合, 每个监听器的 java 类名之间用半角逗号分隔，监听器类必须实现 {2} 接口    
convert:        数据集字段的处理逻辑集合, 格式：字段名:字段处理逻辑类名，格式: JAVA处理逻辑类名?属性名=属性值&属性名=属性值 
                \t\t\t\t\t\t其中字段处理逻辑类名必须实现 {3} 接口, 可以在类名后使用 “?属性名=属性值” 格式向处理逻辑中设置属性 
                \t\t\t\t\t\t使用半角逗号分隔 
                \t\t\t\t\t\tconvert 参数映射关系的优先级 高于 数据库方言提供的映射关系 
charhide:       字符串型字段的字符过滤器参数，指定哪些字符需要过滤，未设置参数时默认过滤回车符和换行符    
escapes:        字符串型字段中需要进行转义的所有字符集合 
writebuf:       数据输出流中缓冲区的行数（必须大于零），默认缓冲 100 行    
append:         写入数据的方式, 无值型属性，设置属性时表示将数据追加写入到卸载位置上，不设置属性时表示覆盖原有数据。
maxrows:        表示最大行数, 超过最大行数时将后续数据写入到一个新存储上，通过增加数字编号区分不同存储信息，默认 0 表示无限制 
dateformat:     表示日期格式 
timeformat:     表示时间格式 
timestampformat:表示时间戳格式 
progress:       表示卸载数据的进度输出接口编号，需要提前在脚本引擎中定义进度输出接口 
```

`db export` 命令支持 `container` 命令，可以并行执行多个数据卸载命令

### 示例

```shell
declare global test0001 catalog configuration use host ${databaseHost} driver $databaseDriverName url "${databaseUrl}" username ${username} password $password sshuser ${databaseSSHUser} sshuserpw ${databaseSSHUserPw} ssh 22

db connect to test0001

declare exportTaskId progress use out print "${taskId}正在执行 ${process}%, 总共${totalRecord}个记录${leftTime}" total $tcount times

db export to $temp\v7_test_tmp.del of del modified by progress=exportTaskId chardel=* charhide=0 escapes=1 writebuf=200 maxrows=30041 title message=$temp/v7_test_tmp.txt select * from v7_test_tmp with ur;
```



## dbload

将指定位置的数据文件装载到数据库表中

```shell
$ db load from 文件位置 of 数据类型 [ method P(3,2,1) C(字段名, 字段名) ] [ modified by 参数名=参数值 参数名=参数值 参数名 ] [ replace | insert | merge ] into table[(字段名,字段名,字段名)] [ for exception tableName ] [ indexing mode [ rebuild | incremental ]] [ statistics use profile ] [ prevent repeat operation ];
```

### 文件位置

用于指定数据文件绝对路径，可以指定多个文件路径（用半角逗号分割，文件结构必须一致）。

### 数据类型

默认支持的数据类型有:

{5}

实现用户自定义数据类型

必须实现 {7} 接口上的方法

如: 自定义 csv 数据类型：

```shell
@{1}(name = "csv", description = "csv文件格式")
public class CsvExtractStyle implements {0} {
..
public CsvExtractStyle() ..
..
}
```

### 参数说明

```properties
charset:        表示数据文件的字符集，默认使用 JVM 的 file.encoding 参数作为默认值 
codepage:       表示数据文件的字符集，默认使用 JVM 的 file.encoding 参数作为默认值（与 charset 属性冲突） 
rowdel:         表示数据文件中行间分隔符，使用回车或换行符需要转义，如: \\r \\n 
coldel:         表示数据文件中字段间的分隔符 
escape:         表示数据文件中字符串中的转义字符 
chardel:        表示数据文件中字符串型字段的二端的限定符 
column:         表示文件中每行记录的字段个数（如果记录的字段数不等于这个值时会抛出异常） 
colname:        表示文件中字段名，格式是：位置信息:字段名，如: 1:客户名,2:客户编号 如果已设置 table 属性则可以使用表中字段名如：username:客户名,2:userage 
readbuf:        用于指定输入流缓冲区长度（单位字节），可以是 100M 或 1G 这种格式  
catalog:        表示数据库编目编号，用于设置从哪个数据源中卸载数据，默认使用脚本引擎当前数据库编目。 
tableCatalog:   表示数据库表所属数据库的编目  
launch:         表示装数引擎启动条件，属性值可以是类名或脚本语句（脚本语句返回值是0表示可以执行数据装载，返回值是非0不能执行数据装载）  
convert:        数据集字段的处理逻辑集合, 格式：字段名:字段处理逻辑类名?属性名=属性值&属性名=属性值，用于设置字段名数据转换器的映射关系  
savecount:      表示每装载 n 行后建立一致点。消息文件中将生成和记录一些消息，用于表明在保存点所在时间上有多少输入行被成功地装载。 
dateformat:     表示日期字符串格式 
timeformat:     表示时间字符串格式 
timestampformat:表示时间戳字符串格式 
keepblanks:     表示将数据装入到一个变长列时，会截断尾部空格，若未指定则将保留空格。只有参数名无需设置参数值；
message:        用于指定消息文件绝对路径  
nocrlf:         表示自动删除字符串中的回车符和换行符，只有参数名无需设置参数值  
dumpfile:       用于指定错误数据存储文件路径  
progress:       用于指定装载文件的进度输出接口编号，需要提前在脚本引擎中定义进度输出接口 
```

### 数据装载模式

```
replace:        表示先清空数据库表中所有数据，然后再读取文件批量插入到数据库表中。 
insert:         表示插入模式：读取文件内容，并将文件每行记录通过批量接口插入到数据库表中。 
merge:          表示合并模式：读取文件内容，并将文件每行记录通过批量接口插入到数据库表中。如果数据在数据库表中已存在则使用文件内容更新，如果数据不存在则将文件行插入到表中。  
_               使用合并模式时，需要使用 method C(字段名, 字段名) 语句设置判断记录是否相等的字段名。 
```

### 设置字段顺序

可以使用 method P(3,2,1) 句柄设置文件中列插入到数据库表的顺序，如 method P(3,2,1) 表示按第三列，第二列，第一列的顺序读取文件中每列数据并插入到数据库表中。
可以在数据库表名后面使用小括号与字段名的方式指定向数据库表中插入字段的顺序，如：tableName(fname1,fname3,fname2)，表示按 fname1,fname3,fname2 字段顺序插入数据。

### 其他句柄说明

指定对于主键冲突错误问题，自动将重复记录保存到 for 语句指定的表中。

```shell
for exception tableName
```

指定装载文件之前，先根据消息文件中的内容判断是否需要重新装载数据文件，只有参数名无需设置参数值

```shell
prevent repeat operation
```

用于设置索引模式，rebuild 表示先删除索引文件装载成功后重建索引，incremental 表示只向索引中添加新的数据

```shell
indexing mode [ rebuild | incremental ]
```

表示在数据文件装载成功后，因为数据库表中添加了更多的数据，导致之前的目标表统计信息很可能已经无效了。可以为数据库表重新收集统计信息

```shell
statistics use profile
```

`db load` 命令支持 `container` 命令，可以并行执行多个数据文件装载命令。



## increment

对比二个表格型文件并抽取增量数据

```shell
$ extract increment compare 新文件 of 数据类型 modified by 属性名=属性值 and 旧文件 of 数据类型 modified by 属性名=属性值 write [new [and] upd [and] del] into filepath [of 数据类型] [modified by 属性名=属性值] [write log into [filepath | stdout | stderr]]
```

### 参数说明

新文件表示新文件的绝对路径 

旧文件表示旧文件的绝对路径 

数据类型表示文件内容对应的类型标识符，当前脚本引擎支持的类型有：
{0}
可以定义增量数据输出流将新增数据，变化数据，删除数据输出到指定文件中，如下面语句表示将新增数据与变化的数据写入到 filepath 文件中，且新增数据的第一个字段修改为false，第二个字段内容修改 uuid，第三个字段内容修改为格式是yyyyMMddHHmmss的当前时间

```shell
write new and upd into /home/user/inc.txt of txt modified by newchg=1:false,2:uuid updchg=3:date=yyyyMMddHHmmss
```

可以将新增，变化，删除数据都写入到一个文件中（如果输出流中未定义数据类型时，默认使用新文件的数据类型），如:

```shell
write into /home/user/inc.txt modified by newchg=1:uuid
```

可以定义日志信息输出流（非必填）将增量日志写入到指定文件中或脚本引擎的标准输出流中，如：

```shell
write log into /home/user/inc.log
write log into stdout 
write log into stderr
```

### 文件属性

```properties
charset:    表示数据文件的字符集，默认使用 JVM 的 file.encoding 参数作为默认值 
codepage:   表示数据文件的字符集，默认使用 JVM 的 file.encoding 参数作为默认值（与 charset 属性冲突） 
rowdel:     表示数据文件中的行间分隔符，使用回车或换行符需要转义，如: \\r \\n 
coldel:     表示数据文件中字段间的分隔符 
escape:     表示数据文件中字符串中的转义字符 
chardel:    表示数据文件中字符串型字段的二端的限定符 
column:     表示数据文件中每行记录的字段个数（如果记录的字段数不等于这个值时会抛出异常） 
colname:    表示数据文件中字段名，格式是：位置信息:字段名，如: 1:客户名,2:客户编号 如果已设置 table 属性则可以使用表中字段名如：username:客户名,2:userage 
index:      必填，表示数据文件中唯一确定一条记录的索引字段集合，格式: 字段位置信息, 如：1,2,3,4 如果已设置 table 属性则可以使用表中字段名如： id,name,age,value 
compare:    表示文件中比较字段（相同索引字段时，用于区分二条记录是否相等的字段，如果二条记录中的索引字段与比较字段都相等则认为二条记录相等），格式: 字段位置信息如：1,2,3,4 如果已设置 table 属性则可以使用表中字段名如：name,age,val1,val2。未设置参数时会默认比较记录中每个字段值 
table:      表示文件中字段对应的数据库表名（可以是 schema.tableName 格式）
catalog:    表示脚本引擎中定义的数据库编目号 
readbuf:    表示读取文件时使用的字符缓冲区长度，默认 100M 个字符 
progress:   表示脚本引擎中已定义的进度输出编号，用于输出文件的读取进度信息 
nosort:     设置 true 表示剥离增量之前不会排序文件，默认是 false 表示先排序文件然后再执行剥离增量 
sortcache:  排序文件输出流使用的缓冲行数，默认是 100 行 
sortrows:   排序文件时每个临时文件的最大行数，默认是 10000 行
sortThread: 排序文件时的线程数，默认是 3 个线程 
sortReadBuf:排序文件时的输入流的缓冲区长度，默认是 10M 个字符 
maxfile:    排序文件时，每个线程每次合并的最大临时文件数, 默认是 4 个文件 
keeptemp:   设置 true 表示排序文件后保留临时文件，默认是 false 表示删除产生的临时文件 
covsrc:     设置 true 表示排序文件后覆盖源文件，默认是 false 表示保留源文件内容 
```

增量数据输出流支持的属性有：

```properties
newchg:     表示对新增数据中字段的替换规则 
updchg:     表示对变化数据中字段的替换规则 
delchg:     表示对删除数据中字段的替换规则 
charset:    表示文件对应的字符集编码，默认使用JAVA虚拟机默认的文件字符集 
codepage:   表示文件对应的代码页（与 charset 属性冲突）
append:     设置 true 表示追加方式写入文件，默认是 false 表示覆盖文件 
outbuf:     设置输出流的缓冲行数，默认是 20 行 
```

剥离增量命令支持 container 命令，可以大批量并行执行多个剥离增量命令



## sorttablefile

对表格型文件排序

```shell
$ sort table file 数据文件绝对路径 of 数据类型 [modified by 属性名=属性值 属性名] order by 排序字段 {asc | desc}
```

### 参数说明

数据类型表示文件内容对应的类型标识符，当前脚本引擎支持的类型有:
{0}

### 文件属性

```properties
charset:    表示数据卸载后的字符集，默认使用 JVM 的 file.encoding 参数作为默认值 
codepage:   表示数据卸载后的字符集，默认使用 JVM 的 file.encoding 参数作为默认值（与 charset 属性冲突） 
rowdel:     表示行间分隔符，使用回车或换行符需要转义，如: \\r \\n 
coldel:     表示文件中字段间的分隔符 
escape:     表示文件中字符串中的转义字符 
chardel:    表示字符串型的字段二端的限定符 
column:     表示文件中每行记录的字段个数（如果记录的字段数不等于这个值时会抛出异常） 
colname:    表示文件中字段名，格式是：位置信息:字段名，如: 1:客户名,2:客户编号 如果已设置 table 属性则可以使用表中字段名如：username:客户名,2:userage 
readbuf:    表示读取文件时使用的字符缓冲区长度，默认 10M 个字符 
writebuf:   表示写文件时使用的缓存行数，默认 100 行 
thread:     排序文件时的线程数，默认是 3 个线程 
maxrow:     表示每个临时文件最大记录书, 默认是 10000 行 
maxfile:    排序文件时，每个线程每次合并的最大临时文件数, 默认是 4 个文件 
keeptemp:   无属性值，使用属性表示排序文件后保留临时文件，否则表示自动删除产生的临时文件 
covsrc:     设置 true 表示排序文件后覆盖源文件，默认是 false 表示保留源文件内容 
```

排序字段:    由排序字段的位置（大于零）组成，如: 1,2,3

排序方向:    asc 或 desc

asc:        排序字段从小到大排列 

desc:       排序字段从大到小排列 



## container

建立并发任务的运行容器，并立即运行并发任务

### 语法

```shell
$ container to execute tasks in parallel [ using 参数名=参数值 参数名=参数值 参数名 ] begin 并发任务 end
```

### 参数说明

thread    表示同时运行任务的数量，如未设置参数默认同时最多执行3个任务

参数会作为并发任务的默认参数值

```shell
container to execute tasks in parallel using thread=3 rowdel=\\r\\n coldel=: begin  
  db export to $filepath1.del of del select * from table with ur; 
  db export to $filepath2.del of del select * from table with ur; 
  db export to $filepath3.del of del select * from table with ur; 
  db export to $filepath4.del of del select * from table with ur; 
  db export to $filepath5.del of del select * from table with ur; 
  db export to $filepath6.del of del select * from table with ur; 
end
```



## commit

提交当前数据库连接上的事务

### 语法

```shell
$ commit[;]
```



## rollback

回滚当前数据库连接上的事务

### 语法

```shell
$ rollback[;]
```



## quiet

以静默方式执行命令，如果命令发生错误时不会抛出异常或输出错误信息。

### 语法

```shell
$ quiet 命令语句;
```

### 示例

```shell
$ quiet select * from table;
$ quiet commit
```



## callprocudure

执行数据库存储过程

### 语法

```shell
$ call SCHEMA.PRODUCENAME(?)[;]
```

### 示例

```shell
$ call SYSPROC.ADMIN_CMD('reorg indexes all for table ALLOW READ ACCESS');
$ call TEST('read in msg', ?);
$ call TEST('read in msg', $RES); echo $RES;
```



## cursor

遍历数据库游标

### 语法

```shell
cursor 游标名 loop
..
end loop
```

可以在循环体中使用 break, continue, return 控制语句

- break：退出当前循环
- continue：执行下一次循环
- return：退出当前方法



## while

while 循环语句

### 语法

```shell
while .. loop
..
end loop
```

可以在循环体中使用 break, continue, return 控制语句

- break：退出当前循环
- continue：执行下一次循环
- return：退出当前方法



## for

for 循环语句

### 语法

```shell
for 变量名 in 数组表达式 loop
..
end loop
```

for 循环语句用于便利数组中的元素信息，每次循环会按顺序将数组中的元素保存到变量名中，可通过变量名在循环体中引用数组中的变量。

数组表达式范围：

1）可以是替换命令如：`ls` 
2）可以是一个数组变量名，如: `${var}` 
3）可以是字符串常量，如：`(1,2,3,4)`

可以在循环体中使用 break, continue, return 控制语句

- break：退出当前循环
- continue：执行下一次循环
- return：退出当前方法



## read

读取文件或文本信息

### 语法

```shell
while read 变量名 do
..
done < [ filepath | command ]
```

可以在循环体中使用 break, continue, return 控制语句

- break：退出当前循环
- continue：执行下一次循环
- return：退出当前方法

### 示例

```shell
# 循环遍历 /home/user 目录下的文件
while read line do 
\t.. 
done < ls /home/user 


# 逐行读取文件中内容
while read line do 
\t..
done < /home/user/list.txt
```



## if

if语句，支持嵌套使用。

### 语法

```shell
if .. then .. elsif .. then .. elseif .. then .. fi
```



## ssh2

登录 ssh 服务器并执行命令

### 语法

```shell
$ ssh username@host:port?password= && . /etc.profile && . ~/.profile && shell command [;]
```

### 示例

```shell
$ ssh admin@192.168.1.1:10?password=admin && ./shell.sh && . ~/load.sh
```



## declaresshtunnel

建立本地端口转发隧道，配合 sftp 命令实现通过本地局域网代理服务器访问远程服务器ssh端口的功能。

### 语法

建立隧道命令：

```shell
$ declare 隧道名 ssh tunnel use proxy 代理服务器用户名@代理服务器HOST:代理服务器SSH端口号?password=密码 connect to 本地端口号:远程服务器HOST:远程服务器SSH端口号 [;]
```

其中本地服务器端口号为零时表示端口由操作系统随机分配，随机分配的端口号通过标准输出接口输出

关闭隧道命令：

```shell
$ undeclare 隧道名 ssh tunnel [;]
```

### 示例

```shell
# 建立隧道并获取本地端口号 
set localport=`declare sshname ssh tunnel use proxy root@192.168.1.10:22?password=root connect to 0:192.168.10.20:22 | tail -n 1` 

# 建立sftp连接
'sftp test@127.0.0.1:${localport}?password=test' 
\tput `pwd`/file.txt /home/test 
bye

# 关闭隧道 
undeclare sshname tunnel
```



## sftp

建立SFTP连接

### 语法

```shell
$ sftp 用户名@服务器HOST:端口?password=密码
```

### 相关命令

可以使用如下 SFTP 命令操作文件: 

```properties
cd          filepath 进入远程服务器目录  
ls          filepath 查看远程服务器上文件列表信息 
rm          filepath 删除远程服务器上文件或目录 
mkdir       filepath 在远程服务器上创建目录 
pwd         filepath 查看远程服务器上当前目录的绝对路径 
exists      filepath 判断远程服务器上的文件或目录是否存在  
isfile      filepath 判断远程服务器的文件是否存在 
isDirectory filepath 判断远程服务器的目录文件是否存在 
get         remotefilepath localfilepath 从远程服务器下载文件 
put         localfilepath remotefilepath 上传文件到远程服务器 
bye         关闭 SFTP 连接 
```

在 `cd ls rm mkdir pwd exists isFile isDirectory` 语句中可以使用 -l 选项, 表示操作本地操作系统上的文件



## ftp

建立FTP连接

### 语法

```shell
$ ftp 用户名@服务器HOST:端口?password=密码
```

### 相关命令

可以使用如下FTP命令操作文件

```properties
cd          filepath 进入远程服务器目录  
ls          filepath 查看远程服务器上文件列表信息 
rm          filepath 删除远程服务器上文件或目录 
mkdir       filepath 在远程服务器上创建目录 
pwd         filepath 查看远程服务器上当前目录的绝对路径 
exists      filepath 判断远程服务器上的文件或目录是否存在  
isfile      filepath 判断远程服务器的文件是否存在 
isDirectory filepath 判断远程服务器的目录文件是否存在 
get         remotefilepath localfilepath 从远程服务器下载文件 
put         localfilepath remotefilepath 上传文件到远程服务器 
bye         关闭 FTP 连接 
```

在 `cd ls rm mkdir pwd exists isFile isDirectory` 语句中可以使用 -l 选项, 表示操作本地操作系统上的文件



## ls

显示本地目录下的文件或远程ftp服务器当前目录下文件

### 语法

```shell
$ ls 文件名或文件路径 [;]
```

文件名或文件路径二端可以使用成对的单引号或双引号

### 选项

```shell
-l 表示文件名或文件路径是本地操作系统文件路径
```



## cd

进入本地目录或远程服务器目录

### 语法

```shell
$ cd 文件名或文件路径 [;]
```

文件名或文件路径二端可以使用成对的单引号或双引号

### 选项

```shell
-l 表示文件名或文件路径是本地操作系统文件路径
```



## length

显示本地文件或远程文件的大小，或测量字符串的长度

### 语法

```shell
$ length string;
```

### 选项

```shell
-h 选项表示输出可读高的信息
-b 选项表示显示字节数 
-c 选项表示显示字符数 
-f 选项表示本地文件的字节数 
-r 选项表示显示远程文件的字节数
```

### 示例

```shell
length -h string;   
length -b string;   
length -c string;   
length -f filepath; 
length -r remotefilepath;
```



## pwd

显示本地目录路径或远程ftp服务器当前目录路径

### 语法

```shell
$ pwd [-l] [;]
```

### 选项

```shell
-l 选项表示显示本地操作系统上的目录
```



## mkdir

创建本地目录或在远程服务器上创建目录

### 语法

```shell
$ mkdir [-l] 文件名或文件路径 [;]
```

文件名或文件路径二端可以使用成对的单引号或双引号

### 选项

```shell
-l 选项表示文件名或文件路径是本地操作系统上的文件
```



## rm

删除本地文件或目录或远程ftp服务器上的文件或目录

### 语法

```shell
$ rm [-l] 文件名或文件路径 [;]
```

文件名或文件路径二端可以使用成对的单引号或双引号

### 选项

```shell
-l 选项表示文件名或文件路径是本地操作系统上的文件
```



## isfile

判断文件是否存在或远程ftp服务器上是否存在文件

### 语法

```shell
$ [!]isfile [-l] 文件名或文件路径 [;]
```

文件名或文件路径二端可以使用成对的单引号或双引号

### 选项

```shell
-l 选项表示文件名或文件路径是本地操作系统上的文件
```



## isdirectory

判断目录文件是否存在或远程ftp服务器上是否存在目录

### 语法

```shell
$ [!]isDirectory [-l] 文件名或文件路径 [;]
```

文件名或文件路径二端可以使用成对的单引号或双引号

### 选项

```shell
-l 选项表示文件名或文件路径是本地操作系统上的文件
```



## exists

判断文件路径在本地是否存在 或 远程ftp服务器上是否存在文件路径

### 语法

```shell
$ [!]exists [-l] 文件名或文件路径 [;]
```

文件名或文件路径二端可以使用成对的单引号或双引号

### 选项

```shell
-l 选项表示文件名或文件路径是本地操作系统上的文件
```



## cat

输出文件内容

### 语法

```shell
$ cat 文件名或文件路径 [;]
```

文件名或文件路径二端可以使用成对的单引号或双引号



## head

输出文件前几行的内容

### 语法

```shell
$ head [-n 行号] 文件名或文件路径 [;]
```

文件名或文件路径二端可以使用成对的单引号或双引号

### 示例

表示输出文件前10行的内容

```shell
$ head -n 10 /home/user/file.txt
```



## tail

输出文件结尾几行的内容

### 语法

```shell
$ tail [-n 行号] 文件名或文件路径 [;]
```

文件名或文件路径二端可以使用成对的单引号或双引号

### 示例

表示输出文件结尾最后一行的内容

```shell
$ tail -n 1 /home/user/file.txt
```



## wc

显示文件的行数、字数、字节数、文件名

### 语法

```shell
$ wc [-l] [-w] [-c] filepath [;]
```

文件名或文件路径二端可以使用成对的单引号或双引号

### 选项

```shell
-l 选项表示行数 
-w 选项表示字符数 
-c 选项表示字节数 
```



## df

显示当前操作系统的文件系统信息

### 语法

```shell
$ df [;]
```

### 输出格式

```shell
第一个字段是文件系统 
第二个字段是总容量 
第三个字段是剩余容量 
第四个字段是已用容量 
第五个字段是文件系统类型（如: ext4） 
第六个字段是挂载位置信息
```



## dos2unix

将文件或字符串中的行间分隔符转为换行符

### 语法

```shell
$ dos2unix 文件路径|字符串 [;]
```



## grep

过滤数据

### 语法

```shell
$ grep string
```

可以在管道符后过滤前一个命令的标准输出信息

### 选项

```shell
-i 选项表示忽略字符大小写 
-v 选项表示不包括字符串参数 
```

### 示例

```shell
$ cat $temp/greptest.txt | grep -i test | wc -l
```



## executeos

执行本地操作系统命令，脚本引擎当前默认命令是 os 时，执行本地命令语句时可以不使用 os 前缀。

### 语法

```shell
$ os command [;]
```

### 示例

```shell
$ os cd /home/user/dir 
$ os ipconfig /all 
```



## executefile

执行脚本文件，可以在脚本文件前使用 nohup 命令实现并行执行脚本文件。

子脚本可继承父脚本的全局变量、全局的数据库编目配置信息、全局的用户自定义方法、全局的异常错误处理逻辑、全局的 echo 命令处理逻辑、全局的错误处理逻辑、全局的步骤输出逻辑。

### 语法

```shell
$ . 文件名或文件路径 [;]
```

```shell
$ source 文件名或文件路径 [;]
```

### 输出

```shell
- 可以使用 >> 或 > 字符将日志信息输出到指定文件 
- 可以使用 1>> stdlogfile 表示将标准输出信息写入 stdlogfile 日志 . 
- 可以使用 2>> errlogfile 表示将错误输出信息写入 errlogfile 日志 . 
- 可以使用 2>&1 语句将标准输出与错误输出都写到同一个日志文件. 
- 可以使用 wait 语句等待并行脚本执行完毕，并返回脚本的返回值 
```

### 示例

```shell
$ nohup . /home/user/script.sql &
```



## daemon

执行脚本文件，与 source 命令不同点: 脚本执行完毕后，会将脚本产生的局部变量，全局变量，全局的数据库编目信息同步到当前脚本引擎中。

### 语法

```shell
$ daemon 文件名或文件路径 [;]
```



## declareprogress

建立进度输出逻辑

### 语法

```shell
$ declare [global] [任务编号] progress use 输出方式 print 输出信息 total 总循环次数 times [;]
```

任务编号是可选选项,  用于在并发任务区分不同任务的唯一编号 

global是可选选项,  用于表示是全局进度输出逻辑（可被子脚本继承） 

输出方式是必填选项,  用于设置进度输出方式： out表示使用标准输出 err表示使用错误输出 step表示使用step输出 

总循环次数是必填选项,用于设置总循环次数，只能是整数数值 

#### 占位符

```shell
输出信息中可以使用 ${process} 输出当前进度百分比 
输出信息中可以使用 ${totalRecord} 输出总循环次数 
输出信息中可以使用 ${leftTime} 输出预估的剩余时间 
输出信息中可以使用 ${taskId} 输出多任务输出的任务编号 
```

### 示例

```shell
# 定义一个进度输出信息 
declare global progress use step print "正在更新数据库记录 ${process}, 共有 ${totalRecord} 笔数据记录, ${leftTime} " total 10000 times 

while ... loop 
\t# 进度输出 
\tprogress 
\t... 
end loop
```



## declarehandler

建立异常处理逻辑

### 语法

```shell
$ declare (exit | continue) handler for ( exception | exitcode != 0 | sqlstate == '02501' | errorcode -803 ) begin .. end
```

### 保留变量

```shell
{0} 当脚本引擎发生异常时,      {1} 表示异常详细信息
{2} 当脚本引擎发生数据库错误时, {3} 表示数据库厂商提供的错误码
{4} 当脚本引擎发生数据库错误时, {5} 表示数据库厂商提供的SQL状态
{6} 当脚本引擎发生异常错误时,   {7} 表示发生错误的脚本语句
{8} 当脚本引擎执行语句完毕时,   {9} 表示语句执行的返回值, 一般来讲返回0表示正确 非0表示错误
```



## undeclarehandler

删除异常处理逻辑

### 语法

```shell
$ undeclare handler for ( exception | exitcode == 0 | sqlstate == 120 | sqlcode == -803 ) ;
```



## handler

显示异常处理逻辑，打印脚本引擎当前的 echo方法处理逻辑，error方法处理逻辑，step方法处理逻辑，所有异常处理逻辑。

### 语法

```shell
$ handler[;]
```



## callback

建立命令的回调函数

在宿主命令执行完毕之后自动执行该命令的回调函数内容。

宿主命令表达式对应的脚本命令必须实现 {0} 接口，命令表达式可以是一个单词（如：echo 或 step 或 error）或一个语句（语句中不能有 begin 关键字）。

回调函数内容可以由单个或多行命令组成的段落，在回调函数内容中可以通过 $1 这种形式使用宿主命令的参数。

每个宿主命令上都可以定义多个回调函数，按定义先后顺序执行回调函数内容。 

### 语法

```shell
$ declare [global] command callback for 宿主命令表达式 begin 回调函数内容 end
```

### 示例

```shell
# 定义一个 echo 命令的回调函数，实现将 echo 命令输出的内容同时写入到数据库表中。
declare gloabl command callback for echo begin
...
insert into logtable (content) values ($1) 
...
end
```

可以使用如下命令删除宿主命令 echo 上的所有回调函数: 

```shell
$ undeclare global command callback for echo
```



## declarestatement

建立数据库批处理逻辑

可以使用 FETCH 变量名1, 变量名2, .. INTO 批处理名字; 语句批量更新数据库中数据

可以使用 undeclare 批处理名字 statement; 语句关闭批处理程序

### 语法

```shell
$ declare 批处理名字 statement by 笔数 batch with insert into table (f1,f2) values (?,?) ;
```

### 示例

```shell
declare s1 statement by 1000 batch with insert into table (f1,f2) values (?,?) ; 
\tset val1='1'
\tset val2='2'
\tFETCH val1, val2 insert s1; 
undeclare s1 statement;
```



## declarecursor

建立数据库查询游标

可以使用 cursor 游标名 loop .. end loop 语句遍历游标对象.

可以使用 fetch cursorName into variableName1, variableName2, variableName3; # 语句将游标中当前行的字段保存到自定义变量中.

可以使用 undeclare 游标名 cursor; 语句关闭游标.

### 语法

```shell
$ declare 游标名 cursor with return for select * from table ;
```

### 示例

```shell
db connect to databasename 
declare cno cursor with return for select * from table ; 
cursor cno loop 
\tfetch cno into tmp_val1, tmp_val2, tmp_val3; 
\techo ${tmp_val1} ${tmp_val2} ${tmp_val3} 
end loop 
undeclare cno cursor;
```



## nohup

后台执行命令

### 语法

```shell
$ nohup 命令语句 [&] [;]
```

### 示例

并行执行脚本

```shell
$ nohup . /home/user/script.sql &
```

后台执行脚本并获取脚本 pid 编号

```shell
$ set pid=`nohup . scriptfile.sql & | tail -n 1`
```



## terminate

终止脚本引擎中所有的用户会话 或 终止脚本引擎中某个用户会话 或 终止当前用户会话中的进程；

脚本引擎会调用命令的 terminate() 方法以尝试终止命令执行。

但是正在运行中的命令是否立即退出，取决于该命令的 terminate() 方法实现。

### 语法

```shell
$ terminate [-p 后台进程编号] [-s 用户会话编号] [;]
```

### 示例

表示终止脚本引擎中所有的用户会话

```shell
$ terminate;
```

表示终止脚本引擎中的一个用户会话 

```shell
$ terminate -s Mc6e4645c26d94666a0a65621078aaeff;
```

表示终止当前用户会话中的一个进程 

```shell
$ terminate -p 21;
```



## wait

等待子进程执行完毕，使用语句 [ 1m | 1s | 1h | 1day ] 设置等待子进程的超时时间, 超时后自动退出。

### 语法

```shell
$ wait pid=进程编号 [ 1m | 1s | 1h | 1day ][;]
```

### 选项

```properties
day       表示单位是自然日 
h         表示单位是小时 
m         表示单位是分钟 
s         表示单位是秒 
millis    表示单位是毫秒
```



## ps

查看进程信息

### 语法

```shell
$ ps [-s] [;]
```

### 选项

```shell
-s 选项表示显示脚本引擎中所有用户会话
```

### 后台进程说明

```properties
{0}       表示进程编号 
{1}       表示脚本语句所在行号  
{2}       表示进程状态：true表示正在运行 
{3}       表示进程终止：true表示已终止进程 
{4}       表示进程启动时间 
{5}       表示进程结束时间 
{6}       表示进程返回值 
{7}       表示进程执行命令 
```

### 用户会话信息说明

```properties
{8}        表示用户会话的编号 
{9}        表示用户会话的父用户会话编号 
{10}        表示用户会话的状态 
{11}        表示用户会话是否已终止 
{12}        表示是当前用户会话信息
```



## sleep

使当前进程进入休眠

### 语法

```shell
$ sleep 1 {day|h|m|s|millis}[;]
```

### 单位

支持的休眠时间的单位有：

```properties
day       表示单位是自然日 
h         表示单位是小时 
m         表示单位是分钟 
s         表示单位是秒 
millis    表示单位是毫秒
```



## stacktrace

打印异常错误信息，打印脚本引擎在执行命令过程中最后一个异常信息，打印格式跟 {0} 的实现有关。 

### 语法

```shell
$ stacktrace[;]
```





## date

日期命令

### 语法

```shell
$ date [-d 日期字符串] { 日期输出后的格式表达式 } [ +|- 数字 day|month|year|hour|minute|second|millis ]* [;]
```

### 选项

```shell
d 设置日期字符串, 可以使用单引号或双引号包住日期字符串
```

### 支持的日期格式

```properties
y+.*MM.*dd  
 
yyyy-MM-dd, e.g: 2017-01-01 || 2017/01/01 || 2017.01.01 # 年月日之间的分隔符可以是以下字符之一: - / | \\ _ : ： . 。 
 
MM/dd/yyyy  
 
yyyy-M-d, e.g: 2017-1-1  
 
yyyyMMdd  
yyyyMMddHH  
yyyyMMddHHmm  
yyyyMMddHHmmss  
yyyyMMddHHmmssSSS  

yyyy-MM-dd hh  
yyyy-MM-dd hh:mm  
yyyy-MM-dd hh:mm:ss  
yyyy-MM-dd hh:mm:ss:SSS  

Sun Oct 11 00:00:00 GMT+08:00 1998  
Sun Oct 11 00:00:00:000 GMT+08:00 1998  

二零一七年十二月二十三  
1998年10月11日  
 
31 december 2017 at 08:38  
31 dec 2017  
```

### 示例

```shell
$ date                        # 输出当前日期时间，显示格式：yyyy-MM-dd hh:mm:ss 
$ date -d 2020-01-01 yyyyMMdd # 格式化指定日期，-d参数值格式详见“支持的日期格式” 
$ date + 1 day                # 当前时间加一天
```



## default

输出或设置脚本引擎的默认命令

脚本引擎的默认命令是指当脚本引擎编译器不能识别脚本命令语句时，默认会使用提前设置的默认命令处理脚本语句。

如当使用 default sql; 命令设置了 SQL 语句为默认命令后，当脚本引擎编译器遇到不能识别的命令语句时会将脚本语句交给默认命令解析并执行。

可以使用 default; 命令查看脚本引擎当前设置的默认命令。

### 语法

```shell
$ default [;]
$ default [sql | os] [;]
```

### 示例

```shell
$ default sql;                # 不能识别语句时默认作为sql执行 
$ default os;                 # 不能识别语句时默认作为本地操作系统命令执行
```



## find

搜索文件

### 语法

```shell
$ find -n string [-r] [-h] [-e charsetName] [-o logfilepath] [-s delimiter] [-d] [-p] filepath [;]
```

### 选项

```properties
-n 搜索内容（可以是正则表达式）  
-R 只遍历当前目录  
-h 查找隐藏文件  
-e 被搜索文件的字符集  
-o 输出文件  
-s 输出信息的分隔符   
-d 去掉重复记录  
-p 显示字符串所在位置的详细信息 
```



## java

执行JAVA类，被执行的 java 类需要继承 icu.etl.script.command.AbstractJavaCommand, 需要实现抽象方法 execute()。

需要通过实现 terminate() 方法终止运行中的命令

可以通过实现 boolean enableJump() 方法通知脚本引擎在执行 jump 命令时是否可以不执行 int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, String[] args) 方法.

### 语法

```shell
$ java JavaClassName [参数]... [;] 
```

### 示例

```shell
$ java icu.etl.script.cmd.JavaCommandTest 10 -c 20200101
```



## uuid

生成唯一32位字符串

### 语法

```shell
$ uuid[;]
```



## md5

生成文件或字符内容的MD5值

### 语法

```shell
$ md5sum 文件名或文件路径
$ md5sum 字符内容
```



## tar

压缩文件或目录

解压文件

### 语法

```shell
$ tar -zcvf 文件名或绝对路径 
$ tar -xvf 文件名或绝对路径
```

### 示例

```shell
压缩文件:  
$ tar -zcvf 文件名或绝对路径  

解压文件: 
$ tar -xvf 文件名或绝对路径
```





## gzip

压缩文件或目录

### 语法

```shell
$ gzip 文件名或绝对路径
```



## gunzip

解压文件

### 语法

```shell
$ gunzip 文件名或绝对路径
```



## zip

压缩文件或目录

### 语法

```shell
$ zip 文件名或绝对路径
```



## unrar

解压文件

### 语法

```shell
$ unrar 文件名或绝对路径
```



## unzip

解压文件

### 语法

```shell
$ unzip 文件名或绝对路径
```



## help

打印脚本引擎帮助信息

### 语法

```shell
$ {help | man} [脚本命令表达式]
```

### 示例

```shell
$ help; help echo; man; man step;
```



# 内置方法



## print

使用脚本引擎标准输出接口输出变量值

### 语法

```
variableName.print()
```

### 参数

无参数

### 返回值

无



## []

返回字符串指定位置的字符，返回数组指定位置的元素

### 语法

```
variableName[index]
```

### 参数

参数名：index，类型：整数，范围：大于等于零且小于字符串长度或数组长度

### 返回值

字符串或数组元素



## substr

截取字符串或数组

### 语法

```
variableName.substr(start, end) 或 variableName.substr(start)
```

### 参数

参数名: start  类型: 整数  范围: 大于等于零 小于字符串长度或数组长度 

参数名: end    类型: 整数  范围: 大于等于零切小于等于 start 参数，截取后的值不包含 end 位置上的值

### 返回值

返回截取后的字符串或数组



## trim

删除字符串或数组中字符串二端的空白字符

### 语法

```
variableName.trim()
```

### 参数

无参数

### 返回值

返回字符串或数组



## ltrim

删除字符串或数组中字符串左边的空白字符

### 语法

```
variableName.ltrim()
```

### 参数

无参数

### 返回值

返回字符串或数组



## rtrim

删除字符串或数组中字符串右边的空白字符

### 语法

```
variableName.rtrim()
```

### 参数

无参数

### 返回值

返回字符串或数组



## length

返回字符串或数组的长度

### 语法

```
variableName.length()
```

### 参数

无参数

### 返回值

```
返回整数
```



## upper

将字符串中的英文字符转为大写字符

### 语法

```
variableName.upper()
```

### 参数

无参数

### 返回值

返回字符串



## lower

将字符串中的英文字符转为小写字符

### 语法

```
variableName.lower()
```

### 参数

无参数

### 返回值

返回字符串



## split

使用分隔符参数与转义字符参数对字符串进行分割

### 语法

```
variableName.split() 或 variableName.split(delimiter) 或 variableName.split(delimiter, escape)
```

### 参数

无参数时，表示默认使用空白字符串作为分隔符分割字符串 
参数名：delimiter  类型：字符串  范围：不能是空白字符 
参数名：escape     类型：字符串  范围：只能是非空的单字符

### 返回值

分割之后的字符串数组



## getfilename

返回文件名（不包含文件目录）

### 语法

```
stringVariableName.getFilename()
```

### 参数

无参数

### 返回值

文件名字符串



## getfilelineseparator

返回文件中的行间分隔符

### 语法

```
stringVariableName.getFileLineSeparator()
```

### 参数

无参数

### 返回值

文件的行间分隔符



## getfileext

返回文件名中的扩展名

### 语法

```
stringVariableName.getFileExt()
```

### 参数

无参数

### 返回值

文件名扩展名字符串



## getfilenamenoext

返回文件名，但不包含扩展名; 文件扩展名: txt 或 exe 或 zip 等

### 语法

```
stringVariableName.getFilenameNoExt()
```

### 参数

无参数

### 返回值

文件名字符串



## getfilesuffix

返回文件名的后缀

### 语法

```
stringVariableName.getFileSuffix()
```

### 参数

无参数

### 返回值

文件名后缀字符串



## getfilenamenosuffix

返回文件名，但不包含文件名后缀; 文件名后缀: tar.gz 或 txt 或 exe

### 语法

```
stringVariableName.getFilenameNoSuffix()
```

### 参数

无参数

### 返回值

文件名字符串



## getparent

返回文件的父目录的绝对路径

### 语法

```
stringVariableName.getParent()
```

### 参数

无参数

### 返回值

父目录的绝对路径字符串



## deletefile

删除文件或目录

### 语法

```
stringVariableName.deleteFile()
```

### 参数

无参数

### 返回值

true表示删除文件或目录成功



## existsfile

判断文件或目录是否存在

### 语法

```
stringVariableName.existsFile()
```

### 参数

无参数

### 返回值

true表示文件或目录存在



## isfile

判断文件是否存在

### 语法

```
stringVariableName.isFile()
```

### 参数

无参数

### 返回值

true表示文件存在



## isdirectory

判断目录是否存在

### 语法

```
stringVariableName.isDirectory()
```

### 参数

无参数

### 返回值

true表示目录存在



## mkdir

创建目录

### 语法

```
stringVariableName.mkdir()
```

### 参数

无参数

### 返回值

true表示创建目录成功



## touch

创建文件

### 语法

```
stringVariableName.touch()
```

### 参数

无参数

### 返回值

true表示创建文件成功


## ls

显示文件详细信息

### 语法

```
stringVariableName.ls()
```

### 参数

无参数

### 返回值

文件详细信息字符串



## format

将日期变量使用 pattern 格式格式化并输出字符串

将日期字符串变量转为日期并按参数 pattern 格式输出日期信息

### 语法

```
dateVariableName.format('yyyy-MM-dd') 或 stringVariableName.format(yyyy-MM-dd)
```

### 参数

参数名：pattern  类型：字符串  范围：日期正则表达式

### 返回值

日期时间字符串



## indexof

搜索字符串参数string 在字符串或字符串数组中首次出现的位置

### 语法

```
variableName.indexOf(string, from) or variableName.indexOf(string)
```

### 参数

参数名: string  类型: 字符串  范围: 必填且不能是null或空字符串 
参数名: from    类型: 整数    范围: 开始搜索的起始位置，选填且大于等于零且小于字符串长度或数组长度

### 返回值

字符串参数首次出现的位置，从0开始，-1表示未找到



## getyear

返回日期的年份

### 语法

```
dateVariableName.getYear()
```

### 参数

无参数

### 返回值

整数



## getmonth

返回日期的月份

### 语法

```
dateVariableName.getmonth()
```

### 参数

无参数

### 返回值

1-12



## getday

返回日期的天数

### 语法

```
dateVariableName.getDay()
```

### 参数

无参数

### 返回值

1-31



## getdays

返回日期从1970年1月1日零点开始直到日期时点的天数

### 语法

```
dateVariableName.getDays()
```

### 参数

无参数

### 返回值

正整数



## gethour

返回日期时间的小时数

### 语法

```
timeVariableName.getHour()
```

### 参数

无参数

### 返回值

0-23



## getminute

返回日期时间的分钟数

### 语法

```
timeVariableName.getMinute()
```

### 参数

无参数

### 返回值

0-59



## getsecond

返回日期时间的秒数

### 语法

```
timeVariableName.getSecond()
```

### 参数

无参数

### 返回值

0-59



## getmillis

返回日期时间的毫秒数

### 语法

```
timeVariableName.getMillis()
```

### 参数

无参数

### 返回值

0-999



## int

将字符串转为整数

### 语法

```
strVariableName.int()
```

### 参数

无参数

### 返回值

整数



## exists

判断数组或集合中是否包含指定变量

### 语法

```
array.exists(variableName)
collection.exists(variableName)
```

### 参数

变量名

### 返回值

返回 boolean 值



# 自定义命令

如果基础命令不能满足需求时，你可以通过自定命令方式对基础命令进行扩展。

自定义命令需要实现 {3} 命令编译器接口，并且需要在接口 {3} 的实现类上使用 {14} 注解对命令进行配置。

在首次使用脚本引擎中的类时，会首先扫描 classpath 下的 class 文件（包括 jar 包中的 class 文件）上的 {14} 注解，并且会判断类是否是实现了接口 {3}，如果满足条件这个类会作为脚本命令进行加载。

如果因为脚本引擎扫描注解消耗时间过长导致启动过慢的话，可以在使用脚本引擎之前通过设置 JVM 虚拟机参数 -D{20}=icu.etl,org.apache,.. 来指定扫描 JAVA 包名的范围以提高脚本引擎启动速度。

脚本引擎首次启动时默认扫描的注解只有如下四种。如果想增加类扫描规则，需要在SPI配置文件 {53} 上配置实现类。如果想指定类扫描器使用的类加载器，可以通过 {54} 来设置。

脚本引擎命令注解：{14} 

脚本引擎变量方法注解：{15}

脚本引擎组件注解：{22}

脚本引擎组件实现类的注解：{19}

自定义命令的实现有二种方法: 

1）实现脚本引擎命令接口：

所有脚本引擎的命令都需要实现接口 {3}，并在接口实现类上配置注解 {14} 标记该类是一个脚本引擎命令类。

脚本引擎在首次启动时会扫描类上的注解，如果类上存在注解 {14}，则加载该类并实例化一个命令工厂实例。

生成的命令工厂的实例对象会交给编译器管理和使用，当编译器在对脚本语句进行编译时，会使用命令工厂的实例对象对脚本语句进行识别，判断脚本语句对应的是哪个命令工厂。

当编译器识别出脚本语句对应的命令工厂后，会调用命令工厂上的 {3}.{52} 方法创建一个命令实例。

编译器会使用命令实例上的 {49} 方法对脚本语句进行词法分析，词法分析是解析当前命令的完整语句（如果语句跨越多行则可以使用词法分析器向下读取多行内容合并成一个完整语句）。

编译器会使用命令实例上的 {50} 方法对脚本语句进行语义分析，语义分析是审查脚本命令有无语义错误，为代码生成阶段收集类型信息。比如语义分析的一个工作是进行类型审查，审查每个算符是否具有语言规范允许的运算对象，当不符合语言规范时，应抛出异常报告错误。

之后脚本引擎会执行命令实例上的 {51} 方法，以执行命令的实际业务逻辑。

2）继承已有的命令模版类： 

脚本引擎提供了一些现成的命令模版类，开发人员可以在模版类的基础上实现自己的业务逻辑，可以大大减少开发工作量。

用继承已有模版方式实现脚本引擎命令时同样需要在命令类上配置注解 {14}。

3）命令模版按用途可以分为：

带日志输出功能的命令模版: {7}

不带日志输出功能的命令模版：{6} 

支持文件操作的命令模版： {8}

支持全局功能的命令模版：{9}

支持主从关系的命令模版类: {10}

4）其他与命令相关接口： 

如下接口都是脚本引擎命令的一些非必要的接口，开发人员可以根据实际需求自主选择是否实现如下接口

{11} 如果想要命令支持管道操作，需要实现该接口。

{12} 如果想要命令支持控制循环体，需要实现该接口。

{13} 如果想要命令支持异步并发运行，需要实现该接口。

5）命令列表如下所示：

{1}



# 自定义方法

变量方法是指在脚本语句中通过使用变量名与变量方法名的方式，访问或修改变量自身值的命令。如：str.trim()

脚本引擎默认提供了一些变量方法以供开发人员使用，如下所示：

{67}

如果以上变量方法不能满足需求，开发人员可以通过自定义变量方法的方式来实现自己的业务逻辑，自定义的变量方法必须实现接口 {5}，且实现类上需要配置 {15} 注解。

{15}.{64} 返回变量方法的名字。

{15}.{65} 返回关键字数组（关键字不能作为变量名使用）。 



# 数据库方言

脚本引擎中数据库相关的操作与命令，都是通过 **JDBC** 接口实现的，在使用数据库相关功能和命令前需要满足如下条件：

- 数据库支持 **JDBC** 驱动，且 **JDBC** 驱动包已加入 classpath 下；

- 数据库有对应的方言类，脚本引擎已有方言如下所示；

  {16}

- 可以通过新建数据库方言类的方式来增加对其他品牌据库的支持；

- 可以针对数据库不同的版本开发不同的数据库方言；

如：现在想增加对 informix 数据库的支持，需要新建一个方言类，且需要在方言类上配置 {19} 注解：

```java
@{19}(name = "informix", description = "informix数据库方言类")
public class InformixDialect extends {18} implements {17} {
	...
}
```

可以通过 `{58}.{57}` 得到数据库对应的方言类，这个方法的第一个参数是 {17}.class，第二个参数可以是一个数据库连接或注解上 {59} {60} 返回值。

优先使用大版本号与小版本号匹配的数据库方言类，如果不能匹配到对应的版本号时，会使用注解中未设置版本号的数据库方言，如果方言注解都设置了版本号则优先返回版本最近的一个方言。



# 表达式说明

支持在 set，if，while 等命令中使用表达式进行计算。

开发人员可以使用类 {63} 完成如下表达式运算：

算数运算 `() +`(正)`-`(负) `*`(乘) `/`(除) `%`(取余) `+`(加) `-`(减) 

三目运算 `?:`

布尔运算 `< <= > >= == !=`

逻辑运算 `&& || and or`

范围运算 `in` 与`not in` 运算符的返回值是布尔值，判断变量是否在设置的范围内，操作符右侧是小括号，小括号内的元素用符号 {68} 分割。

取反运算 `!` 只支持对布尔值进行取反



# 属性信息

程序启动时，默认扫描 {} 包下的类，可以通过如下参数配置扫描特定的包名与jar文件

```java
System.setProperty("tls.includes", "icu.etl,atom.jar");
```

程序启动时，默认使用Slf4j作为日志输出，如果工程中未使用Slf4j，则默认使用System.out作为日志输出，可以通过如下参数配置

```java
System.setProperty("{}", "debug");
```

使用控制台输出日志

```java
System.setProperty("{}", "true");
```

程序启动时，默认使用的字符集编码是 file.encoding，可以通过如下参数配置：

```java
System.setProperty("{}", "UTF-8");
```

设置classpath路径

```java
System.setProperty("{}", "UTF-8");
```

输入流缓存的长度，单位字符

```java
System.setProperty("{}", "UTF-8");
```

外部资源配置文件路径

```java
System.setProperty("{}", "UTF-8");
```



# 设计说明

通用脚本引擎由脚本引擎工厂，脚本引擎配置信息，脚本引擎，脚本引擎上下文信息，编译器，类型转换器，脚本引擎命令，脚本引擎变量方法，数据库方言，国际化资源构成。

脚本引擎工厂：{28} ，是 JDK 脚本引擎工厂接口 {40} 的实现类。

脚本引擎：{25}，是 JDK 脚本引擎接口 {41} 的实现类。

脚本引擎上下文信息：{32}，是 JDK 脚本引擎上下文接口 {42} 的实现类。用于管理脚本引擎运行中产生的变量与程序。

编译器：{30}，编译器用于将外部输入的脚本语句转为JAVA语言代码，由语法分析器 {37}，词法分析器 {38}，语句分析器 {39} 组成。

类型转换器：{33}，用于将 JDBC 查询结果集返回值转为脚本引擎内部使用的类型。

脚本引擎配置信息：{31}，用于管理脚本引擎基本属性信息。

脚本引擎命令：{2}

脚本引擎变量方法：{4}

数据库方言：{34}

国际化资源：{35}，如果需要扩展其他语言可以通过设置 JVM 参数 -D{36}= 来设置外部资源文件绝对路径

脚本引擎支持的接口有：标准信息输出接口，错误信息输出接口，进度信息输出接口。

