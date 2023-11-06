package icu.etl.database.db2.format;

import java.math.BigDecimal;

/**
 * 把BigDecimal格式为DB2的默认格式的字符串
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-04-02
 */
public class DB2DecimalFormat {

    /** decimal字段中数字的位数 */
    private int precision;

    /** decimal字段中小数点后的位数 */
    private int scale;

    /** 缓冲区 */
    private char[] buffer;

    /** 模板 */
    private char[] template;

    /** 数组长度 */
    private int length;

    public DB2DecimalFormat() {
    }

    public void applyPattern(int precision, int scale) {
        this.precision = precision;
        this.scale = scale;

        // 精度 + 符号位 + 小数点
        this.length = this.precision + 1 + (this.scale > 0 ? 1 : 0);
        this.buffer = new char[length];
        this.template = new char[length];
        for (int i = 0; i < length; i++) {
            this.template[i] = '0';
        }
        this.template[0] = '+';
        if (this.scale > 0) {
            this.template[this.precision - this.scale + 1] = '.';
        }
    }

    public char[] getChars() {
        return this.buffer;
    }

    public int length() {
        return this.length;
    }

    public String formatToString(BigDecimal number) {
        format(number);
        return new String(this.buffer);
    }

    public void format(BigDecimal number) {
        if (number == null) {
            return;
        } else {
            System.arraycopy(this.template, 0, this.buffer, 0, this.length);
            int signum = number.signum(); // 符号位 0-表示0 1-表示正 -1表示负
            if (signum == 0) {
                return;
            }
            String numString = number.unscaledValue().toString(10);
            if (signum == 1) {

            }
            int signLen = 0;
            if (signum == -1) {
                this.buffer[0] = '-';
                signLen = 1;
            }
            int scale = number.scale();
            int precision = number.precision();
            int c = precision - scale;
            if (c > 0) {
                int intC = (precision - scale) - (this.precision - this.scale); // 整数位差距
                int intBegin = signLen + (intC > 0 ? intC : 0); // 整数起始位置

                int potC = this.scale - scale; // 小数位差距
                int potBegin = signLen + precision - scale; // 小数位起始位置
                int potEnd = numString.length() + (potC > 0 ? 0 : potC);// 小数位结束位置
                int bufIntBegin = this.length - this.scale - (precision - scale - (intC > 0 ? intC : 0)) - 1; // 整数位起始点（总长-小数位-整数位-小数点）
                int bufPotBegin = this.length - this.scale; // 小数位起始点 （总长-小数位）

                numString.getChars(intBegin, potBegin, this.buffer, bufIntBegin); // 复制整数位
                numString.getChars(potBegin, potEnd, this.buffer, bufPotBegin); // 复制小数位
            } else if (c == 0) {
                int potC = this.scale - scale; // 小数位差距
                int potEnd = numString.length() + (potC > 0 ? 0 : potC);// 小数位结束位置
                int bufPotBegin = this.length - this.scale; // 小数位起始点 （总长-小数位-小数位长度）
                numString.getChars(signLen, potEnd, this.buffer, bufPotBegin); // 复制小数位
            } else {
                int potEnd = numString.length() - (scale >= this.scale ? (scale - this.scale) : 0);// 小数位结束位置
                int bufPotBegin = this.length - this.scale + (this.scale - (potEnd - signLen)); // 小数位起始点 （总长-小数位-小数位长度）
                numString.getChars(signLen, potEnd, this.buffer, bufPotBegin); // 复制小数位
            }
        }
    }
}
