# 功能介绍

通用脚本引擎（以下简称脚本引擎）是一个纯 **Java** 语言编写的脚本引擎工具，功能如下：

- 可以执行SQL脚本（DML与DDL语句）；
- 将数据文件装载到数据库表中；
- 卸载数据库表中数据到指定文件中；
- 对（csv、del、fex，可自定义扩展文件格式）数据文件执行剥离增量（新增记录、修改记录、删除记录）；
- 登陆远程服务器执行shell命令等功能；
- 支持多线程并发运行；
- 可以自定义命令来扩展功能；
- 支持**JDK5**及以上版本；




# 使用方法



## 引入依赖

在 **POM** 引入依赖

```xml
<dependency>
  <groupId>{0}</groupId>
  <artifactId>{1}</artifactId>
  <version>{2}</version>
</dependency>
```



## 使用示例

JDK接口调用


```java
public class Main {
    public static void main(String[] args) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("etl");
        engine.eval("echo hello world!");
    }
}
```

编程式调用


```java
public class Main {
		public static void main(String[] args) {
        EasyContext context = new EasyBeanContext();
        UniversalScriptEngineFactory factory = new UniversalScriptEngineFactory(context);
        UniversalScriptEngine engine = factory.getScriptEngine();
        engine.eval("echo hello world!");
    }
}
```



##  SpringBoot场景启动器



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
    private ScriptEngine engine;
  
    @Autowired
    private EasyContext context;

    @RequestMapping("/help")
    @ResponseBody
    public String help() throws ScriptException, IOException {
      engine.eval("echo hello world!");
			return "success";
    }
}
```



### 功能介绍

引用场景启动器后，可简化脚本引擎的使用，无需配置直接使用。

脚本引擎的场景启动器会随着 **Springboot** 项目一起启动。

可简化脚本引擎容器的创建和使用，在 **SpringBoot** 项目启动时，会初始化一个脚本引擎容器（存储组件信息）。

在上一步得到的脚本引擎容器，会交给 **Spring** 容器管理，可通过 **Spring** 注入机制，得到这个 `{28}` 单例对象。

场景启动器简化了脚本引擎的创建和使用，可以通过 **Spring** 注入机制，得到脚本引擎 `ScriptEngine` 的实例对象。

每次请求，都会创建一个新的脚本引擎实例对象 `{91}`，且这个实例 **Bean** 的 `scope` 属性是 `request`。

在编写脚本语句时，可直接使用 **SpringBoot** 配置文件中的属性作为（环境）变量使用。

在编写脚本语句时，可使用 `application.propertes`、 `application.yaml`  配置文件（包括分环境配置文件）中定义的属性。

可以与 **Spring** 共用同一个线程池，以防止线程的无序扩张使用，公用线程池的方法：

- 可在 **Spring** 容器中定义一个名为 `taskExecutor` 的线程池；
- 可在 **Spring** 容器中定义一个 `ThreadPoolTaskExecutor` 类型的线程池；
- 可在 **Spring** 容器中定义一个 `ExecutorService` 类型的线程池；



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
    public void test() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("etl");
        engine.eval(". classpath:/script/test_etl.sql");
    }
}
```



## 存储过程示例

可以作为存储过程使用，首先建立脚本文件 `/script/test_procedure.sql`，内容如下所示： 

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
    public void test() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("etl");
        engine.eval(". classpath:/script/test_produce.sql");
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

提供了一套内置的基础命令，这些命令无需配置就可以直接使用。

基础命令可按功能划分为：

基本命令：echo，pwd，export，exit，default，grep 等。

声明类命令：以 declare 开头的命令。

逻辑控制类命令：if，while，for，break，continue，step，jump 等。

数据库类命令：select，insert，delete，update，merge, db 等。

网络类命令：os，ftp，sftp 等。

文件类命令：cd，touch，rm，mkdir，cat，head，tail，tar，zip，gunzip，rar 等。

日期类命令：date

线程类命令：container，sleep，nohup，wait 等。

管道符操作：|

命令代换符操作：``



## 已注册命令

{55}



## 自定义命令

有二种方法可以实现自定义命令：



### 实现脚本命令接口

自定义命令需要实现 `{93}` 接口，并且要在实现类上配置 `{50}` 注解。



### 继承命令模版类

脚本引擎提供了一些命令模版类，开发人员可以在模版类的基础上实现自己的业务逻辑，可以大大减少开发工作量。

用继承已有模版方式实现脚本引擎命令时，需要在命令类上配置注解 `{50}`。

命令模版按用途可以分为：

- 带日志输出功能的命令模版：{7}
- 不带日志输出功能的命令模版：{6} 
- 支持文件操作的命令模版：{8}
- 支持全局功能的命令模版：{9}
- 支持主从关系的命令模版类：{10}

其他相关的接口： 

- 脚本命令可以通过实现 `{11}` 接口来支持管道操作；
- 脚本命令可以通过实现 `{12}` 接口来支持控制循环体；
- 脚本命令可以通过实现 `{13}` 接口来支持异步并发运行；

如上所示的接口都是非必要的，可根据实际需求自主选择是否实现这些接口。



# 变量方法



## 简介

变量方法是指在脚本语句中，通过变量名与变量方法名的方式，访问或修改变量自身值的命令。

如下所示 `str.trim()` 就是变量方法：

```javascript
set str = "12345 ";
set strtrim = str.trim();
echo "字符串内容是 $strtrim .."
```



## 内置变量

如下系统默认提供了一些内部使用的变量。



### {4}

表示当前目录的绝对路径。



### {5}

如果正在执行脚本文件, {5} 表示脚本文件的名字



### {6}

如果正在执行脚本文件, {7} 表示脚本文件与外部文件（如 `tail`、`head` 等命令的输入文件）的字符集编码。



### {8}

表示脚本文件中的行间分隔符 `{9}`。



### {10}

表示上一次发生异常的堆栈信息。



### {11}

表示上一次发生异常的脚本语句。



### {12}

表示数据库厂商定义的异常信息错误码。



### {13}

表示数据库厂商定义的异常信息SQL状态码。



### {14}

表示最近一次执行脚本语句的返回状态。



### {15}

表示最近一次执行的 **SQL** 语句影响的数据记录数。

如果 **SQL** 是 `delete` 语句，就表示删除记录数。

如果 **SQL** 是 `update` 语句，就表示更新记录数。

如果 **SQL** 是 `insert` 语句，就表示新增记录数。

如果 **SQL** 是 `select` 语句，就返回零。



### {16}

表示当前是否处于 `jump` 语句中，如果 `jump` 变量值等于 true，则表示脚本引擎正处于 `jump` 语句中。



### {17}

表示上一个 `step` 命令的参数值。



### {18}

表示临时文件的存储目录。



### {19}

表示当前运行中的脚本文件（如果是脚本文件的话）的绝对路径。



### {20}

用于存储当前数据库的编目名。



## 已注册变量方法



{56}



## 自定义变量方法

自定义变量方法需要实现接口 `{54}`，且实现类上要配置注解 `{52}` 。

`{53}.{57}` 返回变量方法的名字。

`{53}.{58}` 返回关键字数组（关键字不能作为变量名使用）。 

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



# 内部设计



## 脚本引擎容器

脚本引擎容器类似于 **Spring** 容器的概念，主要用来管理系统内部组件与外部注册组件。

脚本引擎启动前，需要先初始化启动一个脚本引擎容器 `{28}` 实例对象。

脚本引擎容器包含：类扫描器、组件信息、组件信息表、组件工厂、组件工厂管理器、容器工厂、容器管理器。



### 类扫描器

当脚本引擎容器启动时，会使用类扫描器扫描 `{34}` 包下的类文件，将扫描到的类信息存储到**组件信息表**中。

类扫描器的实现类是 `{107}`，扫描规则详见 `{108}` 接口的实现类，可通过如下参数指定扫描规则：

```java
System.setProperty("{70}", "{0},!org.apache"); // 包名前面使用叹号，表示排除该包名下的所有类
```

类扫描器默认只会扫描被注解 `{22}`、`{51}`、`{53}` 标记的类，如果想增加扫描规则，则可以在**SPI**配置文件 `resources/META-INF/services/{102}` 中增加 `{108}` 接口的实现类。



### 组件信息

组件信息接口是：`{38}`

类扫描器扫描到一个被注解 `{29}` 标记的类后，会将该类信息转为接口 `{38}` 的实例对象。

随后容器会对所有必要的组件进行初始化操作，然后等待其他功能调用组件。



### 组件信息表

组件信息表类是：`{36}`

用于存储容器中所有组件信息的数据结构，存储格式是组件类上的接口类信息与组件类信息的映射。

从脚本引擎容器中查找指定组件时，会根据接口或类信息在组件信息表中查找对应的组件。



### 组件工厂

组件工厂接口是：`{38}`

如果有组件需要使用工厂模式创建，则可以在组件工厂接口 `{38}` 实现类上并标注注解 `{22}`。

脚本引擎容器会使用这个组件工厂实例来创建该组件。



## 脚本引擎工厂 

脚本引擎工厂接口是 `{90}`，用于创建脚本引擎实对象。



## 脚本引擎配置信息

脚本引擎配置信息接口是  `{98}`，用于管理脚本引擎基本属性信息。



## 脚本引擎

脚本引擎类是 `{91}`，是所有脚本语句的运行器。



## 脚本引擎上下文信息 

脚本引擎上下文信息类是 `{92}`，用于管理脚本引擎运行中产生的变量与程序。



## 编译脚本

脚本引擎编译器接口是 `{109}`，可以将脚本语句编译为可以执行的脚本命令。

编译器由语法分析器 `{94}`、语句输入流 `{95}`、语句分析器 `{96}` 、命令编译器 `{93}` 组成。

编译过程：

- 在类扫描阶段，如果一个类上配置了 `{50}` 注解，并且该类也实现了接口 `{93}`，则将这个（命令编译器）类实例化；
- 将上一步生成的命令编译器的实例对象，交给脚本引擎编译器管理和使用；
- 当脚本引擎编译器在对某一个脚本语句进行编译时，会逐个执行命令编译器实例上的 `{106}` 方法，来判断脚本语句应该使用哪个命令编译器来执行编译操作；
- 找出脚本语句对应的命令编译器后，会执行该命令编译器上的 `{103}` 方法来读取一个完整的语句；
- 再执行命令编译器上的 `{104}` 方法，对上一步得到的脚本语句进行编译，得到一个脚本命令实例；



## 运行命令

- 经过脚本引擎编译器编译后，会得到一个脚本命令对象（即 `{99}` 接口的实例对象）；

- 运行实例上的 `{105}` 方法（即执行该命令的业务逻辑），会得到一个返回值与状态码；

- 根据上一步得到的状态码，判断该命令运行的是否成功；
  
  如果状态码是零，表示命令运行成功，读取下一个命令并运行；
  
  如果状态码是非零，表示命令运行错误，立即抛出异常（可用 `set -E` 命令来设置不抛出异常）；



## 国际化信息

国际化资源操作类是 `{101}`，可使用如下代码，来设置自定义的资源文件。

```java
System.setProperty("{75}", "/home/user/../resouce.properties");
```



## 表达式支持

支持在 `set`，`if`，`while` 等命令中使用表达式进行计算。

开发人员可以使用类 `{63}` 完成如下表达式运算：

算数运算 `() +`(正)`-`(负) `*`(乘) `/`(除) `%`(取余) `+`(加) `-`(减) 

三目运算 `?:`

布尔运算 `< <= > >= == !=`

逻辑运算 `&& || and or`

范围运算 `in` 与`not in` 运算符的返回值是布尔值，判断变量是否在设置的范围内，操作符右侧是小括号，小括号内的元素用符号 {68} 分割。

取反运算 `!` 只支持对布尔值进行取反



## 日志输出

脚本引擎容器启动时，会检查类路径下是否有**Slf4j**日志组件。

如果在类路径中检测到**Slf4j**相关jar包，则直接使用**Slf4j**作为日志输出接口。

如果在类路径中未检测到**Slf4j**相关jar包，则默认使用控制台 `System.out` 输出日志。

当使用控制台输出日志时，可配置如下参数，来设置日志输出级别与输出格式。

```java
System.setProperty("{71}", "debug"); // 设置日志输出级别
System.setProperty("{72}", "{81}"); // 设置用控制台输出日志，且指定了日志的输出格式
```

设置打印日志堆栈跟踪信息（用于确定 `fqcn` 值）：

```java
System.setProperty("{78}", "true");
```

设置打印数据库操作日志（可以显示执行的类名、方法名、参数值、返回值）：

```java
System.setProperty("{79}", "true");
```



## 字符集设置

脚本引擎内部默认使用的字符集编码是 `file.encoding` 属性值，也可通过如下参数修改默认的字符集：

```java
System.setProperty("{73}", "UTF-8");
```

可以通过脚本命令来设置脚本文件、外部文件（如 `tail`、`head` 等命令的输入文件）的字符集：

```shell
set {6}=GBK
. /home/user/../script.etl
```



## 临时文件

脚本引擎产生的临时文件，默认存储在 `{35}` 路径下，可通过如下参数修改默认路径：

```java
System.setProperty("{77}", "/home/user/temp/");
```

在编程时可通过如下代码，得到临时文件的存储目录：

```java
{120}.{121};
```



## 数据库方言

因为不同品牌数据库（或同品牌不同版本），其语法与功能实现各不相同，可通过数据库方言接口 `{26}` 来统一操作数据库的接口。但是需要为不同品牌（或同品牌的不同版本）的数据库开发不同的方言实现类。

在脚本引擎中数据库相关的命令，都是通过 **JDBC** 接口实现的。

在使用数据库相关命令前，需要先将数据库的 **JDBC** 驱动包加入到 **classpath** 下。



### 已有方言类

已注册的数据库方言如下所示：

{27}



### 开发方言类

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



### 使用方言类

在编写代码时，可通过容器的 `{28}.{39}` 方法得到数据库方言的实例对象，如下所示：

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

在上面这个案例中，容器方法 `{28}.{39}` 的第一个参数是数据库方言接口，第二个参数是一个有效的数据库连接。

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

容器方法 `{28}.{39}` 的第一个参数是数据库方言接口，第二个参数是 **JDBC** 的 **URL**，容器会根据URL中的数据库信息，查找（规则详见 {31}）对应的数据库方言。



### 匹配规则

先根据数据库简称，便利所有已注册的数据库方言类上 `@{22}` 中的 `name()	` 值，来查找相同的方言类。

如果在上一步查找匹配到多个数据库方言类（存在不同版本的方言类），则优先使用大版本号、小版本号与数据库进行匹配；

如果在上一步中，不能匹配到对应版本号的方言类时，则会使用未设置版本号（即`{30}.getDatabaseMajorVersion()`与`{30}.getDatabaseMinorVersion()`方法返回null或空字符）的数据库方言。

如果在上一步中，所有数据库方言类都设置了版本号，则优先返回版本号最接近的方言类；

具体的匹配规则详见 `{31}`



## 输出接口

- 标准信息输出接口；
- 错误信息输出接口；
- 进度信息输出接口；



## 类型转换器

类型转换器  `{97}`，用于将 JDBC 查询结果集返回值转为脚本引擎内部使用的类型。



## 其他配置

设置 **classpath** 路径

```java
System.setProperty("{74}", "/home/user/cp/..");
```

输入流缓存的长度，单位字符

```java
System.setProperty("{76}", "10000");
```

设置**Linux**操作系统内置账户名（用于过滤操作系统内置账户信息）：

```java
System.setProperty("{80}", "daemon,apache");
```



## 组件附录

脚本引擎容器中已注册的组件如下所示：



{150}

