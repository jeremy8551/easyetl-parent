package icu.etl.script.command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import icu.etl.ProjectPom;
import icu.etl.annotation.EasyBean;
import icu.etl.annotation.ScriptCommand;
import icu.etl.annotation.ScriptFunction;
import icu.etl.collection.CaseSensitivSet;
import icu.etl.database.DatabaseDialect;
import icu.etl.database.Jdbc;
import icu.etl.database.internal.AbstractDialect;
import icu.etl.ioc.EasyBeanBuilder;
import icu.etl.ioc.EasyBeanInfo;
import icu.etl.ioc.EasyContext;
import icu.etl.ioc.scan.ClassScanRule;
import icu.etl.ioc.scan.ClassScanner;
import icu.etl.jdk.JavaDialect;
import icu.etl.jdk.JavaDialectFactory;
import icu.etl.log.LogFactory;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandRepository;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptChecker;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptCompiler;
import icu.etl.script.UniversalScriptConfiguration;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptEngine;
import icu.etl.script.UniversalScriptEngineFactory;
import icu.etl.script.UniversalScriptExpression;
import icu.etl.script.UniversalScriptFormatter;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptJob;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariable;
import icu.etl.script.UniversalScriptVariableMethod;
import icu.etl.script.command.feature.LoopCommandKind;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.internal.CommandCompilerContext;
import icu.etl.script.method.VariableMethodRepository;
import icu.etl.util.CharTable;
import icu.etl.util.ClassUtils;
import icu.etl.util.CollectionUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.Settings;
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
                , "easyetl" // 1 artifactId
                , "2.0.6" // 2 version
                , "easyetl-spring-boot-starter" // 3 easyetl-spring-boot-starter artifactId
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
                , "" //
                , "" //
                , "" //
                , "" //
                , "" //
                , "" //
        };

        String[] old = {"" //
                , StringUtils.addLinePrefix(this.getScriptAttributes(context).toString(CharTable.Style.standard), "\t") // 0
                , StringUtils.addLinePrefix(cr.toString(charsetName), "\t") // 1
                , UniversalScriptCommand.class.getName() // 2
                , UniversalCommandCompiler.class.getName() // 3
                , UniversalScriptVariableMethod.class.getName() // 4
                , UniversalScriptVariableMethod.class.getName() // 5
                , AbstractCommandCompiler.class.getName() // 6
                , AbstractTraceCommandCompiler.class.getName() // 7
                , AbstractFileCommandCompiler.class.getName() // 8
                , AbstractGlobalCommandCompiler.class.getName() // 9
                , AbstractSlaveCommandCompiler.class.getName() // 10
                , UniversalScriptInputStream.class.getName() // 11
                , LoopCommandKind.class.getName() // 12
                , UniversalScriptJob.class.getName() // 13
                , ScriptCommand.class.getName() // 14
                , ScriptFunction.class.getName() // 15
                , this.supportedDatabase(context) // 16
                , DatabaseDialect.class.getName() // 17
                , AbstractDialect.class.getName() // 18
                , EasyBean.class.getName() // 19
                , ClassScanner.PROPERTY_SCANNPKG // 20
                , CollectionUtils.firstElement(context.getFactory().getExtensions()) // 21
                , "" // 22
                , "" // 23
                , ProjectPom.getGroupID() // 24
                , UniversalScriptEngine.class.getName() // 25
                , ClassUtils.getClasspath(HelpCommand.class) // 26
                , Settings.getFileEncoding() // 27
                , UniversalScriptEngineFactory.class.getName() // 28
                , UniversalScriptChecker.class.getName() // 29
                , UniversalScriptCompiler.class.getName() // 30
                , UniversalScriptConfiguration.class.getName() // 31
                , UniversalScriptContext.class.getName() // 32
                , UniversalScriptFormatter.class.getName() // 33
                , DatabaseDialect.class.getName() // 34
                , ResourcesUtils.ResourceName + ".properties" // 35
                , ResourcesUtils.PROPERTY_RESOURCE // 36
                , UniversalScriptParser.class.getName() // 37
                , UniversalScriptReader.class.getName() // 38
                , UniversalScriptAnalysis.class.getName() // 39
                , ScriptEngineFactory.class.getName() // 40
                , ScriptEngine.class.getName() // 41
                , ScriptContext.class.getName() // 42
                , LogFactory.PROPERTY_LOG // 43
                , StringUtils.PROPERTY_CHARSET // 44
                , "" // 45
                , Jdbc.PROPERTY_DBLOG // 46
                , StringUtils.CHARSET // 47
                , "" // 48
                , ClassUtils.toMethodName(UniversalCommandCompiler.class, "read", UniversalScriptReader.class, UniversalScriptAnalysis.class) // 49
                , ClassUtils.toMethodName(UniversalCommandCompiler.class, "compile", UniversalScriptParser.class, UniversalScriptAnalysis.class, String.class)// 50
                , ClassUtils.toMethodName(UniversalScriptCommand.class, "execute", UniversalScriptSession.class, UniversalScriptContext.class, UniversalScriptStdout.class, UniversalScriptStderr.class, Boolean.class) // 51
                , ClassUtils.toMethodName(UniversalCommandCompiler.class, "compile") // 52
                , "META-INF/services/" + ClassScanRule.class.getName() // 53
                , "" // 54
                , "" // 55
                , "" // 56
                , "" // 57
                , "" // 58
                , "" // 59
                , "" // 60
                , ClassUtils.toMethodName(EasyBean.class, "description") // 61
                , "" // 62
                , UniversalScriptExpression.class.getName() // 63
                , ClassUtils.toMethodName(ScriptFunction.class, "name") // 64
                , ClassUtils.toMethodName(ScriptFunction.class, "keywords") // 65
                , "" // 66
                , StringUtils.addLinePrefix(repository.toString(charsetName), "\t") // 67
                , String.valueOf(analysis.getSegment()) // 68
                , this.toAllImplements(context) // 69
                , this.toJavaVersionTable(context) // 70
                , JavaDialectFactory.getDialect().getClass().getSimpleName() // 71
                , Settings.class.getName() // 72
                , "info" // 73 日志默认级别
        };

        // 返回命令的使用说明文件
        String packageUri = UniversalScriptEngine.class.getPackage().getName().replace('.', '/');
        InputStream in = ClassUtils.getResourceAsStream("/" + packageUri + "/readme.md", HelpCommand.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IO.write(in, out);
        String markdown = out.toString(charsetName);
        String usage = markdown; // MessageFormat.format(markdown, args);
        stdout.println(usage);
        return 0;
    }

    public void terminate() throws Exception {
    }

    /**
     * 返回脚本当前支持的所有JDK版本信息
     *
     * @return
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
            List<EasyBeanInfo> list = context.getFactory().getContext().getBeanInfoList(JavaDialect.class);
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

    public String toAllImplements(UniversalScriptContext scriptContext) {
        StringBuilder buf = new StringBuilder();
        EasyContext context = scriptContext.getFactory().getContext();
        Set<Class<?>> setes = new LinkedHashSet<Class<?>>(context.getBeanInfoTypes());

        // 以下这些接口的实现类已在帮助说明中删除
        setes.remove(UniversalScriptVariableMethod.class);
        setes.remove(UniversalCommandCompiler.class);
        setes.remove(DatabaseDialect.class);

        for (Class<?> cls : setes) {
            if (context.getBeanBuilder(cls) != null) {
                continue;
            }

            buf.append("* ").append(cls.getName()).append(FileUtils.lineSeparator);
            List<EasyBeanInfo> list = context.getBeanInfoList(cls);
            CharTable ct = new CharTable();
            ct.addTitle("");
            ct.addTitle("");
            for (EasyBeanInfo beanInfo : list) {
                ct.addCell(beanInfo.getType().getName());
                ct.addCell(beanInfo.getDescription());
            }
            buf.append(ct.toString(CharTable.Style.simple));
            buf.append(FileUtils.lineSeparator);
            buf.append(FileUtils.lineSeparator);
        }

        Set<Class<?>> beanClses = new LinkedHashSet<Class<?>>(context.getBeanBuilderType());
        beanClses.remove(UniversalScriptVariableMethod.class);
        beanClses.remove(UniversalCommandCompiler.class);
        beanClses.remove(DatabaseDialect.class);
        for (Class<?> cls : beanClses) {
            EasyBeanBuilder<?> beanBuilder = context.getBeanBuilder(cls);
            buf.append("* ").append(cls.getName()).append(" -> ").append(beanBuilder.getClass().getName()).append(FileUtils.lineSeparator);

            List<EasyBeanInfo> list = context.getBeanInfoList(cls);
            CharTable ct = new CharTable();
            ct.addTitle("");
            ct.addTitle("");
            for (EasyBeanInfo beanInfo : list) {
                ct.addCell(beanInfo.getType().getName());
                ct.addCell(beanInfo.getDescription());
            }
            buf.append(ct.toString(CharTable.Style.simple));
            buf.append(FileUtils.lineSeparator);
            buf.append(FileUtils.lineSeparator);
        }

        return StringUtils.addLinePrefix(buf, "\t");
    }

    /**
     * 查询当前支持的数据库
     *
     * @param context 脚本引擎上下文信息
     * @return
     */
    public String supportedDatabase(UniversalScriptContext context) {
        List<EasyBeanInfo> list = context.getFactory().getContext().getBeanInfoList(DatabaseDialect.class);
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
            table.addCell(beanInfo.getDescription() + "     ");
            table.addCell("          " + beanInfo.getType().getName());
        }
        return StringUtils.addLinePrefix(table.toString(CharTable.Style.standard), "\t");
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
     * @param context
     * @return
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
                ((UniversalScriptEngineFactory) factory).setContext(context.getFactory().getContext());
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
