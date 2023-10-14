package icu.etl.database.export;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.annotation.EasyBean;
import icu.etl.io.TableLine;

/**
 * 卸载数据的输出流接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-18
 */
@EasyBean(builder = ExtractWriterBuilder.class)
public interface ExtractWriter extends Flushable, Closeable {

    /**
     * 返回 true 表示可以写入新数据
     *
     * @return
     * @throws IOException
     * @throws SQLException
     */
    boolean rewrite() throws IOException, SQLException;

    /**
     * 将缓冲区中数据写入到输出流中
     *
     * @param line
     * @throws IOException
     */
    void write(TableLine line) throws IOException, SQLException;

}