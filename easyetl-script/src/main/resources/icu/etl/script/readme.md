[TOC]







# 简介

通用脚本引擎是一个用 Java 语言编写的脚本引擎工具，可以执行SQL脚本、装载数据文件到数据库表中、从数据库表中卸载数据文件、对数据文件执行剥离增量、登陆远程服务器执行shell命令等功能，可以自定义命令来扩展功能。

通用脚本引擎的功能是动态解释执行脚本命令或脚本文件，将脚本命令译成 JAVA 语言代码来完成相应功能。

通用脚本引擎在执行脚本语句时不需要预编译步骤，直接动态解释运行脚本命令语句。

通用脚本引擎依据每个命令的返回值（整数）来判断命令运行的是否成功。

如果脚本命令的返回值是零，表示运行成功，脚本引擎继续读取下一个命令并运行！ 

如果脚本命令的返回值是非零时，表示命令运行失败，脚本引擎会立即终止并退出！

通用脚本引擎框架支持多线程并发运行脚本语句。








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

引用了场景启动器后，在编写脚本语句时可以直接使用 `application.propertes` 与 `application.yaml` 配置文件（包括分环境配置文件）中定义的属性作为（环境）变量使用。

可以通过 **Spring** 注入机制，来得到一个脚本引擎 `ScriptEngine` 的实例对象。



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

    /** 注入脚本引擎实例，request范围有效 */
    @Autowired
    private ScriptEngine engine;

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

可以作为存储过程使用，首先建立脚本文件 `test_procedure.sql` 内容如下所示： 

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
        ScriptEngineManager manager = new ScriptEngineManager();
        try {
            ScriptEngine engine = manager.getEngineByExtension("etl");
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
$ ssh ${admin}@${host}:22?password=${adminPw} \
&& export LANG=zh_CN.GBK \
&& db2 connect to ${databaseName} user ${username} using ${password} \
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







# 脚本命令



## 简介

通用脚本引擎默认提供了一套内置的基础命令（如 echo、set、help 等命令），这些基础命令无需设置或配置就可以直接使用。

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





## 已注册命令

{55}





## 自定义命令

定义命令需要实现 {3} 命令编译器接口，并且需要在接口 {3} 的实现类上使用 {14} 注解对命令进行配置。

在首次使用脚本引擎中的类时，会首先扫描 `classpath` 下的 `class` 文件（包括 jar 包中的 class 文件）上的 {14} 注解，并且会判断类是否是实现了接口 {3}，如果满足条件这个类会作为脚本命令进行加载。

如果因为脚本引擎扫描注解消耗时间过长导致启动过慢的话，可以在使用脚本引擎之前通过设置 **JVM** 虚拟机参数 

`-D{20}=icu.etl,org.apache` 来指定扫描 JAVA 包名的范围以提高脚本引擎启动速度。

脚本引擎首次启动时默认扫描的注解只有如下四种。如果想增加类扫描规则，需要在SPI配置文件 {53} 上配置实现类。如果想指定类扫描器使用的类加载器，可以通过 {54} 来设置。

脚本引擎命令注解：{14}

脚本引擎变量方法注解：{15}

脚本引擎组件注解：{22}

脚本引擎组件实现类的注解：{19}



自定义命令的实现有二种方法：

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

{11} 如果想要命令支持管道操作，需要命令实现该接口。

{12} 如果想要命令支持控制循环体，需要实现该接口。

{13} 如果想要命令支持异步并发运行，需要实现该接口。

5）命令列表如下所示：

{1}



# 变量方法

## 简介

变量方法是指在脚本语句中，通过变量名与变量方法名的方式，访问或修改变量自身值的命令。

如下所示：

```javascript
set str = "12345 ";
set strtrim = str.trim();
echo "字符串内容是 $strtrim .."
```



## 内置变量

脚本引擎内部定义了一些内部使用的变量



### {4}

如果正在执行脚本文件，{4} 表示脚本文件所在目录的绝对路径。



### {5}

如果正在执行脚本文件, {5} 表示脚本文件的名字



### {6}

如果正在执行脚本文件, {7} 表示脚本文件的字符集编码



### {8}

如果正在执行脚本文件, {9} 表示脚本文件中的行间分隔符



### {10}

如果脚本引擎发生异常, {10} 表示异常详细信息



### {11}

如果脚本引擎发生异常, {11} 表示报错的语句



### {12}

如果脚本引擎发生数据库错误, {12} 表示数据库厂商定义的错误码



### {13}

如果脚本引擎发生数据库错误, {13} 表示数据库厂商定义的SQL状态码



### {14}

表示最近一次执行语句的返回值



### {15}

表示最近一次执行sql语句影响的数据记录数 



### {16}

表示当前是否处于 jump 语句中. 如果 jump 变量等于 true 表示脚本引擎正处于 jump 语句中



### {17}

表示当前步骤变量值，即最近一个 step 命令的参数值



### {18}

表示临时文件所在目录



### {19}

如果正在执行脚本文件, 表示当前脚本文件的绝对路径



### {20}

局部变量名，用于存储当前数据库编目名



## 已注册变量方法

脚本引擎中已注册的变量方法如下所示：

{56}



## 自定义变量方法

可以自定义变量方法，自定义的变量方法必须实现接口 {54}，且实现类上要配置 {52} 注解。

{15}.{64} 返回变量方法的名字。

{15}.{65} 返回关键字数组（关键字不能作为变量名使用）。 

```java
import java.util.List;

import icu.etl.annotation.ScriptFunction;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;

@ScriptFunction(name = "add")
public class AddMethod extends AbstractMethod {

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptAnalysis analysis, String variableName, String methodHandle) throws Exception {
        int funcStart = "add(".length();
        int funcEnd = analysis.indexOf(methodHandle, ")", funcStart, 2, 2);
        if (funcEnd == -1) {
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        Integer value = (Integer) session.getMethodVariable(variableName);
        String parameters = methodHandle.substring(funcStart, funcEnd);
        List<String> array = analysis.split(parameters);
        for (String str : array) {
            value += Integer.parseInt(str);
        }
        this.value = value;

        int next = funcEnd + 1;
        return this.executeNextMethod(session, context, stdout, stderr, analysis, variableName, methodHandle, value, next);
    }

}
```



# 数据库方言

## 简介

在脚本引擎中数据库相关的命令，都是通过 **JDBC** 接口实现的。

在使用数据库相关命令前，需要数据库支持 **JDBC** 驱动，且要将 **JDBC** 驱动包加入到 **classpath** 下。

因为不同品牌的数据库，其功能语法各不相同，所以要为不同品牌的数据库开发不同的方言接口的实现类。

这样我们就可以通过一个统一一致的数据库方言接口，来操作不同品牌的数据库。



## 已有方言类

在脚本引擎中已注册的数据库方言类如下所示：

{27}



## 开发方言类

可以通过新建数据库方言类的方式来增加对其他品牌据库的支持。

例如想要增加对 `informix` 数据库的支持，如下所示需要新建并实现数据库方言类，且在该类上配置注解 `{29}` 。

```java
@{22}(name = "informix")
public class InformixDialect extends {25} implements {26} {
	...
}
```

也可以针对同一数据库的不同版本，开发对应的数据库方言类。

因为在同一个品牌数据库的不同版本中，同一个功能的实现也可能不同，这时可针对数据库的特殊版本增加方言类，如下所示：

```java
@{22}(name = "db2")
public class DB2Dialect115 extends {33} implements {30} {

    /**
     * 大版本号，对应 DatabaseMetaData.getDatabaseMajorVersion() 的返回值
     */
    public String getDatabaseMajorVersion() {
        return "11";
    }

    /**
     * 小版本号，对应 DatabaseMetaData.getDatabaseMinorVersion() 的返回值
     */
    public String getDatabaseMinorVersion() {
        return "5";
    }
}
```



## 使用方言类

在编写代码时，可通过容器的 `{28}` 方法得到数据库方言的实例对象，如下所示：

```java
@{22}
public class JdbcTest1 {

    /** 容器上下文信息 */
    private {32} context;
  
    /** 数据库连接 */
    private Connection conn;

    /**
     * 初始化
     *
     * @param context   容器上下文信息
     * @param conn      数据库连接
     */
    public JdbcTest1({32} context, Connection conn) {
        this.context = context;
        this.conn = conn;
    }

    /**
     * 返回数据库方言
     *
     * @return 数据库方言
     */
    public {30} getDialect() {
        return this.context.getBean({30}.class, this.conn);
    }
}
```

在上面这个案例中，容器方法 `{28}` 的第一个参数是数据库方言接口，第二个参数是一个有效的数据库连接。

容器会根据数据库连接信息中的数据库缩写与版本号，查找对应的数据库方言。

```java
@{22}
public class JdbcTest2 {

    /** 容器上下文信息 */
    @{22}
    private {32} context;

    /** JDBC URL */
    private String url = "jdbc:db2://127.0.0.1:50000/sample";

    /**
     * 初始化
     */
    public JdbcTest2() {
    }

    /**
     * 返回数据库方言
     *
     * @return 数据库方言
     */
    public {30} getDialect() {
        return this.context.getBean({30}.class, this.url);
    }
}
```

在上面这个案例中，容器自动注入了容器上下文信息，即 `context` 实例。

容器方法 `{28}` 的第一个参数是数据库方言接口，第二个参数是 **JDBC** 的 **URL**，容器会根据URL中的数据库信息，查找（规则详见 {31}）对应的数据库方言。



## 匹配规则

先根据数据库简称，便利所有已注册的数据库方言类上 `@{22}` 中的 `name()	` 值，来查找相同的方言类。

如果在上一步查找匹配到多个数据库方言类（存在不同版本的方言类），则优先使用大版本号、小版本号与数据库进行匹配；

如果在上一步中，不能匹配到对应版本号的方言类时，则会使用未设置版本号（即`{30}.getDatabaseMajorVersion()`与`{30}.getDatabaseMinorVersion()`方法返回null或空字符）的数据库方言。

如果在上一步中，所有数据库方言类都设置了版本号，则优先返回版本号最接近的方言类；

具体的匹配规则详见 `{31}`



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

脚本引擎由脚本引擎工厂，脚本引擎配置信息，脚本引擎，脚本引擎上下文信息，编译器，类型转换器，脚本引擎命令，脚本引擎变量方法，数据库方言，国际化资源构成。

脚本引擎工厂 {28} 用于创建脚本引擎实对象。

脚本引擎 {25} 用于提供脚本引擎操作接口。

脚本引擎上下文信息 {32} 用于管理脚本引擎运行中产生的变量与程序。

编译器：{30}，用于将外部输入的脚本语句转为**JAVA**代码，其是由语法分析器 {37}，词法分析器 {38}，语句分析器 {39} 组成。

类型转换器：{33}，用于将 JDBC 查询结果集返回值转为脚本引擎内部使用的类型。

脚本引擎配置信息：{31}，用于管理脚本引擎基本属性信息。

脚本引擎命令：{2}

脚本引擎变量方法：{4}

数据库方言：{34}，用于提供统一的数据库操作接口。

国际化资源：{35}，如果需要扩展其他语言可以通过设置 JVM 参数 -D{36}= 来设置外部资源文件绝对路径

脚本引擎支持的接口有：标准信息输出接口，错误信息输出接口，进度信息输出接口。

