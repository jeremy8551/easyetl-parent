package icu.etl.database.export;

import icu.etl.io.TableLine;

public interface ExtractReader extends TableLine {

    /**
     * 将数据保存到缓冲区
     *
     * @return
     * @throws Exception
     */
    boolean hasLine() throws Exception;

    /**
     * 关闭输入流
     */
    void close();

}