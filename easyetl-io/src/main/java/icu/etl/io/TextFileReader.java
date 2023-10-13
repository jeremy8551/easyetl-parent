package icu.etl.io;

import java.io.Closeable;

/**
 * 按行读取字符串
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-01-31
 */
public interface TextFileReader extends Closeable, LineNumber, LineSeparator, TextReader {
}
