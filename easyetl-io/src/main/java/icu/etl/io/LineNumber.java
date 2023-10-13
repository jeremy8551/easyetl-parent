package icu.etl.io;

/**
 * 文件或数据行数设置
 *
 * @author jeremy8551@qq.com
 */
public interface LineNumber {

    /**
     * 当前文件或数据的行号，从1开始 0表示未读行
     *
     * @return
     */
    long getLineNumber();

}
