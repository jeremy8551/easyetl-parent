package icu.etl.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.script.ScriptEngineFactory;

import icu.etl.ioc.EasyBeanContext;
import icu.etl.ioc.EasyContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.CharTable;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎工厂类
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-06-01
 */
public class UniversalScriptEngineFactory implements ScriptEngineFactory {

    /** 配置信息 */
    protected UniversalScriptConfiguration config;

    /** 容器的上下文信息 */
    protected EasyContext context;

    /**
     * 初始化
     * 因为涉及到 {@linkplain javax.script.ScriptEngineManager} 使用 SPI 机制读取并创建脚本引擎工厂实例，所以本方法中只做简单操作。
     */
    public UniversalScriptEngineFactory() {
    }

    /**
     * 初始化
     *
     * @param context 容器上下文信息
     */
    public UniversalScriptEngineFactory(EasyContext context) {
        this();
        this.setContext(context);
    }

    /**
     * 设置脚本引擎使用的容器上下文信息
     *
     * @param context 容器上下文信息
     */
    public void setContext(EasyContext context) {
        this.context = Ensure.notNull(context);
    }

    /**
     * 返回组件的容器上下文信息
     *
     * @return 容器上下文信息
     */
    public EasyContext getContext() {
        if (this.context == null) {
            synchronized (this) {
                if (this.context == null) {
                    this.context = new EasyBeanContext();
                }
            }
        }
        return this.context;
    }

    /**
     * 返回脚本引擎配置信息
     *
     * @return 配置信息
     */
    public UniversalScriptConfiguration getConfiguration() {
        if (this.config == null) {
            synchronized (this) {
                if (this.config == null) {
                    this.config = this.getContext().getBean(UniversalScriptConfiguration.class);
                }
            }
        }
        return this.config;
    }

    public String getEngineName() {
        return this.getConfiguration().getEngineName();
    }

    public String getEngineVersion() {
        return this.getConfiguration().getEngineVersion();
    }

    public String getLanguageName() {
        return this.getConfiguration().getLanguageName();
    }

    public String getLanguageVersion() {
        return this.getConfiguration().getLanguageVersion();
    }

    public String getMethodCallSyntax(String obj, String method, String... args) {
        if (StringUtils.isBlank(obj) && StringUtils.isNotBlank(method)) {
            StringBuilder buf = new StringBuilder();
            for (String str : args) {
                buf.append(' ');
                if (StringUtils.indexOfBlank(str, 0, -1) != -1) {
                    buf.append("\"");
                    buf.append(StringUtils.defaultString(str, ""));
                    buf.append("\"");
                } else {
                    buf.append(StringUtils.defaultString(str, ""));
                }
            }
            return buf.toString();
        } else if (StringUtils.isNotBlank(obj) && StringUtils.isNotBlank(method)) {
            StringBuilder buf = new StringBuilder(obj).append('.').append(method).append('(');
            ArrayList<String> list = ArrayUtils.asList(args);

            for (Iterator<String> it = list.iterator(); it.hasNext(); ) {
                String str = it.next();

                if (StringUtils.indexOfBlank(str, 0, -1) != -1) {
                    buf.append('\'');
                    buf.append(StringUtils.defaultString(str, ""));
                    buf.append('\'');
                } else {
                    buf.append(StringUtils.defaultString(str, ""));
                }

                if (it.hasNext()) {
                    buf.append(", ");
                }
            }
            buf.append(')');
            return buf.toString();
        }

        throw new IllegalArgumentException("getMethodCallSyntax(" + obj + ", " + method + ", " + StringUtils.toString(args) + ")");
    }

    public List<String> getMimeTypes() {
        List<String> list = new ArrayList<String>();
        StringUtils.split(this.getConfiguration().getMimeTypes(), ',', list);
        return Collections.unmodifiableList(StringUtils.trimBlank(list));
    }

    public List<String> getExtensions() {
        List<String> list = new ArrayList<String>();
        StringUtils.split(this.getConfiguration().getExtensions(), ',', list);
        return Collections.unmodifiableList(StringUtils.trimBlank(list));
    }

    public List<String> getNames() {
        List<String> list = new ArrayList<String>();
        StringUtils.split(this.getConfiguration().getNames(), ',', list);
        return Collections.unmodifiableList(StringUtils.trimBlank(list));
    }

    public String getOutputStatement(String message) {
        return "echo " + StringUtils.ltrimBlank(message);
    }

    public Object getParameter(String key) {
        return this.getConfiguration().getProperty(key);
    }

    public String getProgram(String... statements) {
        char token = this.buildCompiler().getAnalysis().getToken();
        StringBuilder buf = new StringBuilder();
        for (String str : statements) {
            buf.append(str).append(token).append(' ');
        }
        return buf.toString();
    }

    public UniversalScriptEngine getScriptEngine() {
        return new UniversalScriptEngine(this);
    }

    /**
     * 关键字集合
     *
     * @return 关键字集合
     */
    public Set<String> getKeywords() {
        return this.getConfiguration().getKeywords();
    }

    /**
     * 返回用户会话工厂
     *
     * @return 用户会话工厂
     */
    public UniversalScriptSessionFactory buildSessionFactory() {
        String flag = StringUtils.defaultString(this.getConfiguration().getSessionFactory(), "default");
        return this.getContext().getBean(UniversalScriptSessionFactory.class, flag);
    }

    /**
     * 返回编译器
     *
     * @return 编译器
     */
    public UniversalScriptCompiler buildCompiler() {
        String flag = StringUtils.defaultString(this.getConfiguration().getCompiler(), "default");
        return this.getContext().getBean(UniversalScriptCompiler.class, flag);
    }

    /**
     * 返回类型转换器
     *
     * @return 类型转换器
     */
    public UniversalScriptFormatter buildFormatter() {
        String flag = StringUtils.defaultString(this.getConfiguration().getConverter(), "default");
        return this.getContext().getBean(UniversalScriptFormatter.class, flag);
    }

    /**
     * 创建校验器
     *
     * @return 校验器
     */
    public UniversalScriptChecker buildChecker() {
        String flag = StringUtils.defaultString(this.getConfiguration().getChecker(), "default");
        UniversalScriptChecker obj = this.getContext().getBean(UniversalScriptChecker.class, flag);
        obj.setScriptEngineKeywords(this.getKeywords());
        return obj;
    }

    /**
     * 打印脚本引擎属性信息
     *
     * @param charsetName 字符集
     * @return 图形表格
     */
    public String toString(String charsetName) {
        String[] array = StringUtils.split(ResourcesUtils.getMessage("script.engine.usage.msg006"), ',');
        String[] titles = StringUtils.split(ResourcesUtils.getMessage("script.engine.usage.msg005"), ',');

        CharTable table = new CharTable(charsetName);
        table.addTitle(array[0]);
        table.addTitle(array[0]);
        table.addCell(titles[0]);
        table.addCell(this.getEngineName());
        table.addCell(titles[1]);
        table.addCell(StringUtils.join(this.getNames(), ", "));
        table.addCell(titles[2]);
        table.addCell(this.getEngineVersion());
        table.addCell(titles[3]);
        table.addCell(StringUtils.join(this.getExtensions(), ", "));
        table.addCell(titles[4]);
        table.addCell(StringUtils.join(this.getMimeTypes(), ", "));
        table.addCell(titles[5]);
        table.addCell(this.getLanguageName());
        table.addCell(titles[6]);
        table.addCell(this.getLanguageVersion());
        table.addCell(titles[7]);
        table.addCell(StringUtils.objToStr(this.getParameter("THREADING")));
        table.addCell(titles[8]);
        table.addCell(this.getOutputStatement("'hello world!'"));
        table.addCell(titles[9]);
        table.addCell(this.getProgram("help", "help script", "help set"));
        table.addCell(titles[10]);
        table.addCell(this.getMethodCallSyntax("obj", "split", new String[]{"':'", "'\\\\'"}));
        table.addCell(titles[11]);
        table.addCell("cat `pwd`/text | tail -n 1 ");
        table.addCell(titles[12]);
        table.addCell("set processId=`nothup script.txt & | tail -n 1`");
        return table.toString(CharTable.Style.db2);
    }

}
