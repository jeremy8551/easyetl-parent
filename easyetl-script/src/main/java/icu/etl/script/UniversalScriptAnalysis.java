package icu.etl.script;

import icu.etl.expression.Analysis;
import icu.etl.expression.WordIterator;
import icu.etl.io.Escape;

/**
 * 脚本语句分析器
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptAnalysis extends Analysis, Escape {

    /**
     * 对字符串参数 str 执行命令替换和变量替换 <br>
     * 命令替换: 替换 `` 中的命令 <br>
     * 变量替换: 替换（会替换单引号中的变量占位符） $name ${name} $? $# $0 $1 格式的变量占位符 <br>
     *
     * @param session 用户会话信息
     * @param context 脚本引擎上下文信息
     * @param str     字符串
     * @param escape  true表示对字符串参数 str 进行转义（转义规则详见 {@linkplain #unescapeSQL(String)}}）, false表示不执行转义
     * @return
     */
    String replaceVariable(UniversalScriptSession session, UniversalScriptContext context, String str, boolean escape);

    /**
     * 对字符串参数 str 执行命令替换和变量替换 <br>
     * 命令替换: 替换 `` 中的命令 <br>
     * 变量替换: 替换（会保留单引号中的变量占位符） $name ${name} $? $# $0 $1 格式的变量占位符 <br>
     *
     * @param session      用户会话信息
     * @param context      脚本引擎上下文信息
     * @param str          字符串
     * @param removeQuote  true表示删除字符串参数 str 二端的单引号或双引号, false表示不作处理
     * @param keepVariable true表示保留变量值是null的变量占位符 false表示删除变量值是null的变量占位符
     * @param evalInnerCmd true表示执行命令替换 false表示不执行命令替换
     * @param escape       true表示对字符串参数 str 进行转义（转义规则详见 {@link #unescapeSQL(String)}}）, false表示不执行转义
     * @return
     */
    String replaceShellVariable(UniversalScriptSession session, UniversalScriptContext context, String str, boolean removeQuote, boolean keepVariable, boolean evalInnerCmd, boolean escape);

    /**
     * 返回语句分隔符，默认是半角分号
     *
     * @return
     */
    char getToken();

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
    int lastIndexOf(String str, String dest, int from, int left, int right);

    /**
     * 在脚本语句参数 script 中搜索字符数组 array 中元素第一次出现的位置
     *
     * @param script 脚本语句
     * @param array  字符数组
     * @param from   搜索起始位置, 从0开始
     * @return 返回 -1 表示不存在
     */
    int indexOf(CharSequence script, char[] array, int from);

    /**
     * 搜索单词序列 <br>
     * 第一个单词的左侧只能是空白或控制字符，右侧只能是空白字符 <br>
     * 最后一个单词的左侧只能是空白字符，右侧只能是空白或控制字符
     *
     * @param script 脚本语句
     * @param array  单词数组
     * @param from   搜索起始位置, 从0开始
     * @return 数组的第一个位置表示第一个单词的起始位置，第二个位置表示最后一个单词的起始位置, 返回null表示未找到字符词词组
     */
    int[] indexOf(CharSequence script, String[] array, int from);

    /**
     * 搜索整数结束的位置
     *
     * @param script 脚本语句
     * @param from   搜索起始位置, 从0开始
     * @return 整数的下一个位置
     */
    int indexOfInteger(CharSequence script, int from);

    /**
     * 搜索变量名结束位置
     *
     * @param script 缓存
     * @param from   变量名的起始位置
     * @return
     */
    int indexOfVariableName(String script, int from);

    /**
     * 搜索变量方法的结束位置
     *
     * @param script 脚本语句
     * @param from   搜索起始位置, 从0开始
     * @return
     */
    int indexOfVariableMethod(String script, int from);

    /**
     * 在字符串参数 str 中搜索命令替换符 ` 的结束位置（忽略转义字符右侧的字符）
     *
     * @param script 字符串
     * @param from   命令替换符的起始位置
     * @return -1表示命令替换符没有出现
     */
    int indexOfAccent(CharSequence script, int from);

    /**
     * 读取脚本语句的前缀 <br>
     * 命令前缀 或 自定义方法名 或 变量名
     *
     * @param script 脚本语句（左侧不能有空白字符）
     * @return
     */
    String getPrefix(String script);

    /**
     * 忽略空白字符并删除字符串二端的字符
     *
     * @param str   字符串
     * @param left  左端字符
     * @param right 右端字符
     * @return
     */
    String removeSide(CharSequence str, char left, char right);

    /**
     * 忽略字符串二端的空白字符，判断字符串二端是否存在字符参数 lc 与字符参数 rc
     *
     * @param str 字符串
     * @param lc  左侧字符（不能是空白字符）
     * @param rc  右侧字符（不能是空白字符）
     * @return
     */
    boolean containsSide(CharSequence str, char lc, char rc);

    /**
     * 删除字符串二端的空白字符
     *
     * @param str   字符串
     * @param left  0-表示删除左侧空白字符 1-表示删除左侧空白字符与语句分隔字符 2-表示删除左侧空白字符，语句分隔字符与字符数组参数 array 中的字符
     * @param right 0-表示删除右侧空白字符 1-表示删除右侧空白字符与语句分隔字符 2-表示删除右侧空白字符，语句分隔字符与字符数组参数 array
     * @param array
     * @return
     */
    String trim(CharSequence str, int left, int right, char... array);

    /**
     * 判断字符串是否为空白行 <br>
     * 注释行也算空白行
     *
     * @param str
     * @return
     */
    boolean isBlankline(CharSequence str);

    /**
     * 把SQL中的 {@literal &ads; } 替换为 $
     *
     * @param str
     * @return
     */
    String unescapeSQL(String str);

    /**
     * 返回一个单词分析器
     *
     * @param script
     * @return
     */
    WordIterator parse(String script);

}
