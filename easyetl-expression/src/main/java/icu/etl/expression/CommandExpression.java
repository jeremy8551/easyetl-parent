package icu.etl.expression;

import java.util.Collections;
import java.util.List;

import icu.etl.expression.command.CommandName;
import icu.etl.expression.command.CommandOptionList;
import icu.etl.expression.command.CommandOptionValue;
import icu.etl.expression.command.CommandParameter;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 命令表达式
 *
 * @author jeremy8551@qq.com
 */
public class CommandExpression {

    /** 语句分析器 */
    protected Analysis analysis;

    /** 命令语句 */
    protected String command;

    /** 命令规则 */
    protected String pattern;

    /** 命令名 */
    protected CommandName name;

    /** 选项 */
    protected CommandOptionList option;

    /** 参数 */
    protected CommandParameter parameter;

    /**
     * 初始化
     *
     * @param analysis 语句分析器
     * @param pattern  命令规则, 详见: {@linkplain #parse(String)}
     * @param command  命令语句
     */
    public CommandExpression(Analysis analysis, String pattern, String command) {
        this.prepared(analysis);
        this.parse(pattern);
        this.setValue(command);
    }

    /**
     * 初始化
     *
     * @param pattern 命令规则, 详见: {@linkplain #parse(String)}
     * @param command 命令语句
     */
    public CommandExpression(String pattern, String command) {
        this(new StandardAnalysis(), pattern, command);
    }

    /**
     * 初始化
     *
     * @param analysis 分析器
     */
    protected void prepared(Analysis analysis) {
        this.analysis = Ensure.notNull(analysis);
        this.name = new CommandName(this);
        this.option = new CommandOptionList(this);
        this.parameter = new CommandParameter(this);
    }

    /**
     * 解析命令
     *
     * @param pattern 命令表达式由三部分组成： <br>
     *                <br>
     *                <br>
     *                第一部分是定义命令的名字: <br>
     *                set|var 用竖线表示命令名的范围可以是 set 或 var <br>
     *                可以在命令名字前使用 ! 符号表示命令支持取反操作 <br>
     *                <br>
     *                <br>
     *                第二部分是定义命令的选项: <br>
     *                -n 表示选项 <br>
     *                --name 表示长选项（通常可以使用 = 符号为长选项赋值）<br>
     *                -ni: 选项后面使用 : 符号表示选项后面如果有值的话，则该值是选项的值（不会作为命令参数） <br>
     *                -d:date 符号后面使用 date 表示选项值只能是日期 <br>
     *                -r:'\\d+' 符号后面可以使用正则表达式表示参数的格式 <br>
     *                [-x|-v:int|-f:date|r:\d+] 中括号中的内容表示三个选项只能同时存在一个 <br>
     *                (-x|-v:int|-f:date|r:\d+) 小括号中的内容表示三个选项在命令中必须要使用一个 <br>
     *                单引号和双引号中的内容作为选项值或参数值 <br>
     *                <br>
     *                <br>
     *                第三部分是定义命令的参数: <br>
     *                {0-1|5} 大括号内表示参数个数，可以使用范围如: 0-10 表示可以有0到10个参数，为0时表示命令没有参数，竖线分割表示参数范围 <br>
     *                未设置参数时，不会对命令表达中参数进行检查 <br>
     *                <br>
     *                <br>
     *                如: !isDate|date -c -ivf [-s:date] [-d:'\\d+{8}'] --prefix: --name [-x|-v|-f] {0-2|5|6}
     */
    protected void parse(String pattern) {
        this.clear();

        this.pattern = pattern;
        pattern = StringUtils.trimBlank(pattern);
        if (pattern == null || pattern.length() == 0) {
            return;
        }

        // 解析表达式
        List<String> list = this.analysis.split(pattern);
        for (String str : list) {
            // 解析选项相关表达式
            if (this.option.match(str)) {
                this.option.parse(str);
            }

            // 设置命令参数规则
            else if (this.parameter.match(str)) {
                this.parameter.parse(str);
            }

            // 设置命令名的规则
            else if (this.name.match(str)) {
                this.name.parse(str);
            } else {
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(70, this.pattern, str));
            }
        }
    }

    /**
     * 判断字符串参数是否与命令定义规则相符
     *
     * @param command 命令表达式
     */
    protected void setValue(String command) {
        this.command = Ensure.notNull(command);
        for (int i = 0, count = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            if (!Character.isWhitespace(c)) { // 查找单词起始位置
                int j = this.analysis.indexOfWhitespace(command, i + 1); // 搜索单词结束位置
                if (j == -1) {
                    j = command.length();
                }

                // 如果是第一个单词，则先作为命令名
                if (++count == 1) {
                    String word = command.substring(i, j);
                    this.name.setValue(word);
                    this.name.check();
                    i = j - 1;
                    continue;
                }

                i = this.parseWord(command, i, j);
            }
        }

        this.option.check(); // 校验选项是否复合规则
        this.parameter.check(); // 校验参数是否复合规则
    }

    protected int parseWord(String str, int begin, int end) {
        String word = str.substring(begin, end);

        // 命令的选项
        if (this.option.isOption(word)) {
            char nc = word.charAt(1);

            // 长选项 --prefix
            if (nc == '-') {
                String tmp = word.substring(2); // 长选项名
                int b = tmp.indexOf('=');
                String optionName = (b == -1) ? tmp : tmp.substring(0, b);
                String optionValue = (b == -1 || b == tmp.length() - 1) ? null : tmp.substring(b + 1); // 长选项的值

                if (!this.option.supportName(optionName)) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(71, this.command, optionName));
                }

                // 如果可以有选项值
                if (this.option.supportValue(optionName)) {
                    if (optionValue == null) {
                        Word next = this.readNextWord(str, end);
                        if (next != null) {
                            if (!this.option.match(next.getContent())) {
                                this.option.addOption(new CommandOptionValue(optionName, next.getContent(), true));
                                return next.getEnd() - 1;
                            } else {
                                this.option.addOption(new CommandOptionValue(optionName, null, true));
                                return end - 1;
                            }
                        } else {
                            this.option.addOption(new CommandOptionValue(optionName, null, true));
                            return end - 1;
                        }
                    } else {
                        this.option.addOption(new CommandOptionValue(optionName, optionValue, true));
                        return end - 1;
                    }
                } else {
                    if (StringUtils.isNotBlank(optionValue)) {
                        throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg079", this.command, "-" + optionName));
                    } else {
                        this.option.addOption(new CommandOptionValue(optionName, null, true));
                        return end - 1;
                    }
                }
            } else { // 解析短选项
                if (word.length() > 2) { // 解析复合选项: -xvf 复合选项不能有选项值
                    for (int i = 1; i < word.length(); i++) {
                        String optionName = String.valueOf(word.charAt(i));
                        if (this.option.supportName(optionName)) {
                            this.option.addOption(new CommandOptionValue(optionName, null, false));
                        } else {
                            throw new ExpressionException(ResourcesUtils.getExpressionMessage(71, this.command, "-" + optionName));
                        }
                    }
                    return end - 1;
                } else { // 解析单选项-
                    String optionName = word.substring(1);
                    if (!this.option.supportName(optionName)) {
                        throw new ExpressionException(ResourcesUtils.getExpressionMessage(71, this.command, word));
                    }

                    // 如果选项可以有值
                    if (this.option.supportValue(optionName)) {
                        Word next = this.readNextWord(str, end);
                        if (next != null && !this.option.match(next.getContent())) {
                            this.option.addOption(new CommandOptionValue(optionName, next.getContent(), false));
                            return next.getEnd() - 1;
                        }
                    }

                    this.option.addOption(new CommandOptionValue(optionName, null, false));
                    return end - 1;
                }
            }
        }

        // 如果是参数
        if (this.parameter.onlyOne()) { // 如果只能有一个参数
            String parameter = StringUtils.rtrimBlank(str.substring(begin));
            String option = this.parseOption(parameter);
            if (option != null) {
                throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg080", this.command, parameter, option));
            }
            this.parameter.add(parameter);
            return str.length() - 1;
        } else {
            this.parameter.add(word);
            return end - 1;
        }
    }

    /**
     * 解析字符串中是否包含的选项
     *
     * @param str 字符串
     * @return 返回选项信息
     */
    public String parseOption(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '-') {
                int p = i - 1;
                if (p < 0 || Character.isWhitespace(str.charAt(p))) {
                    int next = i + 1;
                    if (next >= str.length() || StringUtils.isLetter(str.charAt(next)) || StringUtils.isNumber(str.charAt(next))) {
                        return str.substring(i, next + 1);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 读取一下单词
     *
     * @param str   字符串
     * @param begin 空白字符的起始位置
     * @return 单词
     */
    protected Word readNextWord(String str, int begin) {
        int start = StringUtils.indexOfNotBlank(str, begin, -1);
        if (start == -1) {
            return null;
        }

        int end = this.analysis.indexOfWhitespace(str, start + 1);
        if (end == -1) {
            end = str.length();
        }
        return new Word(start, end, str.substring(start, end));
    }

    /**
     * 返回true表示命令前存在 ! 符号
     *
     * @return 返回true表示命令前存在 ! 符号 false表示没有取反符号
     */
    public boolean isReverse() {
        return this.name.isReverse();
    }

    /**
     * 命令名, 如: echo
     *
     * @return 命令名
     */
    public String getName() {
        return this.name.getValue();
    }

    /**
     * 判断是否包含指定选项
     *
     * @param array 选项名数组, 如: -d
     * @return 返回true表示包含选项 false表示不包含选项
     */
    public boolean containsOption(String... array) {
        for (String name : array) {
            if (!this.option.containsOption(StringUtils.ltrim(name, '-'))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 返回选项值
     *
     * @param name 选项名, 如: -l 或 --lang
     * @return 选项值
     */
    public String getOptionValue(String name) {
        return this.option.getOption(StringUtils.ltrim(name, '-'));
    }

    /**
     * 返回所有选项名
     *
     * @return 选项名
     */
    public String[] getOptionNames() {
        return this.option.getOptionNames();
    }

    /**
     * 返回命令的所有参数集合
     *
     * @return 参数集合
     */
    public List<String> getParameters() {
        return Collections.unmodifiableList(this.parameter.getValues());
    }

    /**
     * 返回命令参数个数
     *
     * @return 参数个数
     */
    public int getParameterSize() {
        return this.parameter.size();
    }

    /**
     * 返回第 n 个参数值
     *
     * @param n 从 1 开始
     * @return 参数值
     */
    public String getParameter(int n) {
        return this.parameter.get(Ensure.isFromOne(n) - 1);
    }

    /**
     * 返回第一个参数值
     *
     * @return 参数值
     */
    public String getParameter() {
        return this.getParameter(1);
    }

    /**
     * 清空信息
     */
    protected void clear() {
    }

    /**
     * 返回语句分析器
     *
     * @return 语句分析器
     */
    public Analysis getAnalysis() {
        return analysis;
    }

    /**
     * 返回命令语句
     *
     * @return 语句
     */
    public String getCommand() {
        return command;
    }

    /**
     * 返回命令规则
     *
     * @return 命令规则
     */
    public String getPattern() {
        return pattern;
    }

    public String toString() {
        return this.command;
    }

}
