package icu.etl.expression;

import java.math.BigDecimal;

import icu.etl.util.StringUtils;

/**
 * 计算机中数据单位表达式
 *
 * @author jeremy8551@qq.com
 * @createtime 2023-09-18
 */
public class DataUnitExpression {

    private BigDecimal value;

    /**
     * 将 1kb 或 1mb, 1gb, 1tb 转为字节数 <br>
     * <p>
     * 将容量字节转为可读性字符串详见 {@linkplain #toString(BigDecimal)} 方法
     *
     * @param expression 表达式
     */
    public DataUnitExpression(CharSequence expression) {
        this.value = parse(expression);
    }

    public BigDecimal getValue() {
        return value;
    }

    /**
     * 将 1kb 或 1mb, 1gb, 1tb 转为字节数 <br>
     * <p>
     * 将容量字节转为可读性字符串详见 {@linkplain #toString(BigDecimal)} 方法
     *
     * @param str
     * @return 总字节数
     */
    public static BigDecimal parse(CharSequence str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        String value = str.toString().toUpperCase().replace('B', ' ');
        for (int i = 0; i < "KMGTPEZYND".length(); i++) {
            char c = "KMGTPEZYND".charAt(i);
            StringBuilder req = new StringBuilder();
            for (int j = 0; j <= i; j++) {
                req.append(" * 1024");
            }
            value = StringUtils.replaceAll(value, String.valueOf(c), req.toString());
        }

        return new Expression(value).decimalValue();
    }

    /**
     * 将字节容量转为可读性字符串<br>
     * 1056 == 1.03 KB<br>
     * 663040000 == 632.32 MB<br>
     * 678952960000 == 632.32 GB<br>
     * 695247831040000 == 632.32 TB<br>
     * <p>
     * 将可读性字符串转为容量详见 {@linkplain #parse(CharSequence)} 方法
     *
     * @param value
     * @return
     */
    public static String toString(BigDecimal value) {
        if (value == null) {
            return null;
        }

        int count = -1;
        BigDecimal x = value;
        BigDecimal m = new BigDecimal("1024");
        while (x.compareTo(m) >= 0) {
            x = x.divide(m, 4, BigDecimal.ROUND_HALF_UP);
            count++;
        }

        return x.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + (count == -1 ? "" : " " + "KMGTPEZYND".charAt(count) + "B");
    }

}
