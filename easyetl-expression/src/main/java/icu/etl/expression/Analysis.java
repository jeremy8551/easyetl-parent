package icu.etl.expression;

import java.util.List;

/**
 * 脚本语句分析器
 *
 * @author jeremy8551@qq.com
 */
public interface Analysis {

    /**
     * 返回语句中段落的分隔符, 默认是半角逗号
     *
     * @return 字符
     */
    char getSegment();

    /**
     * 返回映射关系的分隔符, 默认是半角冒号 <br>
     * 1:name,2:age
     *
     * @return 字符
     */
    char getMapdel();

    /**
     * 返回注释的起始标志符
     *
     * @return 字符
     */
    char getComment();

    /**
     * 判断是否忽略大小写比较字符串
     *
     * @return 返回 true 表示忽略大小写
     */
    boolean ignoreCase();

    /**
     * 判断两个字符串是否相等
     *
     * @param str1 字符串
     * @param str2 字符串
     * @return 返回true表示字符串参数相等 false表示字符串不等
     */
    boolean equals(String str1, String str2);

    /**
     * 判断字符串参数 key 是否在数组参数中
     *
     * @param key   字符串
     * @param array 字符串数组
     * @return
     */
    boolean exists(String key, String... array);

    /**
     * 判断脚本语句参数 str 的指定位置上的字符是否满足条件
     *
     * @param str       脚本语句
     * @param index     位置信息
     * @param condition 0-表示指定位置上的字符必须是空白字符（含-1和最右侧） 1-表示指定位置上的字符必须是空白字符与控制字符 2-表示指定位置上的字符可以是任意字符
     * @return
     */
    boolean charAt(CharSequence str, int index, int condition);

    /**
     * 在字符串参数 {@code str} 中搜索字符串数组 {@code dest} 的起始位置
     *
     * @param str  字符串
     * @param dest 搜索的单词序列
     * @param from 搜索起始位置, 从0开始
     * @return 单词序列在字符串中的位置数组（第一个单词的起始位置对应数组的第一个元素，以此类推）
     */
    int[] indexOf(CharSequence str, String[] dest, int from);

    /**
     * 在字符串参数 {@code str} 中搜索字符串 {@code dest} 的起始位置
     *
     * @param str   字符串
     * @param dest  被搜索的字符串
     * @param from  搜索起始位置, 从0开始
     * @param left  0-表示左侧字符必须是空白字符或字符串的起始位置 1-表示左侧字符必须是空白字符与控制字符 2-表示任意字符
     * @param right 0-表示右侧字符必须是空白字符或字符串的结束位置 1-表示右侧字符必须是空白字符与控制字符 2-表示任意字符
     * @return 字符串的起始位置
     */
    int indexOf(CharSequence str, String dest, int from, int left, int right);

    /**
     * 搜索符号 : 位置
     *
     * @param str  字符数组
     * @param from 起始位置（不包含）
     * @return 位置 -1表示不存在
     */
    int indexOfSemicolon(CharSequence str, int from);

    /**
     * 查询数字结束位置的下一个位置
     *
     * @param str  字符串
     * @param from 起始位置
     * @return 数字结束位置
     */
    int indexOfFloat(CharSequence str, int from);

    /**
     * 在字符串参数 str 中搜索反引号的结束位置（忽略转义字符右侧的字符）
     *
     * @param str  字符串
     * @param from 反引号的起始位置
     * @return 返回反引号的结束位置, -1表示没有找到反引号
     */
    int indexOfAccent(CharSequence str, int from);

    /**
     * 对字符串参数 str 进行转义
     *
     * @param str
     * @return
     */
    String unescapeString(CharSequence str);

    /**
     * 将脚本语句使用空白字符与字符数组参数 delimiter 分割成多个字符串
     *
     * @param str       脚本语句
     * @param delimiter 字段分隔符数组
     * @return
     */
    List<String> split(CharSequence str, char... delimiter);

    /**
     * 判断字符串参数 str 是否以 prefix 开头
     *
     * @param str
     * @param prefix
     * @param from
     * @param ignoreBlank
     * @return
     */
    boolean startsWith(CharSequence str, CharSequence prefix, int from, boolean ignoreBlank);

    /**
     * 从字符串参数 str 中指定位置 from 开始到 end 位置为止开始搜索字符串参数 dest，返回字符串参数 dest 在字符串参数 str 中最后一次出现所在的位置
     *
     * @param str   字符串
     * @param dest  搜索字符串
     * @param from  搜索起始位置(包含该点)
     * @param left  0-表示左侧字符必须是空白字符（含-1和最右侧） 1-表示左侧字符必须是空白字符与控制字符 2-表示任意字符
     * @param right 0-表示右侧字符必须是空白字符（含-1和最右侧） 1-表示右侧字符必须是空白字符与控制字符 2-表示任意字符
     * @return -1表示字符串 dest 没有出现
     */
    int lastIndexOf(CharSequence str, String dest, int from, int left, int right);

    /**
     * 在字符串参数 str 中搜索小括号的结束位置
     *
     * @param str  字符串
     * @param from 小括号的起始位置
     * @return -1表示小括号没有出现
     */
    int indexOfParenthes(CharSequence str, int from);

    /**
     * 在字符串参数 str 中搜索中括号的结束位置
     *
     * @param str  字符串
     * @param from 中括号的起始位置
     * @return -1表示中括号没有出现
     */
    int indexOfBracket(CharSequence str, int from);

    /**
     * 在字符串参数 str 中搜索大括号的结束位置
     *
     * @param str  字符串
     * @param from 大括号的起始位置
     * @return -1表示大括号没有出现
     */
    int indexOfBrace(CharSequence str, int from);

    /**
     * 在字符串参数 str 中搜索单引号的结束位置（忽略转义字符右侧的字符）
     *
     * @param str  字符串
     * @param from 单引号的起始位置
     * @return -1表示单引号没有出现
     */
    int indexOfQuotation(CharSequence str, int from);

    /**
     * 在字符串参数 str 中搜索双引号结束位置（忽略转义字符右侧的字符）
     *
     * @param str  字符串
     * @param from 双引号的起始位置
     * @return -1表示双引号没有出现
     */
    int indexOfDoubleQuotation(CharSequence str, int from);

    /**
     * 搜索十六进制数值结尾位置
     *
     * @param str  字符串
     * @param from 搜索起始位置
     * @return
     */
    int indexOfHex(CharSequence str, int from);

    /**
     * 搜索八进制数值的结束位置
     *
     * @param str  字符串
     * @param from 搜索起始位置
     * @return
     */
    int indexOfOctal(CharSequence str, int from);

    /**
     * 搜索下一个空白字符的位置信息
     *
     * @param str  字符串
     * @param from 搜索起始位置
     * @return
     */
    int indexOfWhitespace(CharSequence str, int from);

    /**
     * 判断字符串二端是否有对称的单引号或双引号，自动忽略字符串二端的空白字符
     *
     * @param str 字符串
     * @return
     */
    boolean containsQuotation(CharSequence str);

    /**
     * 删除字符串参数 str 二端的单引号或双引号 <br>
     * 且自动删除字符串二端的空白字符
     *
     * @param str 字符串
     * @return
     */
    String unQuotation(CharSequence str);

}
