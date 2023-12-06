package icu.etl.expression;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import icu.etl.expression.operation.AndOper;
import icu.etl.expression.operation.DivOper;
import icu.etl.expression.operation.EqualsOper;
import icu.etl.expression.operation.GreaterEqualsOper;
import icu.etl.expression.operation.GreaterOper;
import icu.etl.expression.operation.InOper;
import icu.etl.expression.operation.LessEqualsOper;
import icu.etl.expression.operation.LessOper;
import icu.etl.expression.operation.ModOper;
import icu.etl.expression.operation.MupliOper;
import icu.etl.expression.operation.NotEqualsOper;
import icu.etl.expression.operation.NotInOper;
import icu.etl.expression.operation.Operator;
import icu.etl.expression.operation.OrOper;
import icu.etl.expression.operation.PlusOper;
import icu.etl.expression.operation.SubOper;
import icu.etl.expression.operation.TreeOper;
import icu.etl.expression.parameter.ArrayParameter;
import icu.etl.expression.parameter.ComplexParameter;
import icu.etl.expression.parameter.DateUnitParameter;
import icu.etl.expression.parameter.ExpressionParameter;
import icu.etl.expression.parameter.Parameter;
import icu.etl.expression.parameter.TwoParameter;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 表达式解析器
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-21 13:28:52
 */
public abstract class Parser {

    /**
     * 默认解析器
     */
    private static Parser defaultFormat;

    /**
     * 默认解析器
     *
     * @return 解析器
     */
    public static Parser getFormat() {
        if (defaultFormat == null) {
            defaultFormat = new Parser(new StandardAnalysis()) {

                public int parse(String array, int start, boolean isData, List<Parameter> datas, List<Operator> operation) throws ExpressionException {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(26, String.valueOf(array), start + 1), start + 1);
                }
            };
        }
        return defaultFormat;
    }

    /**
     * 默认解析器
     *
     * @param format 解析器
     */
    public static void setFormat(Parser format) {
        Parser.defaultFormat = format;
    }

    /** 分析器 */
    protected Analysis analysis;

    /**
     * 初始化
     */
    public Parser(Analysis analysis) {
        this.analysis = analysis;
    }

    /**
     * 解析表达式
     *
     * @param str       表达式
     * @param index     当前字符数组所在位置（大于等于0且小于字符数组长度）
     * @param isData    true表示上一个是数值; false表示上一个是操作符
     * @param datas     表达式数值
     * @param operation 操作符对象
     * @return 记录解析符号位置 必须大于等0小于字符数组长度
     * @throws ExpressionException 表达式不合法
     */
    public abstract int parse(String str, int index, boolean isData, List<Parameter> datas, List<Operator> operation) throws ExpressionException;

    /**
     * 解析表达式，返回运算公式
     *
     * @param str 表达式
     * @return 运算公式
     */
    public Formula parse(String str) {
        ArrayList<Operator> operations = new ArrayList<Operator>(10);
        ArrayList<Parameter> datas = new ArrayList<Parameter>(10);
        boolean isData = false; // true表示上一个是数值 false表示上一个是操作符
        for (int i = 0, length = str.length(); i < length; i++) {
            char c = str.charAt(i);
            int next = i + 1; // 下一个位置

            // 忽略空白字符
            if (Character.isWhitespace(c)) {
                continue;
            }

            // 解析加号
            else if (c == '+') {
                if (isData) { // 表示加法符
                    PlusOper oper = new PlusOper();
                    operations.add(oper);
                    isData = false;
                }
                // 表示正整数符时，可以忽略向下执行
                continue;
            }

            // 减法 或 负数
            else if (c == '-') {
                if (isData) {
                    SubOper oper = new SubOper();
                    operations.add(oper);
                    isData = false;
                } else {
                    if (next >= length || "0123456789".indexOf(str.charAt(next)) == -1) {
                        throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                    }

                    int index = this.analysis.indexOfFloat(str, next);
                    String value = str.substring(i, index);
                    if (value.indexOf('.') == -1) {
                        datas.add(new ExpressionParameter(Parameter.LONG, new Long(value)));
                    } else {
                        datas.add(new ExpressionParameter(Parameter.DOUBLE, new Double(value)));
                    }
                    i = index - 1;
                    isData = true;
                }
                continue;
            }

            // 乘法
            else if (c == '*') {
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }

                MupliOper oper = new MupliOper();
                operations.add(oper);
                isData = false;
                continue;
            }

            // 除法
            else if (c == '/') {
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }

                DivOper oper = new DivOper();
                operations.add(oper);
                isData = false;
                continue;
            }

            // 取余
            else if (c == '%') {
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }

                ModOper oper = new ModOper();
                operations.add(oper);
                isData = false;
                continue;
            }

            // 解析并且
            else if (c == '&') {
                if (!isData || next >= length) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }
                if (str.charAt(next) != '&') {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(29, str, next), next);
                }

                AndOper oper = new AndOper();
                operations.add(oper);
                i = next;
                isData = false;
                continue;
            }

            // 解析并且 and
            else if ((c == 'a' || c == 'A') && this.analysis.indexOf(str, "and", i, 1, 1) == i) {
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }

                AndOper oper = new AndOper();
                operations.add(oper);
                i = i + 2;
                isData = false;
                continue;
            }

            // 解析或
            else if (c == '|') {
                if (!isData || next >= length) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }
                if (str.charAt(next) != '|') {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(30, str, next), next);
                }

                OrOper oper = new OrOper();
                operations.add(oper);
                i = next;
                isData = false;
                continue;
            }

            // 解析或 or
            else if ((c == 'o' || c == 'O') && this.analysis.indexOf(str, "or", i, 1, 1) == i) {
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }

                OrOper oper = new OrOper();
                operations.add(oper);
                i = next;
                isData = false;
                continue;
            }

            // 解析大于 和 大于等于
            else if (c == '>') {
                if (!isData || next >= length) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }

                if (str.charAt(next) == '=') {
                    operations.add(new GreaterEqualsOper());
                    i = next;
                } else {
                    operations.add(new GreaterOper());
                }
                isData = false;
                continue;
            }

            // 解析小于 和 小于等于
            else if (c == '<') {
                if (!isData || next >= length) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }

                if (str.charAt(next) == '=') {
                    operations.add(new LessEqualsOper());
                    i = next;
                } else {
                    operations.add(new LessOper());
                }
                isData = false;
                continue;
            }

            // 解析等于
            else if (c == '=') {
                if (!isData || next >= length) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }
                if (str.charAt(next) != '=') {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(31, str, next), next);
                }

                operations.add(new EqualsOper());
                i = next;
                isData = false;
                continue;
            }

            // 解析不等于
            else if (c == '!' && next < length && str.charAt(next) == '=') { // 只解析不等于, 取反操作放在扩展方法中实现
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }

                operations.add(new NotEqualsOper());
                i = next;
                isData = false;
                continue;
            }

            // 解析括号
            else if (c == '(') {
                int index = this.analysis.indexOfParenthes(str, i);
                if (index == -1) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(32, str, next), next);
                }

                // 如果上一个是 in 操作符
                else if (!isData && operations.size() > 0 && (operations.get(operations.size() - 1) instanceof InOper)) {
                    String content = StringUtils.trimBlank(str.substring(next, index));
                    List<String> list = this.analysis.split(content, this.analysis.getSegment());
                    ArrayParameter parameter = new ArrayParameter();
                    for (String expression : list) {
                        Expression expr = new Expression(expression);
                        parameter.add(new ExpressionParameter(expr.getType(), expr.value()));
                    }
                    datas.add(parameter);
                } else { // 正常括号
                    String content = str.substring(next, index);
                    datas.add(new ComplexParameter(content));
                }

                i = index;
                isData = true;
                continue;
            }

            // 非法字符
            else if (c == ')') {
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(33, str, next), next);
            }

            // 字符常量
            else if (c == '\'') {
                if (isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(34, str, next), next);
                }
                int index = this.analysis.indexOfQuotation(str, i);
                if (index == -1) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(27, str, next), next);
                }

                String content = this.analysis.unescapeString(str.substring(next, index));
                datas.add(new ExpressionParameter(Parameter.STRING, content));
                i = index;
                isData = true;
                continue;
            }

            // 字符变量
            else if (c == '\"') {
                if (isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(34, str, next), next);
                }
                int index = this.analysis.indexOfDoubleQuotation(str, i);
                if (index == -1) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(27, str, next), next);
                }

                String content = this.analysis.unescapeString(str.substring(next, index));
                datas.add(new ExpressionParameter(Parameter.STRING, content));
                i = index;
                isData = true;
                continue;
            }

            // 十六进制数 0x1e4d 和八进制数 01234
            else if (c == '0') {
                if (isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(34, str, next), next);
                }

                if (next < length) {
                    char nc = str.charAt(next);
                    if (nc == 'x' || nc == 'X') { // 十六进制
                        int start = next + 1;
                        int index = this.analysis.indexOfHex(str, start);
                        if (index == -1) {
                            throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                        }

                        datas.add(new ExpressionParameter(Parameter.LONG, Long.parseLong(str.substring(start, index), 16)));
                        i = index - 1;
                        isData = true;
                        continue;
                    } else if ("0123456789".indexOf(nc) != -1) { // 八进制
                        int index = this.analysis.indexOfOctal(str, next);
                        if (index == -1) {
                            throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                        }

                        datas.add(new ExpressionParameter(Parameter.LONG, Long.parseLong(str.substring(next, index), 8)));
                        i = index - 1;
                        isData = true;
                        continue;
                    }
                }

                int index = this.analysis.indexOfFloat(str, i);
                String content = str.substring(i, index);
                if (content.indexOf('.') == -1) {
                    datas.add(new ExpressionParameter(Parameter.LONG, new Long(content)));
                } else {
                    datas.add(new ExpressionParameter(Parameter.DOUBLE, new Double(content)));
                }
                i = index - 1;
                isData = true;
                continue;
            }

            // 解析数字
            else if ("0123456789".indexOf(c) != -1) {
                if (isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(34, str, next), next);
                }

                int index = this.analysis.indexOfFloat(str, i);
                String content = str.substring(i, index);
                if (content.indexOf('.') == -1) {
                    datas.add(new ExpressionParameter(Parameter.LONG, new Long(content)));
                } else {
                    datas.add(new ExpressionParameter(Parameter.DOUBLE, new Double(content)));
                }
                i = index - 1;
                isData = true;
                continue;
            }

            // 三目运算符 boolean ? value1 : value2
            else if (c == '?') {
                if (datas.isEmpty()) { // 如果是三目操作则只能有一个操作符
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(35, str), i);
                }

                int index = this.analysis.indexOfSemicolon(str, i);
                if (index == -1) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(36, str), i);
                }

                ComplexParameter trueRun = new ComplexParameter(str.substring(next, index));
                ComplexParameter falseRun = new ComplexParameter(str.substring(index + 1));

                TwoParameter data = new TwoParameter();
                data.setTrueRun(trueRun);
                data.setFalseRun(falseRun);

                TreeOper oper = new TreeOper();
                operations.add(oper);
                datas.add(data);
                i = length;
                isData = true;
                continue;
            }

            // 在范围内 in
            else if ((c == 'i' || c == 'I') && this.analysis.indexOf(str, "in", i, 0, 1) == i) {
                if (!isData || next >= length) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }

                InOper oper = new InOper();
                operations.add(oper);
                i = next;
                isData = false;
                continue;
            }

            // 不在范围内 not in
            else if ((c == 'n' || c == 'N') && this.analysis.indexOf(str, "not", i, 1, 0) == i && this.analysis.startsWith(str, "in", i + 3, true)) {
                int index = str.indexOf("in", i + 3); // 查找 not in 语句中 in 关键字的位置
                if (index == -1) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(5, str, i + 4), i + 4);
                }
                index += "in".length(); // in 关键字的结束位置
                if (!isData || index >= str.length() || !this.analysis.charAt(str, index, 1)) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, index), index);
                }

                NotInOper oper = new NotInOper();
                operations.add(oper);
                i = index - 1;
                isData = false;
                continue;
            }

            // 日 day
            else if ((c == 'd' || c == 'D') && this.analysis.indexOf(str, "day", i, 2, 1) == i) {
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }
                int index = datas.size() - 1;
                if (index < 0) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(4, str, next), next);
                }

                datas.set(index, new DateUnitParameter(datas.get(index), Calendar.DAY_OF_MONTH));
                i = i + "day".length() - 1;
                continue;
            }

            // 月份 month
            else if ((c == 'm' || c == 'M') && this.analysis.indexOf(str, "month", i, 2, 1) == i) {
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }
                int index = datas.size() - 1;
                if (index < 0) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(4, str, next), next);
                }

                datas.set(index, new DateUnitParameter(datas.get(index), Calendar.MONTH));
                i = i + "month".length() - 1;
                continue;
            }

            // 年份 year
            else if ((c == 'y' || c == 'Y') && this.analysis.indexOf(str, "year", i, 2, 1) == i) {
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }
                int index = datas.size() - 1;
                if (index < 0) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(4, str, next), next);
                }

                datas.set(index, new DateUnitParameter(datas.get(index), Calendar.YEAR));
                i = i + "year".length() - 1;
                continue;
            }

            // 小时 hour
            else if ((c == 'h' || c == 'H') && this.analysis.indexOf(str, "hour", i, 2, 1) == i) {
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }
                int index = datas.size() - 1;
                if (index < 0) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(4, str, next), next);
                }

                datas.set(index, new DateUnitParameter(datas.get(index), Calendar.HOUR));
                i = i + "hour".length() - 1;
                continue;
            }

            // 分钟 minute
            else if ((c == 'm' || c == 'M') && this.analysis.indexOf(str, "minute", i, 2, 1) == i) {
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }
                int index = datas.size() - 1;
                if (index < 0) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(4, str, next), next);
                }

                datas.set(index, new DateUnitParameter(datas.get(index), Calendar.MINUTE));
                i = i + "minute".length() - 1;
                continue;
            }

            // 秒 second
            else if ((c == 's' || c == 'S') && this.analysis.indexOf(str, "second", i, 2, 1) == i) {
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }
                int index = datas.size() - 1;
                if (index < 0) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(4, str, next), next);
                }

                datas.set(index, new DateUnitParameter(datas.get(index), Calendar.SECOND));
                i = i + "second".length() - 1;
                continue;
            }

            // 毫秒 millisecond
            else if ((c == 'm' || c == 'M') && this.analysis.indexOf(str, "millis", i, 2, 1) == i) {
                if (!isData) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(28, str, next), next);
                }
                int index = datas.size() - 1;
                if (index < 0) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(4, str, next), next);
                }

                datas.set(index, new DateUnitParameter(datas.get(index), Calendar.MILLISECOND));
                i = i + "millis".length() - 1;
                continue;
            }

            // 扩展接口
            else {
                int oldDataSize = datas.size(), oldOperationSize = operations.size();
                int index = this.parse(str, i, isData, datas, operations);
                int c1 = datas.size() - oldDataSize;
                int c2 = operations.size() - oldOperationSize;
                if (c1 > c2) {
                    isData = true;
                } else if (c1 < c2) {
                    isData = false;
                }
                i = index;
                continue;
            }
        }

        // 生成运算公式
        return this.calc(datas, operations);
    }

    /**
     * 将数据代入公式执行运算并返回结果数值
     *
     * @param datas      参数集合
     * @param operations 操作集合
     * @return 计算公式
     */
    protected Formula calc(ArrayList<Parameter> datas, ArrayList<Operator> operations) {
        return new Formula(datas, operations);
    }

}
