package icu.etl.script.command;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import icu.etl.Easyetl;
import icu.etl.ProjectPom;
import icu.etl.annotation.EasyBean;
import icu.etl.annotation.ScriptCommand;
import icu.etl.annotation.ScriptFunction;
import icu.etl.collection.CaseSensitivSet;
import icu.etl.database.DatabaseDialect;
import icu.etl.database.Jdbc;
import icu.etl.database.db2.DB2Dialect;
import icu.etl.database.export.ExtractWriter;
import icu.etl.database.internal.AbstractDialect;
import icu.etl.database.internal.DatabaseDialectBuilder;
import icu.etl.io.TextTableFile;
import icu.etl.ioc.EasyBeanBuilder;
import icu.etl.ioc.EasyBeanInfo;
import icu.etl.ioc.EasyBeanInfoValue;
import icu.etl.ioc.EasyBeanTable;
import icu.etl.ioc.EasyContext;
import icu.etl.ioc.scan.ClassScanRule;
import icu.etl.ioc.scan.ClassScanner;
import icu.etl.jdk.JavaDialect;
import icu.etl.jdk.JavaDialectFactory;
import icu.etl.log.LogFactory;
import icu.etl.os.linux.Linuxs;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandRepository;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptCompiler;
import icu.etl.script.UniversalScriptConfiguration;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptEngine;
import icu.etl.script.UniversalScriptEngineFactory;
import icu.etl.script.UniversalScriptFormatter;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.UniversalScriptVariableMethod;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.CommandCompilerContext;
import icu.etl.script.method.VariableMethodRepository;
import icu.etl.util.CharTable;
import icu.etl.util.CharsetName;
import icu.etl.util.ClassUtils;
import icu.etl.util.CollectionUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.LogUtils;
import icu.etl.util.MessageFormatter;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 打印脚本引擎的使用说明
 * <p>
 * help
 * help markdown
 * help html
 * help command
 */
public class HelpCommand extends AbstractTraceCommand implements NohupCommandSupported {

    /** 输出格式 */
    private String format;

    public HelpCommand(UniversalCommandCompiler compiler, String command, String parameter) {
        super(compiler, command);
        this.format = parameter;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        UniversalScriptAnalysis analysis = session.getAnalysis();
        String type = analysis.replaceVariable(session, context, this.format, false); // md html cmd

        UniversalCommandRepository cr = session.getCompiler().getRepository();
        VariableMethodRepository repository = cr.get(VariableMethodCommandCompiler.class).getRepository();
        String charsetName = context.getCharsetName();

        Object[] args = { //
                ProjectPom.getGroupID() // 0 groupId
                , ProjectPom.getArtifactID() // 1 artifactId
                , ProjectPom.getVersion() // 2 version
                , ProjectPom.getArtifactID() + "-spring-boot-starter" // 3 springboot场景启动器
                , UniversalScriptVariable.SESSION_VARNAME_PWD // 4
                , UniversalScriptVariable.SESSION_VARNAME_SCRIPTNAME // 5
                , UniversalScriptVariable.VARNAME_CHARSET // 6
                , StringUtils.CHARSET // 7
                , UniversalScriptVariable.SESSION_VARNAME_LINESEPARATOR // 8
                , StringUtils.escapeLineSeparator(FileUtils.lineSeparator) // 9
                , UniversalScriptVariable.VARNAME_EXCEPTION // 10
                , UniversalScriptVariable.VARNAME_ERRORSCRIPT // 11
                , UniversalScriptVariable.VARNAME_ERRORCODE // 12
                , UniversalScriptVariable.VARNAME_SQLSTATE // 13
                , UniversalScriptVariable.VARNAME_EXITCODE // 14
                , UniversalScriptVariable.VARNAME_UPDATEROWS // 15
                , UniversalScriptVariable.SESSION_VARNAME_JUMP // 16
                , UniversalScriptVariable.SESSION_VARNAME_STEP // 17
                , UniversalScriptVariable.SESSION_VARNAME_TEMP // 18
                , UniversalScriptVariable.SESSION_VARNAME_SCRIPTFILE // 19
                , UniversalScriptVariable.VARNAME_CATALOG // 20
                , StringUtils.addLinePrefix(repository.toString(charsetName), "\t") // 21 方法
                , EasyBean.class.getSimpleName() // 22
                , TextTableFile.class.getSimpleName() // 23
                , ExtractWriter.class.getSimpleName() // 24
                , AbstractDialect.class.getName() // 25 基础数据库方言类
                , DatabaseDialect.class.getName() // 26 数据库方言类
                , this.supportedDatabase(context) // 27 所有数据库方言类
                , EasyContext.class.getName()  // 28
                , EasyBean.class.getName() // 29
                , DatabaseDialect.class.getSimpleName() // 30
                , DatabaseDialectBuilder.class.getName() // 31
                , EasyContext.class.getSimpleName() // 32
                , DB2Dialect.class.getSimpleName() // 33
                , Arrays.toString(context.getContainer().getScanRule()) // 34
                , Easyetl.class.getName().toLowerCase().replace('.', '/') // 35
                , EasyBeanTable.class.getName() // 36
                , EasyBeanBuilder.class.getName() // 37
                , EasyBeanInfoValue.class.getName() // 38
                , ClassUtils.toMethodName(EasyContext.class, "getBean", Class.class, Object[].class) // 39
                , "" // 40
                , "" // 41
                , "" // 42
                , "" // 43
                , "" // 44
                , "" // 45
                , "" // 46
                , "" // 47
                , "" // 48
                , "" // 49
                , ScriptCommand.class.getName() // 50
                , ScriptCommand.class.getSimpleName() // 51
                , ScriptFunction.class.getName() // 52
                , ScriptFunction.class.getSimpleName() // 53
                , UniversalScriptVariableMethod.class.getName() // 54
                , this.readCommandUsage(2) // 55
                , this.readVariableMethodUsage(2) // 56
                , ClassUtils.toMethodName(ScriptFunction.class, "name") // 57
                , ClassUtils.toMethodName(ScriptFunction.class, "keywords") // 58
                , "" // 59
                , "" // 60
                , "" // 61
                , "" // 62
                , "" // 63
                , "" // 64
                , "" // 65
                , "" // 66
                , "" // 67
                , "" // 68
                , "" // 69
                , ClassScanner.PROPERTY_SCANNPKG // 70
                , LogFactory.PROPERTY_LOG // 71
                , LogFactory.PROPERTY_LOG_SOUT // 72
                , StringUtils.PROPERTY_CHARSET // 73
                , ClassUtils.PROPERTY_CLASSPATH // 74
                , ResourcesUtils.PROPERTY_RESOURCE // 75
                , IO.PROPERTY_READBUF // 76
                , FileUtils.PROPERTY_TEMPDIR // 77
                , LogUtils.PROPERTY_LOG_STACKTRACE // 78
                , Jdbc.PROPERTY_DBLOG // 79
                , Linuxs.PROPERTY_LINUX_BUILTIN_ACCT // 80
                , LogFactory.DEFAULT_LOG_PATTERN // 81
                , "" // 82
                , "" // 83
                , "" // 84
                , "" // 85
                , "" // 86
                , "" // 87
                , "" // 88
                , "" // 89
                , UniversalScriptEngineFactory.class.getName() // 90
                , UniversalScriptEngine.class.getName() // 91
                , UniversalScriptContext.class.getName() // 92
                , UniversalCommandCompiler.class.getName() // 93
                , UniversalScriptParser.class.getName() // 94
                , UniversalScriptReader.class.getName() // 95
                , UniversalScriptAnalysis.class.getName() // 96
                , UniversalScriptFormatter.class.getName() // 97
                , UniversalScriptConfiguration.class.getName() // 98
                , UniversalScriptCommand.class.getName() // 99
                , UniversalScriptVariableMethod.class.getName() // 100
                , ResourcesUtils.class.getName() // 101
                , ClassScanRule.class.getName() // 102
                , ClassUtils.toMethodName(UniversalCommandCompiler.class, "read", UniversalScriptReader.class, UniversalScriptAnalysis.class) // 103
                , ClassUtils.toMethodName(UniversalCommandCompiler.class, "compile", UniversalScriptSession.class, UniversalScriptContext.class, UniversalScriptParser.class, UniversalScriptAnalysis.class, String.class) // 104
                , ClassUtils.toMethodName(UniversalScriptCommand.class, "execute", UniversalScriptSession.class, UniversalScriptContext.class, UniversalScriptStdout.class, UniversalScriptStderr.class, Boolean.class) // 105
                , ClassUtils.toMethodName(UniversalCommandCompiler.class, "match", String.class, String.class) // 106
                , ClassScanner.class.getName() // 107
                , ClassScanRule.class.getSimpleName() // 108
                , UniversalScriptCompiler.class.getName() // 109
                , "" // 110
                , "" // 111
                , "" // 112
                , "" // 113
                , "" // 114
                , "" // 115
                , "" // 116
                , "" // 117
                , "" // 118
                , "" // 119
                , FileUtils.class.getSimpleName() // 120
                , ClassUtils.toMethodName(FileUtils.class, "getTempDir") // 121
                , "" // 122
                , "" // 123
                , "" // 124
                , "" // 125
                , "" // 126
                , "" // 127
                , "" // 128
                , "" // 129
                , "" // 130
                , "" // 131
                , "" // 132
                , "" // 133
                , "" // 134
                , "" // 135
                , "" // 136
                , "" // 137
                , "" // 138
                , "" // 139
                , "" // 140
                , "" // 141
                , "" // 142
                , "" // 143
                , "" // 144
                , "" // 145
                , "" // 146
                , "" // 147
                , "" // 148
                , "" // 149
                , this.toAllImplements(context) // 150
        };

        // 返回命令的使用说明文件
        String packageUri = UniversalScriptEngine.class.getPackage().getName().replace('.', '/');
        InputStream in = ClassUtils.getResourceAsStream("/" + packageUri + "/readme.md", HelpCommand.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IO.write(in, out);
        String markdown = out.toString(charsetName);
        String usage = new MessageFormatter(MessageFormatter.Placeholder.NORMAL).format(markdown, args);
        stdout.println(usage);

        File mdfile = new File("/Users/user/Desktop/使用说明.md");
        FileUtils.createFile(mdfile, true);
        System.out.println(mdfile.getAbsolutePath());
        FileUtils.write(mdfile, "utf-8", false, usage);
        return 0;
    }

    public static void main(String[] args) {
        String str = ClassUtils.toMethodName(ScriptFunction.class, "keywords");
        System.out.println(str);
        for (int i = 119; i <= 150; i++) {
//            System.out.println(", \"\" // " + i);
        }
    }

    public void terminate() throws Exception {
    }

    public String readCommandUsage(int size) throws IOException {
        String prefix = StringUtils.left("", size, '#');
        String url = "/" + ClassUtils.getPackageName(HelpCommand.class, 3).replace('.', '/') + "/scriptCommands.md";
        StringBuilder buf = new StringBuilder(500);
        InputStream in = ClassUtils.getResourceAsStream(url);
        BufferedReader br = IO.getBufferedReader(new InputStreamReader(in, CharsetName.UTF_8));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    buf.append(prefix).append(line).append(FileUtils.lineSeparator);
                } else {
                    buf.append(line).append(FileUtils.lineSeparator);
                }
            }
            return buf.toString();
        } finally {
            br.close();
        }
    }

    public String readVariableMethodUsage(int size) throws IOException {
        String prefix = StringUtils.left("", size, '#');
        String url = "/" + ClassUtils.getPackageName(HelpCommand.class, 3).replace('.', '/') + "/scriptVariableMethods.md";
        StringBuilder buf = new StringBuilder(500);
        InputStream in = ClassUtils.getResourceAsStream(url);
        BufferedReader br = IO.getBufferedReader(new InputStreamReader(in, CharsetName.UTF_8));
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    buf.append(prefix).append(line).append(FileUtils.lineSeparator);
                } else {
                    buf.append(line).append(FileUtils.lineSeparator);
                }
            }
            return buf.toString();
        } finally {
            br.close();
        }
    }

    /**
     * 返回脚本当前支持的所有JDK版本信息
     *
     * @return 所有JDK版本信息
     */
    private String toJavaVersionTable(UniversalScriptContext context) {
        StringBuilder buf = new StringBuilder();

        String cp = ClassUtils.getClasspath(JavaDialect.class);
        if ("jar".equalsIgnoreCase(FileUtils.getFilenameExt(cp))) { // 如果是在 jar 包中
            return JavaDialectFactory.getDialect().getClass().getSimpleName();
        } else {
            String classpath = ClassUtils.getClasspath(JavaDialect.class);
            String packageName = JavaDialect.class.getPackage().getName().replace('.', File.separatorChar);
            File dir = new File(FileUtils.joinPath(classpath, packageName));

            File[] files = FileUtils.array(dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.length() > "java".length() && name.startsWith("Java") && StringUtils.isNumber(name.charAt("Java".length()));
                }
            }));

            // 读取Java 文件名中的版本号
            Set<String> set = new CaseSensitivSet();
            for (File file : files) {
                String filename = FileUtils.getFilenameNoSuffix(file.getName());
                String version = StringUtils.replaceAll(filename, "Java", "JDK");
                set.add(version); // Java5, Java6
            }

            // 读取大版本号
            List<EasyBeanInfo> list = context.getContainer().getBeanInfoList(JavaDialect.class);
            for (EasyBeanInfo anno : list) {
                set.add(anno.getType().getSimpleName());
            }

            // 添加版本号
            for (String version : set) {
                buf.append(version).append(", ");
            }
        }

        return StringUtils.rtrimBlank(buf, ',');
    }

    public String toAllImplements(UniversalScriptContext context) {
        EasyContext ioc = context.getContainer();

        List<Class<?>> types1 = ioc.getBeanInfoTypes();
        Collections.sort(types1, new Comparator<Class<?>>() {
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        });
        LinkedHashSet<Class<?>> keys = new LinkedHashSet<Class<?>>(types1);

        // 以下这些接口的实现类已在帮助说明中删除
        keys.remove(UniversalScriptVariableMethod.class);
        keys.remove(UniversalCommandCompiler.class);
        keys.remove(DatabaseDialect.class);

        String colName1 = "Bean Class Name";
        String colName2 = "Description";

        StringBuilder buf = new StringBuilder();
        for (Class<?> cls : keys) {
            if (ioc.getBeanBuilder(cls) != null) {
                continue;
            }

            List<EasyBeanInfo> list = ioc.getBeanInfoList(cls);
            if (list.isEmpty()) {
                continue;
            }

            CharTable ct = new CharTable();
            ct.addTitle(colName1);
            ct.addTitle(colName2);
            for (EasyBeanInfo beanInfo : list) {
                ct.addCell(beanInfo.getType().getName());
                ct.addCell(beanInfo.getDescription());
            }

            buf.append("### ").append(cls.getSimpleName());
            buf.append(FileUtils.lineSeparatorUnix);

            buf.append(ct.toString(CharTable.Style.markdown));
            buf.append(FileUtils.lineSeparatorUnix);
            buf.append(FileUtils.lineSeparatorUnix);
            buf.append(FileUtils.lineSeparatorUnix);
        }

        List<Class<?>> types2 = ioc.getBeanBuilderType();
        Collections.sort(types2, new Comparator<Class<?>>() {
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        });

        LinkedHashSet<Class<?>> builders = new LinkedHashSet<Class<?>>(types2);
        builders.remove(UniversalScriptVariableMethod.class);
        builders.remove(UniversalCommandCompiler.class);
        builders.remove(DatabaseDialect.class);
        for (Class<?> cls : builders) {
            EasyBeanBuilder<?> beanBuilder = ioc.getBeanBuilder(cls);

            List<EasyBeanInfo> list = ioc.getBeanInfoList(cls);
            if (list.isEmpty()) {
                continue;
            }

            CharTable ct = new CharTable();
            ct.addTitle(colName1);
            ct.addTitle(colName2);

            for (EasyBeanInfo beanInfo : list) {
                ct.addCell(beanInfo.getType().getName());
                ct.addCell(beanInfo.getDescription());
            }

            buf.append("### ").append(cls.getSimpleName()).append(FileUtils.lineSeparatorUnix);
            buf.append("**Use Bean Builder：").append(beanBuilder.getClass().getName()).append("**").append(FileUtils.lineSeparatorUnix);
            buf.append(ct.toString(CharTable.Style.markdown));
            buf.append(FileUtils.lineSeparatorUnix);
            buf.append(FileUtils.lineSeparatorUnix);
            buf.append(FileUtils.lineSeparatorUnix);
        }

        return buf.toString();
    }

    /**
     * 查询当前支持的数据库
     *
     * @param context 脚本引擎上下文信息
     * @return 当前支持的数据库
     */
    public String supportedDatabase(UniversalScriptContext context) {
        List<EasyBeanInfo> list = context.getContainer().getBeanInfoList(DatabaseDialect.class);
        Collections.sort(list, new Comparator<EasyBeanInfo>() {
            public int compare(EasyBeanInfo o1, EasyBeanInfo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        String[] array = StringUtils.split(ResourcesUtils.getMessage("script.engine.usage.msg008"), ',');
        CharTable table = new CharTable(context.getCharsetName());
        table.addTitle(array[0], CharTable.ALIGN_MIDDLE);
        table.addTitle(array[1], CharTable.ALIGN_LEFT);
        table.addTitle(array[2], CharTable.ALIGN_RIGHT);

        for (EasyBeanInfo beanInfo : list) {
            table.addCell(beanInfo.getName());
            table.addCell(StringUtils.defaultString(beanInfo.getDescription(), "") + "     ");
            table.addCell("          " + beanInfo.getType().getName());
        }
        return table.toString(CharTable.Style.markdown);
    }

    public String supportedVariableMethods(UniversalScriptContext context, VariableMethodRepository repository) {
        List<EasyBeanInfo> list = context.getContainer().getBeanInfoList(DatabaseDialect.class);
        Collections.sort(list, new Comparator<EasyBeanInfo>() {
            public int compare(EasyBeanInfo o1, EasyBeanInfo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        String[] array = StringUtils.split(ResourcesUtils.getMessage("script.engine.usage.msg008"), ',');
        CharTable table = new CharTable(context.getCharsetName());
        table.addTitle(array[0], CharTable.ALIGN_MIDDLE);
        table.addTitle(array[1], CharTable.ALIGN_LEFT);
        table.addTitle(array[2], CharTable.ALIGN_RIGHT);

        for (EasyBeanInfo beanInfo : list) {
            table.addCell(beanInfo.getName());
            table.addCell(StringUtils.defaultString(beanInfo.getDescription(), "") + "     ");
            table.addCell("          " + beanInfo.getType().getName());
        }
        return table.toString(CharTable.Style.markdown);
    }

    /**
     * 输出所有命令的使用说明
     *
     * @param session 用户会话信息
     * @param context 脚本引擎上下文信息
     * @param out     输出流
     */
    private void printScriptCommandDetailUsage(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout out) {
        UniversalCommandRepository commandBuilder = session.getCompiler().getRepository();
        for (Iterator<CommandCompilerContext> it = commandBuilder.iterator(); it.hasNext(); ) {
            CommandCompilerContext cxt = it.next();
            if (ResourcesUtils.existsScriptMessage(cxt.getUsage())) {
                cxt.getCompiler().usage(context, out);
                out.println("");
                out.println("");
            }
        }
    }

    /**
     * 输出脚本引擎属性信息
     *
     * @param context 脚本引擎上下文信息
     * @return 字符图形表格
     */
    protected CharTable getScriptAttributes(UniversalScriptContext context) {
        String[] array = StringUtils.split(ResourcesUtils.getMessage("script.engine.usage.msg006"), ',');

        CharTable table = new CharTable(context.getCharsetName());
        table.addTitle(array[0]);
        table.addTitle(array[0]);

        String[] titles = StringUtils.split(ResourcesUtils.getMessage("script.engine.usage.msg005"), ',');
        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> engineFactories = manager.getEngineFactories();
        for (ScriptEngineFactory factory : engineFactories) {
            if (factory instanceof UniversalScriptEngineFactory) { // 使用当前脚本引擎的容器上下文信息，防止新脚本引擎创建新容器，导致重新扫描
                ((UniversalScriptEngineFactory) factory).setContext(context.getContainer());
            }

            table.addCell(titles[0]);
            table.addCell(factory.getEngineName());
            table.addCell(titles[1]);
            table.addCell(StringUtils.join(factory.getNames(), ", "));
            table.addCell(titles[2]);
            table.addCell(factory.getEngineVersion());
            table.addCell(titles[3]);
            table.addCell(StringUtils.join(factory.getExtensions(), ", "));
            table.addCell(titles[4]);
            table.addCell(StringUtils.join(factory.getMimeTypes(), ", "));
            table.addCell(titles[5]);
            table.addCell(factory.getLanguageName());
            table.addCell(titles[6]);
            table.addCell(factory.getLanguageVersion());
            table.addCell(titles[7]);
            table.addCell(StringUtils.objToStr(factory.getParameter("THREADING")));
            table.addCell(titles[8]);
            table.addCell(factory.getOutputStatement("'hello world!'"));
            table.addCell(titles[9]);
            table.addCell(factory.getProgram("help", "help script", "help set"));
            table.addCell(titles[10]);
            table.addCell(factory.getMethodCallSyntax("obj", "split", new String[]{"':'", "'\\'"}));

            String name = CollectionUtils.firstElement(factory.getNames());
            ScriptEngine engine = manager.getEngineByName(name);
            table.addCell(titles[13]);
            table.addCell(engine == null ? "" : engine.getClass().getName());

            table.addCell("");
            table.addCell("");
        }
        return table;
    }

    public boolean enableNohup() {
        return true;
    }

}
