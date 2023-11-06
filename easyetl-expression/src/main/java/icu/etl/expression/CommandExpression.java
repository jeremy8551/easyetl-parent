package icu.etl.expression;

import java.util.Collections;
import java.util.List;

import icu.etl.expression.command.CommandName;
import icu.etl.expression.command.CommandOptionList;
import icu.etl.expression.command.CommandOptionValue;
import icu.etl.expression.command.CommandParameter;
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
     * @param analysis
     */
    protected void prepared(Analysis analysis) {
        if (analysis == null) {
            throw new NullPointerException();
        }

        this.analysis = analysis;
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
        if (command == null) {
            throw new NullPointerException();
        }

        this.command = command;
        List<String> list = this.analysis.split(StringUtils.trimBlank(command));

        // 位置信息
        int index = 0;

        // 解析命令名字
        if (list.size() > 0 && this.name.match(list.get(index))) {
            String name = list.get(0);
            this.name.setValue(name);
            this.name.check();
            index++;
        }

        // 解析选项与参数
        while (index < list.size()) {
            String str = list.get(index);
            int next = index + 1; // 下一个字符串

            if (this.option.isOption(str)) { // 命令的选项
                char nc = str.charAt(1);
                if (nc == '-') { // 长选项 --prefix
                    String tmp = str.substring(2); // 长选项名
                    int b = tmp.indexOf('=');
                    String name = (b == -1) ? tmp : tmp.substring(0, b);
                    String value = (b == -1) ? null : tmp.substring(b + 1); // 长选项的值

                    if (!this.option.supportName(name)) {
                        throw new ExpressionException(ResourcesUtils.getExpressionMessage(71, this.command, name));
                    }

                    if (value == null && next < list.size()) {
                        String nextStr = list.get(next);
                        if (this.option.match(nextStr) || !this.option.supportValue(name)) {
                            this.option.addOption(new CommandOptionValue(name, null, true));
                        } else {
                            this.option.addOption(new CommandOptionValue(name, nextStr, true));
                            index = next;
                        }
                    } else {
                        this.option.addOption(new CommandOptionValue(name, value, true));
                    }
                } else { // 解析短选项
                    if (str.length() > 2) { // 解析复合选项: -xvf 复合选项不能有选项值
                        for (int i = 1; i < str.length(); i++) {
                            String name = String.valueOf(str.charAt(i));
                            if (!this.option.supportName(name)) {
                                throw new ExpressionException(ResourcesUtils.getExpressionMessage(71, this.command, "-" + name));
                            }
                            this.option.addOption(new CommandOptionValue(name, null, false));
                        }
                    } else { // 解析单选项
                        String name = str.substring(1);
                        if (!this.option.supportName(name)) {
                            throw new ExpressionException(ResourcesUtils.getExpressionMessage(71, this.command, str));
                        }

                        if (next < list.size()) {
                            String nextStr = list.get(next);
                            if (this.option.match(nextStr) || !this.option.supportValue(name)) {
                                this.option.addOption(new CommandOptionValue(name, null, false));
                            } else {
                                this.option.addOption(new CommandOptionValue(name, nextStr, false));
                                index = next;
                            }
                        } else {
                            this.option.addOption(new CommandOptionValue(name, null, false));
                        }
                    }
                }
            } else { // 解析命令的参数
                this.parameter.add(str);
            }

            index++;
        }

        this.option.check(); // 校验选项是否复合规则
        this.parameter.check(); // 校验参数是否复合规则
    }

    /**
     * 返回true表示命令前存在 ! 符号
     *
     * @return
     */
    public boolean isReverse() {
        return this.name.isReverse();
    }

    /**
     * 命令名, 如: echo
     *
     * @return
     */
    public String getName() {
        return this.name.getValue();
    }

    /**
     * 判断是否包含指定选项
     *
     * @param array 选项名数组, 如: -d
     * @return
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
     * @param name 选项名, 如: -d
     * @return 选项值
     */
    public String getOptionValue(String name) {
        return this.option.getOption(StringUtils.ltrim(name, '-'));
    }

    /**
     * 返回所有选项名
     *
     * @return
     */
    public String[] getOptionNames() {
        return this.option.getOptionNames();
    }

    /**
     * 返回命令的所有参数集合
     *
     * @return
     */
    public List<String> getParameters() {
        return Collections.unmodifiableList(this.parameter.getValues());
    }

    /**
     * 返回第 n 个参数值
     *
     * @param n 从 1 开始
     * @return
     */
    public String getParameter(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(String.valueOf(n));
        } else if (n <= this.parameter.getValues().size()) {
            return this.parameter.get(n - 1);
        } else {
            return null;
        }
    }

    /**
     * 返回第一个参数值
     *
     * @return
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
     * @return
     */
    public Analysis getAnalysis() {
        return analysis;
    }

    /**
     * 返回命令语句
     *
     * @return
     */
    public String getCommand() {
        return command;
    }

    /**
     * 返回命令规则
     *
     * @return
     */
    public String getPattern() {
        return pattern;
    }

    public String toString() {
        return this.command;
    }

}
