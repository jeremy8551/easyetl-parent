package icu.etl.expression;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ArrayUtils;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 命令的参数表达式，可以解析并读取参数字符串中的选项名，选项值，参数值 <br>
 * <br>
 * 参数如: java TestMain -d 20100101 -c -can -p E:\temp\temp.txt <br>
 * <br>
 * 使用方法: <br>
 * <br>
 * {@linkplain ArgumentExpression} obj = {@linkplain ArgumentExpression}(); <br>
 * obj.{@linkplain #addOption(String[])}; <br>
 * obj.{@linkplain #addOption(String)}; <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2010-01-19 03:43:16
 */
public class ArgumentExpression implements Serializable, Cloneable {
    private final static Log log = LogFactory.getLog(ArgumentExpression.class);
    private final static long serialVersionUID = 1L;

    /** 选项名与选项值的映射 */
    protected Map<String, String> values;

    /** 参数个数 */
    protected int parameterSize;

    /** 语句分析器 */
    protected Analysis analysis;

    /**
     * 初始化
     *
     * @param analysis 语句分析器
     */
    public ArgumentExpression(Analysis analysis) {
        this.analysis = Ensure.notNull(analysis);
        this.values = new LinkedHashMap<String, String>();
        this.parameterSize = 0;
    }

    /**
     * 初始化
     */
    public ArgumentExpression() {
        this(new BaseAnalysis());
    }

    /**
     * 初始化
     *
     * @param args 参数数组
     */
    public ArgumentExpression(String[] args) {
        this(new BaseAnalysis());
        this.addOption(args);
    }

    /**
     * 初始化
     *
     * @param str 参数
     */
    public ArgumentExpression(String str) {
        this(new BaseAnalysis());
        this.addOption(str);
    }

    /**
     * 先清空参数管理器中所有参数, 解析字符串数组并添加到参数管理器中
     *
     * @param args 字符串数组
     * @return 返回参数表达式
     */
    public ArgumentExpression addOption(String[] args) {
        this.addOptions(ArrayUtils.asList(Ensure.notNull(args)));
        return this;
    }

    /**
     * 解析字符串参数 str 并添加选项到参数管理器中
     *
     * @param str 字符串, 如: -d 20170101 -c str -p "file path" -a 'it is good!'
     * @return 返回参数表达式
     */
    public ArgumentExpression addOption(String str) {
        List<String> list = this.analysis.split(Ensure.notNull(str));
        for (int i = 0; i < list.size(); i++) {
            list.set(i, this.analysis.unQuotation(list.get(i)));
        }
        this.addOptions(list);
        return this;
    }

    /**
     * 添加选项 <br>
     * 合法的选项名以 '-' 字符开始且后面必须有一个英文或数字字符 <br>
     * <br>
     * 字符串 “-c 2009” 中 “-c” 叫选项名，其中 “2009” 是选项值 <br>
     * 只有一个 '-' 字符不是合法选项名
     *
     * @param key   选项名，如: -d
     * @param value 选项值
     */
    public void addOption(String key, String value) {
        String old = this.values.put(key, value);
        if (old != null) {
            if (log.isTraceEnabled()) {
                log.trace(ResourcesUtils.getParamMessage(1, key, old, value));
            }
        }
    }

    /**
     * 添加选项名与选项值的映射集合
     *
     * @param options 选项集合（只添加 Map 中 value 是字符串的选项信息）
     * @return 返回参数表达式
     */
    public ArgumentExpression addOption(Map<String, String> options) {
        if (options == null || options.size() == 0) {
            return this;
        }

        Set<String> names = options.keySet(); // 选项名集合
        for (String name : names) {
            String value = options.get(name); // 选项值

            // 判断选项名是否合法
            if (!this.isOption(name)) {
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getParamMessage(2, name + "=" + value));
                }
                continue;
            }

            // 判断选项值是否合法
            if (value == null) {
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getParamMessage(3, name + "=null"));
                }
                continue;
            }

            this.addOption(name, value);
        }
        return this;
    }

    /**
     * 将参数管理器中的所有选项信息添加到当前对象中
     *
     * @param obj 参数管理器
     * @return 返回参数表达式
     */
    public ArgumentExpression addOption(ArgumentExpression obj) {
        if (obj != null && !obj.values.isEmpty()) {
            Set<Entry<String, String>> entries = obj.values.entrySet();
            for (Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (this.isOption(key)) {
                    this.addOption(key, value);
                } else { // 添加参数值
                    this.addParameter(value);
                }
            }
        }
        return this;
    }

    /**
     * 解析参数数组后添加到参数管理器 <br>
     * 将输入的参数数组 String[] { "java", "Test", "-d", "120.0", "-m" } 解析成选项后: <br>
     * 1) 会有二个选项: <br>
     * 第一个选项名和选项值: "-d" = "120.0" <br>
     * 第二个选项名和选项值: "-m" = "" <br>
     * <br>
     * 2) 会有五个参数值: <br>
     * 第一个参数值: "java" <br>
     * 第二个参数值: "Test" <br>
     * 第三个参数值: "-d" <br>
     * 第四个参数值: "120.0" <br>
     * 第五个参数值: "-m" <br>
     *
     * @param args 字符串数组，如 public static void main(String[] args) {} 方法的参数数组
     */
    protected void addOptions(List<String> args) {
        for (int i = 0; i < args.size(); ) {
            if (this.isOption(args.get(i))) { // 判断是否是选项名
                String name = args.get(i); // 选项名
                String value = ""; // 选项值

                i++;
                if (i < args.size() && !this.isOption(args.get(i))) {
                    value = args.get(i);
                    i++;
                }

                this.addOption(name, value);
            } else {
                this.addParameter(args.get(i));
                i++;
            }
        }
    }

    /**
     * 判断字符串参数 name 是否是合法选项名
     *
     * @param name 选项名, 如: -d
     */
    public boolean isOption(String name) {
        return name != null && name.length() > 1 && name.charAt(0) == '-';
    }

    /**
     * 返回选项名对应的选项值
     *
     * @param name 选项名, 如: -d
     * @return 选项值
     */
    public String getOption(String name) {
        return this.isOption(name) ? this.values.get(name) : null;
    }

    /**
     * 返回所有选项名的数组
     *
     * @return 选项名副本
     */
    public String[] getOptionNames() {
        return this.values.keySet().toArray(new String[this.values.size()]);
    }

    /**
     * 判断选项名是否存在
     *
     * @param name 选项名, 如: -d
     * @return 返回true表示选项名存在 返回false表示不存在
     */
    public boolean containOption(String name) {
        return this.isOption(name) && this.values.containsKey(name);
    }

    /**
     * 判断是否存在选项名
     *
     * @return 返回true表示存在选项名
     */
    public boolean existsOption() {
        return this.values.isEmpty();
    }

    /**
     * 删除选项名及选项值
     *
     * @param name 选项名, 如: -d
     * @return 被移除的选项值，可能为null
     */
    public String removeOption(String name) {
        if (this.isOption(name)) {
            return this.values.remove(name);
        } else {
            return null;
        }
    }

    /**
     * 判断选项值是否为null或空字符串
     *
     * @param name 选项名, 如: -d
     * @return 返回true表示选项值是 null 或空字符串
     */
    public boolean isOptionBlank(String name) {
        return StringUtils.isBlank(this.getOption(name));
    }

    /**
     * 添加参数
     *
     * @param parameter 参数值
     */
    public void addParameter(String parameter) {
        if (StringUtils.isBlank(parameter)) {
            return;
        }

        String key = String.valueOf(++this.parameterSize);
        this.values.put(key, parameter);
    }

    /**
     * 返回第 n 个参数值
     *
     * @param n 位置信息，从1开始
     * @return 返回参数字符串
     */
    public String getParameter(int n) {
        return this.values.get(String.valueOf(n));
    }

    /**
     * 返回参数值的个数
     *
     * @return 参数个数
     */
    public int getParameterSize() {
        return this.parameterSize;
    }

    /**
     * 判断参数值是否为null或空字符串
     *
     * @param index 从1开始
     * @return 返回true表示参数值是 null 或空字符串
     */
    public boolean isParameterBlank(int index) {
        return index <= 0 || index >= this.parameterSize || StringUtils.isBlank(this.values.get(String.valueOf(index)));
    }

    /**
     * 删除选项名左侧的半角横线
     *
     * @return 返回一个新的副本
     */
    public Map<String, String> removeMinus() {
        Map<String, String> map = new HashMap<String, String>(this.values.size());
        Set<String> names = this.values.keySet();
        for (String name : names) {
            String value = this.values.get(name); // 选项值 或 参数值
            if (this.isOption(name)) {
                map.put(name.substring(1), value); // 删除选项名前的 ‘-’ 符号
            } else {
                map.put(name, value);
            }
        }
        return map;
    }

    /**
     * 返回选项名与选项值的映射集合，参数位置与参数值的集合 <br>
     * 且集合中的数据不可修改
     */
    public Map<String, String> values() {
        return Collections.unmodifiableMap(this.values);
    }

    /**
     * 清空所有信息
     */
    public void clear() {
        this.values.clear();
    }

    public ArgumentExpression clone() {
        return new ArgumentExpression().addOption(this);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (Iterator<Entry<String, String>> it = this.values.entrySet().iterator(); it.hasNext(); ) {
            Entry<String, String> e = it.next();
            String key = e.getKey();
            String value = e.getValue();

            boolean option = this.isOption(key);
            if (option) {
                buf.append(key);
                if (value != null && value.length() > 0) {
                    buf.append(' ');
                }
            }

            if (StringUtils.isNotBlank(value) && StringUtils.indexOfBlank(value, 0, -1) != -1 //
                    && !StringUtils.containsQuotation(value) //
                    && !StringUtils.containsDoubleQuotation(value) //
                    && !StringUtils.containsParenthes(value) //
            ) {
                buf.append("'").append(value).append("'");
            } else {
                buf.append(value);
            }

            if (it.hasNext()) {
                buf.append(' ');
            }
        }
        return buf.toString();
    }

}