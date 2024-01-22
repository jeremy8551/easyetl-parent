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
     * 判断是否可以写入新数据
     *
     * @return 返回 true 表示可以写入新数据
     * @throws Exception 写入数据发生错误
     */
    boolean rewrite() throws Exception;

    /**
     * 将缓冲区中数据写入到输出流中
     *
     * @param line 行信息
     * @throws Exception 写入数据发生错误
     */
    void write(TableLine line) throws Exception;

}