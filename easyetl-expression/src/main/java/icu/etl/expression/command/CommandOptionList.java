package icu.etl.expression.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import icu.etl.expression.CommandExpression;
import icu.etl.expression.ExpressionException;
import icu.etl.util.Dates;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public class CommandOptionList {

    /** 表达式信息 */
    private CommandExpression expr;

    /** 选项规则 */
    private LinkedHashMap<String, CommandOptionPattern> patterns;

    /** 命令中的实际选项 */
    private LinkedHashMap<String, CommandOptionValue> values;

    /** 选项名与不能同时使用的选项名集合的映射 */
    private LinkedHashMap<String, Set<String>> rules;

    /** 选项名与不能同时使用的选项名集合的映射 */
    private List<Set<String>> mustExists;

    /**
     * 初始化
     */
    public CommandOptionList(CommandExpression expr) {
        this.expr = expr;
        this.patterns = new LinkedHashMap<String, CommandOptionPattern>();
        this.values = new LinkedHashMap<String, CommandOptionValue>();
        this.rules = new LinkedHashMap<String, Set<String>>();
        this.mustExists = new ArrayList<Set<String>>();
    }

    /**
     * 判断字符串参数是否匹配选项的规则
     *
     * @param str 字符串
     * @return 返回true表示匹配
     */
    public boolean match(String str) {
        return this.isOption(str) //
                || (str.startsWith("[") && str.endsWith("]")) //
                || (str.startsWith("(") && str.endsWith(")")) //
                ;
    }

    /**
     * 判断字符串参数 str 是否是合法选项名
     *
     * @param str 字符串(-d -xvf --prefix)
     * @return 返回true表示字符串是合法选项名
     */
    public boolean isOption(String str) {
        return (str.startsWith("-") && str.length() >= 2 && str.charAt(1) != '-' && !Character.isWhitespace(str.charAt(1))) //
                || (str.startsWith("--") && str.length() >= 3 && str.charAt(2) != '-' && !Character.isWhitespace(str.charAt(2))) //
                ;
    }

    /**
     * 解析命令选项模版
     *
     * @param str 表达式
     */
    public void parse(String str) {
        if (str.charAt(0) == '-') { // 解析单个选项
            if (str.length() >= 3 && str.charAt(1) == '-') { // 长选项
                CommandOptionPattern option = new CommandOptionPattern(this.expr, str);
                if (this.patterns.put(option.getName(), option) != null) {
                    throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg071", this.expr.getCommand(), "-" + option.getName()));
                }
            } else {
                String tmp = StringUtils.ltrim(str, '-'); // 选项名
                String name = tmp;
                int index = tmp.indexOf(':');
                String right = "";
                if (index != -1) {
                    name = tmp.substring(0, index);
                    right = tmp.substring(index);
                }

                for (int i = 0; i < name.length(); i++) {
                    char c = name.charAt(i);
                    String pattern = "-" + c + right; // 选项规则
                    CommandOptionPattern option = new CommandOptionPattern(this.expr, pattern);
                    if (this.patterns.put(option.getName(), option) != null) {
                        throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg071", this.expr.getCommand(), "-" + option.getName()));
                    }
                }
            }
        }

        // [-t|-v|-f] 或 (-t|-v|-f)
        else {
            String[] array = StringUtils.trimBlank(StringUtils.split(str.substring(1, str.length() - 1), '|'));
            Set<String> list = new HashSet<String>();
            for (String optExpr : array) {
                if (this.isOption(optExpr)) {
                    CommandOptionPattern pattern = new CommandOptionPattern(this.expr, optExpr);
                    this.patterns.put(pattern.getName(), pattern);
                    list.add(pattern.getName());
                } else {
                    throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg071", this.expr.getCommand(), optExpr));
                }
            }

            if (str.charAt(0) == '(') {
                this.mustExists.add(list);
            }

            // 添加不能同时使用的选项名规则
            if (list.size() > 1) {
                for (String name : list) {
                    HashSet<String> set = new HashSet<String>();
                    for (String s : list) {
                        if (!name.equals(s)) {
                            set.add(s);
                        }
                    }
                    this.rules.put(name, set);
                }
            }
        }

    }

    /**
     * 判断是否支持选项名
     *
     * @param name 选项名
     * @return 返回 true 表示支持选项名
     */
    public boolean supportName(String name) {
        return this.patterns.containsKey(name);
    }

    /**
     * 判断选项名是否支持选项值
     *
     * @param name 选项名
     * @return 返回 true 表示选项名支持选项值
     */
    public boolean supportValue(String name) {
        CommandOptionPattern pattern = this.patterns.get(name);
        return pattern != null && pattern.containsValue();
    }

    /**
     * 添加命令的选项信息
     *
     * @param value 选项值
     */
    public void addOption(CommandOptionValue value) {
        this.values.put(value.getName(), value);
    }

    /**
     * 判断是否存在指定选项名
     *
     * @param name 选项名
     * @return 返回true表示存在选项
     */
    public boolean containsOption(String name) {
        return this.values.containsKey(name);
    }

    /**
     * 返回选项的值
     *
     * @param name 选项名
     * @return 选项值
     */
    public String getOption(String name) {
        CommandOptionValue option = this.values.get(name);
        return option == null ? null : option.getValue();
    }

    /**
     * 返回所有选项名
     *
     * @return 选项名
     */
    public String[] getOptionNames() {
        String[] array = new String[this.values.size()];
        return this.values.keySet().toArray(array);
    }

    /**
     * 判断是否没有任何选项
     *
     * @return 返回true表示没有任何选项 false表示存在选项
     */
    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    /**
     * 返回选项个数
     *
     * @return 选项个数
     */
    public int size() {
        return this.values.size();
    }

    /**
     * 校验命令的选项是否复合命令选项定义
     */
    public void check() {
        // 检查选项名不能同时使用的规则
        Set<String> names = this.rules.keySet();
        for (String name : names) { // 遍历所有选项名
            if (this.values.containsKey(name)) {
                Set<String> errornames = this.rules.get(name); // 不能同时使用的选项名集合
                for (String errorname : errornames) {
                    if (this.values.containsKey(errorname)) {
                        throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg074", this.expr.getCommand(), "-" + name, "-" + errorname));
                    }
                }
            }
        }

        for (Set<String> opts : this.mustExists) {
            boolean notExists = true;
            for (String name : opts) {
                if (this.values.containsKey(name)) {
                    notExists = false;
                    break;
                }
            }

            if (notExists) {
                StringBuilder buf = new StringBuilder(20);
                for (Iterator<String> it = opts.iterator(); it.hasNext(); ) {
                    String name = it.next();
                    buf.append('-');
                    buf.append(name);
                    if (it.hasNext()) {
                        buf.append(" ");
                    }
                }
                throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg075", this.expr.getCommand(), buf));
            }
        }

        // 检查选项值是否复合规则
        names = this.values.keySet();
        for (String name : names) {
            if (this.patterns.containsKey(name)) {
                CommandOptionPattern pattern = this.patterns.get(name);
                String regex = pattern.getFormat();
                if (StringUtils.isNotBlank(regex)) {
                    CommandOptionValue option = this.values.get(name);
                    String value = option.getValue();
                    if (value != null) { // 校验参数值是否复合规则
                        if (regex.equalsIgnoreCase("date")) {
                            if (Dates.testParse(value) == null) {
                                throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg076", this.expr.getCommand(), "-" + option.getName(), value, regex));
                            }
                        } else if (!value.matches(regex)) {
                            throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg076", this.expr.getCommand(), "-" + option.getName(), value, regex));
                        }
                    }
                }
            }
        }
    }

    public String toString() {
        return super.toString();
    }

}
