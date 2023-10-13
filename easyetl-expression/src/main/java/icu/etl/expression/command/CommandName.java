package icu.etl.expression.command;

import java.util.HashSet;
import java.util.Set;

import icu.etl.collection.CaseSensitivSet;
import icu.etl.expression.CommandExpression;
import icu.etl.expression.ExpressionException;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public class CommandName {

    /** 表达式信息 */
    private CommandExpression expr;

    /** true表示取反 */
    private boolean supportReverse;

    /** true表示取反 */
    private boolean reverse;

    /** 实际命令名 */
    private String value;

    /** 命令名字的范围 */
    private Set<String> rangs;

    /**
     * 初始化
     */
    public CommandName(CommandExpression expr) {
        this.expr = expr;
    }

    /**
     * 判断字符串参数是否复合命令名的规则
     *
     * @param str
     * @return
     */
    public boolean match(String str) {
        return str != null && str.length() > 0 && (str.charAt(0) == '!' || StringUtils.isLetter(str.charAt(0)));
    }

    /**
     * 解析命令名字表达式
     *
     * @param pattern 命令名字的范围表达式: name !name !name|name1 <br>
     *                在命令名前使用 ! 符号表示命令支持反转操作
     */
    public void parse(String pattern) {
        this.rangs = this.expr.getAnalysis().ignoreCase() ? new CaseSensitivSet() : new HashSet<String>();
        this.supportReverse = pattern.startsWith("!");
        pattern = this.supportReverse ? pattern.substring(1) : pattern;
        String[] array = StringUtils.split(pattern, '|');
        for (String name : array) {
            if (StringUtils.isNotBlank(name)) {
                this.rangs.add(name);
            }
        }
    }

    /**
     * 返回实际命令名
     *
     * @return
     */
    public String getValue() {
        return this.value;
    }

    /**
     * 校验名字是否在范围内
     *
     * @param name
     */
    public void setValue(String name) {
        this.reverse = name.startsWith("!");
        this.value = this.reverse ? name.substring(1) : name;
    }

    /**
     * 校验命令名是否复合要求
     */
    public void check() {
        if (this.rangs != null && !this.rangs.isEmpty() && !this.rangs.contains(this.value)) {
            throw new ExpressionException(ResourcesUtils.getExpressionMessage(72, this.expr.getCommand(), this.value, StringUtils.join(this.rangs, ", ")));
        }

        // 检查命令取反符号使用是否正确
        if (!this.supportReverse && this.reverse) {
            throw new ExpressionException(ResourcesUtils.getExpressionMessage(73, this.expr.getCommand()));
        }
    }

    /**
     * 返回 true 表示命令支持反转
     *
     * @return
     */
    public boolean isReverse() {
        return this.reverse;
    }

    /**
     * 返回true 表示命令名支持使用 ! 符号
     *
     * @return
     */
    public boolean supportReverse() {
        return supportReverse;
    }

}
