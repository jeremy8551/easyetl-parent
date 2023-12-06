package icu.etl.log.apd;

import icu.etl.util.StringUtils;

/**
 * 日志信息中的字段对齐方式
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/23
 */
public class LogFieldAlign {

    /** 最小长度 */
    private int min;

    /** 最大长度 */
    private int max;

    /** true表示右对齐 false表示左对齐 */
    private boolean direction;

    public LogFieldAlign(int min, int max, boolean direction) {
        if (max >= 0 && min > max) {
            throw new IllegalArgumentException(String.valueOf(min));
        }

        this.min = min;
        this.max = max;
        this.direction = direction;
    }

    /**
     * 初始化
     *
     * @param expression 修饰符表达式
     *                   %5c
     *                   %-5c
     *                   %.5c
     *                   %20.30c
     */
    public static LogFieldAlign parse(String expression) {
        if (expression == null || expression.length() == 0) {
            throw new IllegalArgumentException(expression);
        }

        LogFieldAlign align = new LogFieldAlign(-1, -1, true);
        if (expression.charAt(0) == '-') { // 左对齐
            align.direction = false;
            int end = indexOfEnd(expression, 1);
            String str = expression.substring(1, end);
            align.min = Integer.parseInt(str);
            expression = expression.substring(end);
        } else if (StringUtils.isNumber(expression.charAt(0))) {
            int end = indexOfEnd(expression, 0);
            String str = expression.substring(0, end);
            align.min = Integer.parseInt(str);
            expression = expression.substring(end);
        }

        if (expression.length() == 0) {
            return align;
        }

        if (expression.charAt(0) == '.') {
            int end = indexOfEnd(expression, 1);
            String str = expression.substring(1, end);
            align.max = Integer.parseInt(str);
            return align;
        } else {
            throw new IllegalArgumentException(expression);
        }
    }

    private static int indexOfEnd(String expression, int from) {
        for (int i = from; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '.' || StringUtils.isLetter(c)) {
                return i;
            } else if (StringUtils.isNumber(c)) {
                continue;
            } else {
                throw new IllegalArgumentException(expression + " [" + c + "]");
            }
        }

        return expression.length();
    }

    public String format(CharSequence str) {
        if (str == null) {
            str = "";
        }

        int length = str.length();

        // 如果设置了最小长度
        if (this.min >= 0) {
            if (length == this.min) {
//                return str.toString();
            } else if (length < this.min) {
                int size = this.min - length;
                StringBuilder buf = new StringBuilder(this.min);
                if (this.direction) {
                    for (int i = 0; i < size; i++) {
                        buf.append(' ');
                    }
                    buf.append(str);
//                    buf.append(StringUtils.right(str, this.min, ' '));
                } else {
                    buf.append(str);
                    for (int i = 0; i < size; i++) {
                        buf.append(' ');
                    }
//                    buf.append(StringUtils.left(str, this.min, ' '));
                }
                str = buf.toString();
                length = str.length();
            } else {
                //
            }
        }

        // length > min
        if (this.max >= 0) {
            if (length <= this.max) {
                return str.toString();
            } else {
                return str.subSequence(length - this.max, length).toString();
//                return StringUtils.right(str, this.max, ' ');
            }
        } else {
            return str.toString();
        }
    }

    /**
     * 返回最小长度
     *
     * @return -1表示没有限制
     */
    public int getMin() {
        return min;
    }

    /**
     * 返回最大长度
     *
     * @return -1表示没有限制
     */
    public int getMax() {
        return max;
    }

    /**
     * 返回对齐方式
     *
     * @return true表示右对齐 false表示左对齐
     */
    public boolean isRight() {
        return direction;
    }
}
