package icu.etl.database.db2.format;

/**
 * 把 Double 转成字符串，格式：+0.00000000000000E+000 <br>
 * 0.0 +0.00000000000000E+000 <br>
 * 1.234567E-11 +1.23456700000000E-011 <br>
 * 1.234567E-10 +1.23456700000000E-010 <br>
 * 1.234567E-9 +1.23456700000000E-009 <br>
 * 1.234567E-8 +1.23456700000000E-008 <br>
 * 1.234567E-7 +1.23456700000000E-007 <br>
 * 1.234567E-6 +1.23456700000000E-006 <br>
 * 1.234567E-5 +1.23456700000000E-005 <br>
 * 1.234567E-4 +1.23456700000000E-004 <br>
 * 0.001234567 +1.23456700000000E-003 <br>
 * 0.01234567 +1.23456700000000E-002 <br>
 * 0.1234567 +1.23456700000000E-001 <br>
 * 1.1234567 +1.12345670000000E+000 <br>
 * 12.345678 +1.23456780000000E+001 <br>
 * 123.45678 +1.23456780000000E+002 <br>
 * 1234.5678 +1.23456780000000E+003 <br>
 * 12345.678 +1.23456780000000E+004 <br>
 * 123456.78 +1.23456780000000E+005 <br>
 * 1234567.8 +1.23456780000000E+006 <br>
 * 1.23456789E7 +1.23456789000000E+007 <br>
 * 1.23456789E8 +1.23456789000000E+008 <br>
 * 0.012345679 +1.23456790000000E-002 <br>
 * 1.23456789012345E14 +1.23456789012345E+014 <br>
 * 1.2345678901234568E37 +1.23456789012346E+037 <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-04-03
 */
public class DB2DoubleFormat {
    public final static double[] sizeTable = new double[64];
    public final static double[] pointSize = new double[30];

    static {
        for (int i = 0; i < pointSize.length; i++) {
            String s = "0.";
            for (int j = 0; j < i; j++) {
                s += "0";
            }
            s += "9";
            pointSize[i] = new Double(s).doubleValue();
        }

        for (int i = 0; i < sizeTable.length; i++) {
            String s = "9";
            for (int j = 0; j < i; j++) {
                s += "9";
            }
            sizeTable[i] = new Double(s).doubleValue();
        }
    }

    /**
     * 求整数的位置
     *
     * @param x 整数
     * @return
     */
    static int stringSize(double x) {
        for (int i = 0; ; i++)
            if (x <= sizeTable[i])
                return i + 1;
    }

    /**
     * 返回点号的位置 <br>
     * 0.101f == 1 <br>
     * 0.01 == 2 <br>
     *
     * @param f
     * @return
     */
    static int pointPosition(double f) {
        for (int i = pointSize.length - 1; i >= 0; i--) {
            if (f <= pointSize[i]) {
                return i + 1;
            }
        }
        throw new IllegalArgumentException(Double.toString(f));
    }

    public StringBuilder format(Double f) {
        StringBuilder buf = new StringBuilder(13);
        double val = f.doubleValue();
        if (val == 0.0F) {
            buf.append("+0.00000000000000E+000");
            return buf;
        }

        if (val < 0.0F) {
            val = 0.0F - val;
            buf.append('-');
        } else {
            buf.append('+');
        }
        boolean zf = false;
        int size = stringSize(val) - 1; // 乘以0的个数
        if (val < 1) {
            int pLen = pointPosition(val); // 小数点后0的位数
            size += pLen;
            for (int j = 1; j <= 15 + pLen - 1; j++) {
                val = val * 10;
            }
            zf = true;
        } else {
            if (size < 15) {
                for (int j = 1; j <= 15 - size - 1; j++) {
                    val = val * 10;
                }
            } else {
                for (int j = 1; j <= size - 15 + 1; j++) {
                    val = val / 10;
                }
            }
        }

        buf.append(Math.round(val));
        buf.insert(2, '.');
        buf.append('E');
        if (zf) {
            buf.append('-');
        } else {
            buf.append('+');
        }

        if (size <= 9) {
            buf.append("00");
        } else if (size <= 99) {
            buf.append('0');
        }
        buf.append(size);
        return buf;
    }

}
