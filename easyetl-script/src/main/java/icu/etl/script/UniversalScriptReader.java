package icu.etl.script;

import java.io.IOException;

/**
 * 脚本语句输入流
 */
public interface UniversalScriptReader {

    /**
     * 从输入流读取下一行内容时，首先返回字符串参数 str 作为下一行字符串的左侧前缀 <br>
     * 不能连续使用 {@link #setNextline(String)} 方法设置下一个行内容，设置完下一行内容后需要使用 read*Script() 方法读取下一个语句
     *
     * @param str 字符串, 不能为null
     * @throws IOException
     */
    void setNextline(String str) throws IOException;

    /**
     * 预览输入流的下一行字符串内容（即无需从输入流中读取就可以获得下一行字符串的内容）
     *
     * @return 返回 null 表示输入流已读取结束
     * @throws IOException
     */
    String previewline() throws IOException;

    /**
     * 从输入流中当前位置读取一行字符串
     *
     * @return
     * @throws IOException
     */
    String readLine() throws IOException;

    /**
     * 从输入流中当前位置读取一个单词 <br>
     * 语句中单词的分隔符：<br>
     * 1.空白字符 <br>
     * 2.回车或换行符 <br>
     * 3.引号中的空白字符与回车或换行符不能作为单词分隔符 <br>
     *
     * @return
     * @throws IOException
     */
    String readSingleWord() throws IOException;

    /**
     * 从输入流中当前位置读取一行语句 <br>
     * 要求语句只能用语句分隔符或回车符或换行符为语句的结束标志
     *
     * @return 语句二端不会留有空白字符，且删除语句最右端的语句分隔符
     * @throws IOException
     */
    String readSinglelineScript() throws IOException;

    /**
     * 从输入流中当前位置读取一个（可能是）跨越多行的语句 <br>
     * 要求语句只能用语句分隔符（{@linkplain UniversalScriptAnalysis#getToken()}）作为结束标志 <br>
     * 如: <br>
     * select * from table with ur; <br>
     *
     * @return
     * @throws IOException
     */
    String readMultilineScript() throws IOException;

    /**
     * 从输入流中当前位置读取一个（可能是）跨越多行的语句 <br>
     * 要求语句中必须要有单词参数 begin 且语句要以单词参数 end 作为结束标志 <br>
     * 起始单词要与结束单词成对出现，且支持单词嵌套
     *
     * @param begin 起始单词（如果是词组，需要使用空白作为单词的分隔符）
     * @param end   结束单词（如果是词组，需要使用空白作为单词的分隔符）
     * @return
     * @throws IOException
     */
    String readPieceofScript(String begin, String end) throws IOException;

    /**
     * 从输入流中当前位置读取一行语句 <br>
     * 要求语句中必须要有单词参数 str 且语句要以单词参数 str 作为结束标志
     *
     * @param str
     * @return
     * @throws IOException
     */
    String readSymmetryScript(String str) throws IOException;

    /**
     * 返回当前读取的行号
     *
     * @return
     */
    long getLineNumber();

}