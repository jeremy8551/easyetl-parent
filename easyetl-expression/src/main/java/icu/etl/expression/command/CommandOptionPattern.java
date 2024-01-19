package icu.etl.expression.command;

import java.util.ArrayList;
import java.util.List;

import icu.etl.expression.CommandExpression;
import icu.etl.expression.ExpressionException;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public class CommandOptionPattern {

    /** 选项名 */
    private String name;

    /** true表示必须有值 */
    private boolean containsValue;

    /** true 表示长选项 */
    private boolean islong;

    /** 选项值的格式 */
    private String format;

    /**
     * 初始化
     *
     * @param str -t -t: -tvf: -tvf:date --filename
     */
    public CommandOptionPattern(CommandExpression expr, String str) {
        if (str == null || str.length() == 0 || str.charAt(0) != '-') {
            throw new IllegalArgumentException(str);
        }

        this.islong = str.charAt(1) == '-';
        List<String> list = new ArrayList<String>();
        StringUtils.split(str.substring(this.islong ? 2 : 1), ':', list);
        switch (list.size()) {
            case 1:
                this.name = list.get(0);
                this.containsValue = false;
                this.format = "";
                break;

            case 2:
                this.name = list.get(0);
                this.containsValue = true;
                this.format = expr.getAnalysis().unQuotation(list.get(1));
                break;

            default:
                throw new ExpressionException(ResourcesUtils.getMessage("expression.standard.output.msg070", expr.getPattern(), str));
        }
    }

    /**
     * 返回选项名
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 返回 true 表示选项有选项值
     *
     * @return
     */
    public boolean containsValue() {
        return containsValue;
    }

    /**
     * 返回 true 表示是长选项
     *
     * @return
     */
    public boolean islong() {
        return islong;
    }

    /**
     * 返回选项值的格式
     *
     * @return
     */
    public String getFormat() {
        return format;
    }

    public String toString() {
        return "CommandOptionPattern [name=" + name + ", containsValue=" + containsValue + ", islong=" + islong + ", format=" + format + "]";
    }

}
