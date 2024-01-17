package icu.etl.script.command;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import icu.etl.annotation.EasyBean;
import icu.etl.annotation.ScriptCommand;
import icu.etl.database.Jdbc;
import icu.etl.database.JdbcObjectConverter;
import icu.etl.database.export.ExtractWriter;
import icu.etl.database.export.ExtractUserListener;
import icu.etl.database.internal.StandardJdbcConverterMapper;
import icu.etl.database.load.IndexMode;
import icu.etl.database.load.LoadEngineContext;
import icu.etl.database.load.LoadEngineLaunch;
import icu.etl.database.load.LoadMode;
import icu.etl.expression.DataUnitExpression;
import icu.etl.expression.WordIterator;
import icu.etl.io.TextTable;
import icu.etl.io.TextTableFile;
import icu.etl.ioc.EasyBeanInfo;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.ProgressMap;
import icu.etl.script.internal.ScriptDataSource;
import icu.etl.script.internal.ScriptProgress;
import icu.etl.script.internal.ScriptUsage;
import icu.etl.util.CharTable;
import icu.etl.util.ClassUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptCommand(name = "db", keywords = {})
public class DBLoadCommandCompiler extends AbstractTraceCommandCompiler {

    public final static String REGEX = "^(?i)db\\s+load\\s+from\\s+.*";

    private Pattern pattern = Pattern.compile(REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    public int match(String name, String script) {
        return pattern.matcher(script).find() ? 0 : 2;
    }

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readMultilineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws IOException {
        ScriptDataSource dataSource = ScriptDataSource.get(context);
        DBLoadCommand cmd = new DBLoadCommand(this, orginalScript);
        LoadEngineContext cxt = cmd.getContext();
        cxt.setStatistics(false);
        cxt.setNorepeat(false);
        cxt.setSavecount(10000);

        CommandAttribute attrs = new CommandAttribute( //
                "charset:", // 文件字符集
                "codepage:", // 文件代码页
                "rowdel:", // 行间分隔符
                "coldel:", // 字段分隔符
                "escape:", // 转义字符
                "chardel:", // 字符串二端的限定符
                "column:", // 字段个数
                "colname:", // 字段名
                "readbuf:", // 读数据文件时使用的缓存区大小
                "catalog:", // 脚本引擎中数据库编目编号
                "tableCatalog:", // 数据库中编目信息
                "launch:", // 装数引擎启动条件，属性值可以是类名或脚本语句
                "convert:", // 字段转换规则类
                "savecount:", // 建立一致点的笔数
                "dateformat:", // 日期格式
                "timeformat:", // 时间格式
                "timestampformat:", // 时间戳格式
                "keepblanks", // 保留字段中的空白字符
                "message:", // 装数消息文件
                "nocrlf", // 自动删除字符串中的回车或换行符
                "dumpfile:", // 数据装载失败的行
                "progress:", // 指定脚本引擎中的进度输出组件编号
                "thread:" // 指定多个线程分段读取数据文件并装载到数据库表中
        );
        cxt.setAttributes(attrs);

        WordIterator it = analysis.parse(analysis.replaceShellVariable(session, context, command, false, true, true, false));
        it.assertNext("db");
        it.assertNext("load");
        if (it.isNext("client")) {
            it.assertNext("client");
        }
        it.assertNext("from");
        String filepath = analysis.unQuotation(it.readUntil("of"));
        cxt.setFiles(analysis.split(analysis.unQuotation(filepath), analysis.getSegment())); // 设置文件路径与文件类型
        cxt.setFiletype(it.next());

        // method p(1,2,3) 表示文件中字段与数据库表中字段映射关系
        // method c(name1, name2, name3) 表示 merge 语句的关联字段
        if (it.isNext("method")) {
            it.assertNext("method");

            boolean pb = false, cb = false;
            String next = null;
            while ((next = it.previewNext()) != null && ((pb = analysis.startsWith(next, "p(", 0, false)) || (cb = analysis.startsWith(next, "c(", 0, false)))) {
                if (!next.endsWith(")")) {
                    throw new IOException(ResourcesUtils.getScriptStderrMessage(139, next));
                }

                it.next();
                String columnExpr = next.substring(2, next.length() - 1); // 截取小括号中的字段或位置信息
                List<String> columns = analysis.split(columnExpr, analysis.getSegment()); // 提取小括号中的字段名或位置信息
                if (columns.isEmpty()) {
                    throw new IOException(ResourcesUtils.getScriptStderrMessage(141, next));
                }

                // 解析 p(..) 语句
                if (pb) {
                    if (cxt.getFileColumn() != null && cxt.getFileColumn().size() > 0) {
                        throw new IOException(ResourcesUtils.getScriptStderrMessage(142, command));
                    } else {
                        cxt.setFileColumn(columns);
                    }
                }

                // 解析 c(..) 语句
                if (cb) {
                    if (cxt.getIndexColumn() != null && cxt.getIndexColumn().size() > 0) {
                        throw new IOException(ResourcesUtils.getScriptStderrMessage(143, command));
                    } else {
                        cxt.setIndexColumn(columns);
                    }
                }
            }
        }

        // 解析 modified by ...
        if (it.isNext("modified")) {
            it.assertNext("modified");
            it.assertNext("by");

            // 逐个读取属性，直到遇见 replace insert 等装载模式
            while (!LoadMode.isMode(it.previewNext())) {
                String next = it.next();
                String[] array = StringUtils.splitProperty(analysis.unQuotation(next));

                String key = null; // 属性名
                String value = null; // 属性值
                if (array == null) { // 只有属性名
                    key = next;
                    value = "";
                } else { // 有属性名和属性值
                    key = array[0];
                    value = analysis.unQuotation(array[1]);
                }

                // 设置建立一致点的笔数
                if (analysis.equals(key, "savecount")) {
                    long saveCount = Long.parseLong(value);
                    cxt.setSavecount(saveCount);
                    continue;
                }

                // 设置读取文件的输入流缓冲区长度
                if (analysis.equals(key, "readbuf")) {
                    int readBuffer = DataUnitExpression.parse(value).intValue();
                    cxt.setReadBuffer(readBuffer);
                    continue;
                }

                // 设置数据库连接池
                if (analysis.equals(key, "catalog")) {
                    DataSource pool = dataSource.getPool(value);
                    cxt.setDataSource(pool);
                    continue;
                }

                // 设置数据库表所在数据库编目信息
                if (analysis.equals(key, "tableCatalog")) {
                    if (StringUtils.isBlank(value)) {
                        cxt.setTableCatalog(null);
                    } else {
                        cxt.setTableCatalog(value);
                    }
                    continue;
                }

                // 设置进度输出接口
                if (analysis.equals(key, "progress")) {
                    ScriptProgress progress = ProgressMap.getProgress(context, value);
                    if (progress == null) {
                        throw new IOException(ResourcesUtils.getScriptStderrMessage(144, value));
                    } else {
                        cxt.setProgress(progress);
                        continue;
                    }
                }

                // 设置启动条件
                if (analysis.equals(key, "launch")) {
                    String expr = StringUtils.trimBlank(analysis.unQuotation(value));
                    Class<LoadEngineLaunch> cls = ClassUtils.forName(expr, true, context.getContainer().getClassLoader());
                    if (cls == null) {
                        cmd.setRule(expr);
                    } else {
                        cmd.setRule((LoadEngineLaunch) context.getContainer().createBean(cls));
                    }
                    continue;
                }

                // 设置用户自定义的类型转换器
                if (analysis.equals(key, "convert")) {
                    String delimiter = String.valueOf(analysis.getSegment());
                    StandardJdbcConverterMapper mapper = new StandardJdbcConverterMapper(value, delimiter, ":");
                    cxt.setConverters(mapper);
                    continue;
                }

                // 保存属性信息
                attrs.setAttribute(key, value);
            }
        }

        // 设置默认的数据库连接池
        if (cxt.getDataSource() == null) {
            cxt.setDataSource(dataSource.getPool());
        }

        cxt.setLoadMode(LoadMode.valueof(it.next())); // 解析装数模式 insert replace merge
        it.assertNext("into");

        // 解析数据库表名 schema.tableName(..)
        String targetExpr = it.next();
        int begin = targetExpr.indexOf('('); // 判断是否通过小括号设置表中字段装载顺序
        if (begin == -1) { // 未指定字段
            cxt.setTableSchema(Jdbc.getSchema(targetExpr));
            cxt.setTableName(Jdbc.removeSchema(targetExpr));
            cxt.setTableColumn(new ArrayList<String>(0));
        } else if (targetExpr.endsWith(")")) {
            String tableExpr = targetExpr.substring(0, begin);
            cxt.setTableSchema(Jdbc.getSchema(tableExpr));
            cxt.setTableName(Jdbc.removeSchema(tableExpr));
            String tableColumns = targetExpr.substring(begin + 1, targetExpr.length() - 1); // 截取小括号中的内容
            List<String> columnList = analysis.split(tableColumns, analysis.getSegment());
            if (columnList.isEmpty()) {
                throw new IOException(ResourcesUtils.getScriptStderrMessage(139, targetExpr));
            } else {
                cxt.setTableColumn(columnList);
            }
        } else {
            throw new IOException(ResourcesUtils.getScriptStderrMessage(139, targetExpr));
        }

        // 解析 for exception schema.tableName 用于保存主键冲突不能入库数据
        if (it.isNext("for")) {
            it.assertNext("for");
            it.assertNext("exception");
            String tableExpr = it.next();
            cxt.setErrorTableSchema(Jdbc.getSchema(tableExpr));
            cxt.setErrorTableName(Jdbc.removeSchema(tableExpr));

            if (Jdbc.equals(cxt.getTableSchema(), cxt.getErrorTableSchema(), cxt.getTableName(), cxt.getErrorTableName())) {
                throw new IOException(ResourcesUtils.getScriptStderrMessage(140, command));
            }
        }

        // 读取短语
        while (it.hasNext()) {
            // 解析 indexing mode ( REBUILD | INCREMENTAL | AUTOSELECT ) 短句
            if (it.isNext("indexing")) {
                it.assertNext("indexing");
                it.assertNext("mode");
                cxt.setIndexMode(IndexMode.valueof(it.next()));
            }

            // 解析 statistics use profile 短句
            else if (it.isNext("statistics")) { // 装数装载完毕后是否重新生成索引字段的统计信息
                it.assertNext("statistics");
                it.assertNext("use");
                it.assertNext("profile");
            }

            // 解析 prevent repeat operation 短句
            else if (it.isNext("prevent")) { // 防止重复运行 load 命令
                it.assertNext("prevent");
                it.assertNext("repeat");
                it.assertNext("operation");
            }

            // 不能识别的短句
            else {
                throw new UnsupportedOperationException(it.next());
            }
        }

        return cmd;
    }

    public void usage(UniversalScriptContext context, UniversalScriptStdout out) {
        // 查找接口对应的的实现类
        List<EasyBeanInfo> list1 = context.getContainer().getBeanInfoList(TextTableFile.class);
        CharTable ct1 = new CharTable(context.getCharsetName());
        ct1.addTitle("");
        ct1.addTitle("");
        ct1.addTitle("");
        for (EasyBeanInfo beanInfo : list1) {
            ct1.addCell(beanInfo.getName());
            ct1.addCell(beanInfo.getDescription());
            ct1.addCell(beanInfo.getType().getName());
        }

        out.println(new ScriptUsage(this.getClass() //
                , TextTable.class.getName() // 0
                , EasyBean.class.getName() // 1
                , ExtractUserListener.class.getName() // 2
                , JdbcObjectConverter.class.getName() // 3
                , ExtractWriter.class.getName() // 4
                , ct1.toString(CharTable.Style.simple) // 5
                , "" // 6
                , TextTable.class.getName() // 7
        ));
    }

}
