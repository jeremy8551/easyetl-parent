package icu.etl.database.export;

import java.io.Closeable;
import java.io.Flushable;

import icu.etl.io.TableLine;

/**
 * 卸载数据的输出流接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-18
 */
public interface ExtractWriter extends Flushable, Closeable {

    /**
     * 返回 true 表示可以写入新数据
     *
     * @return
     * @throws Exception
     */
    boolean rewrite() throws Exception;

    /**
     * 将缓冲区中数据写入到输出流中
     *
     * @param line
     * @throws Exception
     */
    void write(TableLine line) throws Exception;

}