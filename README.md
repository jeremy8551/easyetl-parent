# 框架简介

这是一个在 Java8上运行的ETL工具，可以执行SQL脚本、ETL操作、执行shell、远程执行shell等，甚至可以自定义一个新脚本引擎。



# 使用方法

在你自己的工程pom.xml中引入依赖：<br/>

```xml
<dependency>
  <groupId>icu.etl</groupId>
  <artifactId>easyetl</artifactId>
  <version>1.0.9</version>
</dependency>
```

入门程序：

```java
public class Main {
    public static void main(String[] args) {
        ScriptEngineManager e = new ScriptEngineManager();
        ScriptEngine engine = null;
        try {
            engine = e.getEngineByExtension("usl");
            engine.eval("help");
            engine.eval("exit 0");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
```



# 配置

程序启动时，默认扫描所有jar文件与class文件，可以通过如下参数配置扫描特定的包名与jar文件

```java
System.setProperty("tls.includes", "icu.etl,atom.jar");
```

程序启动时，默认使用Slf4j作为日志输出，如果工程中未使用Slf4j，则默认使用System.out作为日志输出，可以通过如下参数配置

```java
System.setProperty("tls.logger", "debug");
```

程序启动时，默认使用的字符集编码是 file.encoding，可以通过如下参数配置：

```java
System.setProperty("tls.charset", "UTF-8");
```

