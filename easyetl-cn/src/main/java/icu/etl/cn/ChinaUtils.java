package icu.etl.cn;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import icu.etl.jdk.JavaDialectFactory;
import icu.etl.util.ArrayUtils;
import icu.etl.util.CharsetName;
import icu.etl.util.Dates;
import icu.etl.util.Property;
import icu.etl.util.StringUtils;
import icu.etl.util.XMLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 中国相关帮助信息
 *
 * @author jeremy8551@qq.com
 */
public class ChinaUtils {

    /** 资源文件 */
    public static String CONFIG_XML = "/cn/china.xml";

    /** 字符集编码器 */
    public final static CharsetEncoder GBK_ENCODER = Charset.forName(CharsetName.GBK).newEncoder();

    /**
     * 统一社会信用代码加权因子
     */
    private final static int[] UNIFORM_SOCIAL_CREDITCODE_ARRAY = new int[]{1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28};

    /**
     * 统一社会信用代码权重
     */
    private final static String UNIFORM_SOCIAL_CREDITCODE = "0123456789ABCDEFGHJKLMNPQRTUWXY";

    /**
     * 身份证校验用到的常数
     */
    public final static int[] IDNO_CONSTANTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1};

    /**
     * 身份证校验用到的常数
     */
    public final static char[] IDNO_PARITYBIT = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    public ChinaUtils() {
    }

    /**
     * 读取配置信息集合
     *
     * @param nameOrType 参数名或参数类型值, 相见 china.xml 中的 {@literal <item name="xxx" type="xxx"> }
     * @return 属性集合
     */
    public static List<Property> getProperties(String nameOrType) {
        if (StringUtils.isBlank(nameOrType)) {
            throw new IllegalArgumentException(nameOrType);
        }

        List<Property> list = new ArrayList<Property>();
        Element root = XMLUtils.getRoot(CONFIG_XML);
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if ("item".equalsIgnoreCase(node.getNodeName())) {
                String name = StringUtils.removeBlank(XMLUtils.getAttribute(node, "name"));
                String type = StringUtils.removeBlank(XMLUtils.getAttribute(node, "type"));

                if (name.equalsIgnoreCase(nameOrType) || type.equalsIgnoreCase(nameOrType)) {
                    NodeList nodeList = node.getChildNodes();
                    for (int j = 0; j < nodeList.getLength(); j++) {
                        Node item = nodeList.item(j);
                        if ("property".equalsIgnoreCase(item.getNodeName())) {
                            String code = StringUtils.trimBlank(XMLUtils.getAttribute(item, "code"));
                            String notes = StringUtils.trimBlank(XMLUtils.getAttribute(item, "notes"));
                            String order = StringUtils.trimBlank(XMLUtils.getAttribute(item, "orders", String.valueOf(Integer.MAX_VALUE)));

                            Property p = new Property();
                            p.setKey(code);
                            p.setValue(notes);
                            p.setOrder(StringUtils.parseInt(order, Integer.MAX_VALUE));
                            list.add(p);
                        }
                    }

                    Collections.sort(list, new Comparator<Property>() {
                        public int compare(Property o1, Property o2) {
                            return o1.getOrder() - o2.getOrder();
                        }
                    });
                    return list;
                }
            }
        }
        return list;
    }

    /**
     * 校验15位和18位身份证是否合法
     *
     * @param idCard 15位或18位身份证号
     * @return true表示身份证合法
     */
    public static boolean isIdCard(String idCard) {
        if (idCard == null) {
            return false;
        }

        int length = idCard.length();
        if (length == 18) { // 18位身份证
            return isIdCard18(idCard);
        } else if (length == 15) { // 15位身份证
            String id18no = idCard15to18(idCard);
            return ChinaUtils.isIdCard18(id18no);
        } else {
            return false;
        }
    }

    /**
     * 校验18位身份证号
     *
     * @param idCard 18位身份证号
     * @return 返回true表示合法身份证
     */
    public static boolean isIdCard18(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return false;
        }

        String date = idCard.substring(6, 14); // 出生日期
        if (Dates.testParse(date) == null) {
            return false;
        }

        int j = 0;
        for (int i = 0; i < idCard.length() - 1; i++) {
            try {
                j = j + Integer.parseInt(String.valueOf(idCard.charAt(i))) * IDNO_CONSTANTS[i];
            } catch (Throwable e) {
                return false;
            }
        }
        j %= 11;
        return idCard.charAt(idCard.length() - 1) == IDNO_PARITYBIT[j];
    }

    /**
     * 把15位身份证转为18位身份证
     *
     * @param idCard 15位身份证号
     * @return 18位身份证号; 返回null表示输入参数idCard非法
     */
    public static String idCard15to18(String idCard) {
        if (idCard == null || idCard.length() != 15) {
            return null;
        }

        try {
            String id17no = idCard.substring(0, 6) + "19" + StringUtils.right(idCard, 9);
            int j = 0;
            for (int i = 0; i < id17no.length(); i++) {
                j = j + Integer.parseInt(id17no.substring(i, i + 1)) * IDNO_CONSTANTS[i];
            }
            j %= 11;
            return id17no + IDNO_PARITYBIT[j];
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 校验统一社会信用代码编号 <br>
     * 营业执照号、组织机构号、税务号三证合一为18位的统一社会信用代码编号 <br>
     * 统一社会信用代码编号从右边倒数第二个字符开始向左数9个字符表示组织机构号 <br>
     * 统一社会信用代码编号中英文字母全部大写 <br>
     * 统一社会信用代码编号前17位字母校验位第18个字母 <br>
     *
     * @param str 统一社会信用代码编号
     * @return true表示统一社会信用代码合法 false表示统一社会信用代码不合法
     */
    public static boolean isUniformSocialCreditCode(String str) {
        if (str == null || str.length() != 18) {
            return false;
        }

        int ret = 0;
        for (int i = 0; i < str.length() - 1; i++) {
            char c = str.charAt(i);
            int pos = UNIFORM_SOCIAL_CREDITCODE.indexOf(c);
            ret += (pos * UNIFORM_SOCIAL_CREDITCODE_ARRAY[i]); // 权重与加权因子相乘之和
        }

        int checkCode = 31 - ret % 31;
        if (checkCode == 31) {
            checkCode = 0;
        }
        checkCode = UNIFORM_SOCIAL_CREDITCODE.charAt(checkCode);
        return str.charAt(17) == checkCode;
    }

    /**
     * 判断字符是否是中文汉字字符
     *
     * @param c 字符
     * @return 返回true表示参数是中文字符 false表示不是中文字符
     */
    public static boolean isChineseLetter(char c) {
        return isChineseLetter(c, GBK_ENCODER);
    }

    /**
     * 判断字符是否是中文汉字字符
     *
     * @param c       字符
     * @param encoder 字符编码器
     * @return 返回true表示是中文字符 false表示非中文字符
     */
    public static boolean isChineseLetter(char c, CharsetEncoder encoder) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        boolean block = (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS //
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A //
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B //
                || JavaDialectFactory.getDialect().isChineseLetter(ub) //
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS //
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT //
                || (c >= 0xe815 && c <= 0xe864) //
        );

        if (block) {
            if (encoder == null) {
                encoder = GBK_ENCODER;
            }
            return encoder.canEncode(c);
        } else {
            return false;
        }
    }

    /**
     * 将中文数字替换为阿拉伯数字 <br>
     * 中文字符范围包括： 零壹贰叁肆伍陆柒捌玖 零一二三四五六七八九 <Br>
     *
     * @param c 字符
     * @return
     */
    public static char replaceChineseNumber(char c) {
        for (int i = 0; i <= 9; i++) {
            if (c == "零壹贰叁肆伍陆柒捌玖".charAt(i) || c == "零一二三四五六七八九".charAt(i)) {
                return "0123456789".charAt(i);
            }
        }
        return c;
    }

    /**
     * 将字符串参数 str 中的中文字符替换为阿拉伯字母 <br>
     * 中文字符范围包括： 零壹贰叁肆伍陆柒捌玖 零一二三四五六七八九 <Br>
     *
     * @param str 字符串
     * @return 字符串
     */
    public static String replaceChineseNumber(CharSequence str) {
        if (str == null) {
            return null;
        }

        int length = str.length();
        char[] array = new char[length];
        for (int i = 0; i < length; i++) {
            array[i] = replaceChineseNumber(str.charAt(i));
        }
        return new String(array);
    }

    /**
     * 将 BigDecimal 转为汉字大写金额(如：壹万贰仟叁佰肆拾伍元) <br>
     * 将汉字大写金额转为 BigDecimal 详见方法： {@link #parseChineseNumber(CharSequence)}
     *
     * @param value 数值
     * @return 中文大写金额
     */
    public static String toChineseNumber(BigDecimal value) {
        if (value == null) {
            return null;
        }

        StringBuilder str = new StringBuilder();
        // -1, 0, or 1 as the value of this BigDecimal is negative, zero, or
        // positive.
        int signum = value.signum();
        // 零元整的情况
        if (signum == 0) {
            return "零元";
        }

        // 这里会进行金额的四舍五入
        long number = value.movePointRight(2).setScale(0, 4).abs().longValue();
        // 得到小数点后两位值
        long scale = number % 100;
        int numUnit = 0;
        int numIndex = 0;
        boolean getZero = false;
        // 判断最后两位数，一共有四中情况：00 = 0, 01 = 1, 10, 11
        if (!(scale > 0)) {
            numIndex = 2;
            number = number / 100;
            getZero = true;
        }
        if ((scale > 0) && (!(scale % 10 > 0))) {
            numIndex = 1;
            number = number / 10;
            getZero = true;
        }
        int zeroSize = 0;
        while (true) {
            if (number <= 0) {
                break;
            }
            // 每次获取到最后一个数
            numUnit = (int) (number % 10);
            if (numUnit > 0) {
                if ((numIndex == 9) && (zeroSize >= 3)) {
                    str.insert(0, '万');
                }
                if ((numIndex == 13) && (zeroSize >= 3)) {
                    str.insert(0, '亿');
                }
                str.insert(0, "分角元拾佰仟万拾佰仟亿拾佰仟兆拾佰仟".charAt(numIndex));
                str.insert(0, "零壹贰叁肆伍陆柒捌玖".charAt(numUnit));
                getZero = false;
                zeroSize = 0;
            } else {
                ++zeroSize;
                if (!(getZero)) {
                    str.insert(0, "零壹贰叁肆伍陆柒捌玖".charAt(numUnit));
                }
                if (numIndex == 2) {
                    if (number > 0) {
                        str.insert(0, "分角元拾佰仟万拾佰仟亿拾佰仟兆拾佰仟".charAt(numIndex));
                    }
                } else if (((numIndex - 2) % 4 == 0) && (number % 1000 > 0)) {
                    str.insert(0, "分角元拾佰仟万拾佰仟亿拾佰仟兆拾佰仟".charAt(numIndex));
                }
                getZero = true;
            }
            // 让number每次都去掉最后一个数
            number = number / 10;
            ++numIndex;
        }
        // 如果signum == -1，则说明输入的数字为负数，就在最前面追加特殊字符：负
        if (signum == -1) {
            str.insert(0, "负");
        }
        // 输入的数字小数点后两位为"00"的情况，则要在最后追加特殊字符：整
//        if (!(scale > 0)) {
        // str.append(CN_FULL);
//        }
        return str.toString();
    }

    /**
     * 将中文大写金额转为 BigDecimal 类型 <br>
     * 将 BigDecimal 转为中文大写字符串详见： {@linkplain  #toChineseNumber(BigDecimal)}
     *
     * @param str 字符串, 如: 二千三百四十五万
     * @return 金额
     */
    public static BigDecimal parseChineseNumber(CharSequence str) {
        if (StringUtils.isBlank(str)) {
            throw new IllegalArgumentException(String.valueOf(str));
        }

        str = StringUtils.removeBlank(str);
        boolean negative = str.charAt(0) == '-' || str.charAt(0) == '负';
        if (negative) {
            str = str.subSequence(1, str.length());
        }

        String[] array = StringUtils.split(str, ArrayUtils.asList("元", "块", "园", ".", "点"), true);
        if (array.length == 1) {
            String[] newarray = StringUtils.split(str, ArrayUtils.asList("角", "毛", "分", "厘", "豪", "丝"), true);
            if (newarray.length > 1) {
                long v = parseLong(str);
                BigDecimal value = new BigDecimal("0." + v);
                return negative ? value.negate() : value;
            } else {
                long v = parseLong(str);
                return negative ? BigDecimal.valueOf(v).negate() : BigDecimal.valueOf(v);
            }
        } else if (array.length == 2) {
            if (StringUtils.isBlank(array[1])) {
                long v = parseLong(array[0]);
                return negative ? BigDecimal.valueOf(v).negate() : BigDecimal.valueOf(v);
            } else {
                BigDecimal zsw = BigDecimal.valueOf(parseLong(array[0]));
                if (StringUtils.isBlank(array[1])) {
                    return negative ? zsw.negate() : zsw;
                } else {
                    long xs = parseLong(array[1]);
                    BigDecimal v = new BigDecimal(zsw + "." + xs);
                    return negative ? v.negate() : v;
                }
            }
        } else {
            throw new IllegalArgumentException(String.valueOf(str));
        }
    }

    /**
     * 解析整数部分数字
     *
     * @param str 字符串
     * @return 将字符串转为数字
     */
    protected static long parseLong(CharSequence str) {
        if (StringUtils.isBlank(str)) {
            throw new IllegalArgumentException(StringUtils.toString(str));
        }

        str = StringUtils.removeBlank(str);
        boolean negative = str.charAt(0) == '-' || str.charAt(0) == '负';
        long v = 0;

        int level = 0;
        for (int index = negative ? 1 : 0; index < str.length(); index++) {
            char c = str.charAt(index);
            int next = index + 1;

            if (StringUtils.inArray(c, '元', '园')) {
                continue;
            } else if (StringUtils.inArray(c, '.', '点')) {
                throw new IllegalArgumentException("parseLong(\"" + str + "\") exists Illegal character \"" + c + "\" v = " + v);
            }

            boolean isNumber = false;
            int n = "0123456789".indexOf(c);
            if (n == -1) {
                n = "零一二三四五六七八九".indexOf(c);
                if (n == -1) {
                    n = "零壹贰叁肆伍陆柒捌玖".indexOf(c);
                    if (n == -1) {
                        throw new IllegalArgumentException("parseLong(\"" + str + "\") exists Illegal character \"" + str.subSequence(0, index + 1) + "\" v = " + v);
                    }
                }
            } else {
                isNumber = true;
            }

            if (next < str.length()) {
                char nc = str.charAt(next);
                String substr = str.subSequence(next + 1, str.length()).toString();

                if (StringUtils.inArray(nc, '元', '园')) {
                    continue;
                } else if (StringUtils.inArray(nc, '.', '点')) {
                    throw new IllegalArgumentException("Numeric expression \"" + str + "\" exists Illegal character \"" + nc + "\" v = " + v);
                } else if (nc == '角' || nc == '毛') {
                    if (level > 0) {
                        throw new IllegalArgumentException("Numeric expression \"" + str + "\" exists Illegal character \"" + nc + "\" v = " + v);
                    }
                    if (level < -1) {
                        throw new IllegalArgumentException("Numeric expression \"" + str + "\" exists syntax error \"" + nc + "\" v = " + v);
                    }
                    v = v * 10 + n;
                    level = -1;
                } else if (nc == '分') {
                    if (level > 0) {
                        throw new IllegalArgumentException("Numeric expression \"" + str + "\" exists Illegal character \"" + nc + "\" v = " + v);
                    }
                    if (level < -2) {
                        throw new IllegalArgumentException("Numeric expression \"" + str + "\" exists syntax error \"" + nc + "\" v = " + v);
                    }
                    v = v * 10 + n;
                    level = -2;
                } else if (nc == '厘') {
                    if (level > 0) {
                        throw new IllegalArgumentException("Numeric expression \"" + str + "\" exists Illegal character \"" + nc + "\" v = " + v);
                    }
                    if (level < -3) {
                        throw new IllegalArgumentException("Numeric expression \"" + str + "\" exists syntax error \"" + nc + "\" v = " + v);
                    }
                    v = v * 10 + n;
                    level = -3;
                } else if (nc == '豪') {
                    if (level > 0) {
                        throw new IllegalArgumentException("Numeric expression \"" + str + "\" exists Illegal character \"" + nc + "\" v = " + v);
                    }
                    if (level < -4) {
                        throw new IllegalArgumentException("Numeric expression \"" + str + "\" exists syntax error \"" + nc + "\" v = " + v);
                    }
                    v = v * 10 + n;
                    level = -4;
                } else if (nc == '丝') {
                    if (level > 0) {
                        throw new IllegalArgumentException("Numeric expression \"" + str + "\" exists Illegal character \"" + nc + "\" v = " + v);
                    }
                    if (level < -5) {
                        throw new IllegalArgumentException("Numeric expression \"" + str + "\" exists syntax error \"" + nc + "\" v = " + v);
                    }
                    v = v * 10 + n;
                    level = -5;
                } else if (StringUtils.inArray(nc, '十', '拾')) {
                    if (level == 0) {
                        v = (v + n) * 10;
                        level = 1;
                    } else if (level < 1) {
                        v = (v + n) * 10;
                        level = 1;
                        if (StringUtils.isNotBlank(substr)) {
                            v += parseLong(substr);
                            break;
                        }
                    } else {
                        v += n * 10;
                    }
                } else if (StringUtils.inArray(nc, '百', '佰')) {
                    if (level == 0) {
                        v = (v + n) * 100;
                        level = 2;
                    } else if (level < 2) {
                        v = (v + n) * 100;
                        level = 2;
                        if (StringUtils.isNotBlank(substr)) {
                            v += parseLong(substr);
                            break;
                        }
                    } else {
                        v += n * 100;
                    }
                } else if (StringUtils.inArray(nc, '千', '仟')) {
                    if (level == 0) {
                        v = (v + n) * 1000;
                        level = 3;
                    } else if (level < 3) {
                        v = (v + n) * 1000;
                        level = 3;
                        if (StringUtils.isNotBlank(substr)) {
                            v += parseLong(substr);
                            break;
                        }
                    } else {
                        v += n * 1000;
                    }
                } else if (nc == '万') {
                    if (level == 0) {
                        v = (v + n) * 10000;
                        level = 4;
                    } else if (level < 4) {
                        v = (v + n) * 10000;
                        level = 4;
                        if (StringUtils.isNotBlank(substr)) {
                            v += parseLong(substr);
                            break;
                        }
                    } else {
                        v += n * 10000;
                    }
                } else if (nc == '亿') {
                    if (level == 0) {
                        v = (v + n) * (long) Math.pow(10, 8);
                        level = 5;
                    } else if (level < 5) {
                        v = (v + n) * (long) Math.pow(10, 8);
                        level = 5;
                        if (StringUtils.isNotBlank(substr)) {
                            v += parseLong(substr);
                            break;
                        }
                    } else {
                        v += n * Math.pow(10, 8);
                    }
                } else if (nc == '兆') {
                    if (level == 0) {
                        v = (v + n) * (long) Math.pow(10, 12);
                        level = 6;
                    } else if (level < 6) {
                        v = (v + n) * (long) Math.pow(10, 12);
                        level = 6;
                        if (StringUtils.isNotBlank(substr)) {
                            v += parseLong(substr);
                            break;
                        }
                    } else {
                        v += n * Math.pow(10, 12);
                    }
                } else if ("0123456789零一二三四五六七八九零壹贰叁肆伍陆柒捌玖".indexOf(nc) != -1) {
                    if (!isNumber && n == 0) { // c为零
                    } else {
                        v = (v * 10) + n;
                    }

                    if (level < 0) {
                        level--;
                    } else {
                        level++;
                    }
                    continue;
                } else {
                    throw new IllegalArgumentException("string " + str + " Illegal character in " + nc + " !");
                }

                index++;
            } else {
                if (level < 0) {
                    level--;
                    v = v * 10 + n;
                } else {
                    if (isNumber) {
                        v = v * 10 + n;
                    } else {
                        v += n;
                    }
                }
            }
        }
        return negative ? -v : v;
    }

}
