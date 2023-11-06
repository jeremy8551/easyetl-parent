package icu.etl.database.export;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.io.TableLine;

public interface ExtractReader extends TableLine {

    /**
     * 将数据保存到缓冲区
     *
     * @return
     * @throws IOException
     * @throws SQLException
     */
    boolean hasLine() throws IOException, SQLException;

    /**
     * 关闭输入流
     */
    void close();

}