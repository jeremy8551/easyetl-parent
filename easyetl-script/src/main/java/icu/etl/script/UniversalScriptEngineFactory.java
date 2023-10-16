package icu.etl.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.script.ScriptEngineFactory;

import icu.etl.ioc.BeanFactory;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ArrayUtils;
import icu.etl.util.CharTable;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎工厂类
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-06-01
 */
public class UniversalScriptEngineFactory implements ScriptEngineFactory {

    private Log stdout;
    private Log stderr;

    /** 配置信息 */
    protected UniversalScriptConfiguration config;

    /** 关键字集合 */
    protected Set<String> keywords;

    /**
     * 初始化
     */
    public UniversalScriptEngineFactory() {
        this.stdout = LogFactory.getLog(UniversalScriptEngine.class, System.out, System.err);
        this.stderr = LogFactory.getLog(UniversalScriptEngine.class, System.err, System.err);
        this.config = BeanFactory.get(UniversalScriptConfiguration.class);
        this.keywords = this.config.getKeywords();
    }

    public String getEngineName() {
        return this.config.getEngineName();
    }

    public String getEngineVersion() {
        return this.config.getEngineVersion();
    }

    public String getLanguageName() {
        return this.config.getLanguageName();
    }

    public String getLanguageVersion() {
        return this.config.getLanguageVersion();
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
        StringUtils.split(this.config.getMimeTypes(), ',', list);
        return Collections.unmodifiableList(StringUtils.trimBlank(list));
    }

    public List<String> getExtensions() {
        List<String> list = new ArrayList<String>();
        StringUtils.split(this.config.getExtensions(), ',', list);
        return Collections.unmodifiableList(StringUtils.trimBlank(list));
    }

    public List<String> getNames() {
        List<String> list = new ArrayList<String>();
        StringUtils.split(this.config.getNames(), ',', list);
        return Collections.unmodifiableList(StringUtils.trimBlank(list));
    }

    public String getOutputStatement(String message) {
        return "echo " + StringUtils.ltrimBlank(message);
    }

    public Object getParameter(String key) {
        return this.config.getProperty(key);
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
     * @return
     */
    public Set<String> getKeywords() {
        return this.keywords;
    }

    /**
     * 返回标准信息输出日志接口
     *
     * @return
     */
    public Log getStdoutLog() {
        return this.stdout;
    }

    /**
     * 返回错误信息输出日志接口
     *
     * @return
     */
    public Log getStderrLog() {
        return this.stderr;
    }

    /**
     * 返回用户会话工厂
     *
     * @return
     */
    public UniversalScriptSessionFactory buildSessionFactory() {
        String flag = StringUtils.defaultString(this.config.getSessionFactory(), "default");
        return BeanFactory.get(UniversalScriptSessionFactory.class, flag);
    }

    /**
     * 返回编译器
     *
     * @return
     */
    public UniversalScriptCompiler buildCompiler() {
        String flag = StringUtils.defaultString(this.config.getCompiler(), "default");
        return BeanFactory.get(UniversalScriptCompiler.class, flag);
    }

    /**
     * 返回类型转换器
     *
     * @return
     */
    public UniversalScriptFormatter buildFormatter() {
        String flag = StringUtils.defaultString(this.config.getConverter(), "default");
        return BeanFactory.get(UniversalScriptFormatter.class, flag);
    }

    /**
     * 创建校验器
     *
     * @return
     */
    public UniversalScriptChecker buildChecker() {
        String flag = StringUtils.defaultString(this.config.getChecker(), "default");
        UniversalScriptChecker obj = BeanFactory.get(UniversalScriptChecker.class, flag);
        obj.setScriptEngineKeywords(this.getKeywords());
        return obj;
    }

    /**
     * 打印脚本引擎属性信息
     *
     * @param charsetName
     * @return
     */
    public String toString(String charsetName) {
        String[] array = StringUtils.split(ResourcesUtils.getMessage("script.engine.usage.msg006"), ',');
        String[] titles = StringUtils.split(ResourcesUtils.getMessage("script.engine.usage.msg005"), ',');

        CharTable table = new CharTable(charsetName);
        table.addTitle(array[0]);
        table.addTitle(array[0]);
        table.addValue(titles[0]);
        table.addValue(this.getEngineName());
        table.addValue(titles[1]);
        table.addValue(StringUtils.join(this.getNames(), ", "));
        table.addValue(titles[2]);
        table.addValue(this.getEngineVersion());
        table.addValue(titles[3]);
        table.addValue(StringUtils.join(this.getExtensions(), ", "));
        table.addValue(titles[4]);
        table.addValue(StringUtils.join(this.getMimeTypes(), ", "));
        table.addValue(titles[5]);
        table.addValue(this.getLanguageName());
        table.addValue(titles[6]);
        table.addValue(this.getLanguageVersion());
        table.addValue(titles[7]);
        table.addValue(StringUtils.objToStr(this.getParameter("THREADING")));
        table.addValue(titles[8]);
        table.addValue(this.getOutputStatement("'hello world!'"));
        table.addValue(titles[9]);
        table.addValue(this.getProgram("help", "help script", "help set"));
        table.addValue(titles[10]);
        table.addValue(this.getMethodCallSyntax("obj", "split", new String[]{"':'", "'\\\\'"}));
        table.addValue(titles[11]);
        table.addValue("cat `pwd`/text | tail -n 1 ");
        table.addValue(titles[12]);
        table.addValue("set processId=`nothup script.txt & | tail -n 1`");
        return table.toDB2Shape();
    }

}