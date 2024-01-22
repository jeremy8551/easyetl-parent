package icu.etl.database.export;

import icu.etl.io.TableLine;

public interface ExtractReader extends TableLine {

    /**
     * 将数据保存到缓冲区
     *
     * @return 返回true表示成功读取到一行数据 false表示没有数据可以读取
     * @throws Exception 发生错误
     */
    boolean hasLine() throws Exception;

    /**
     * 关闭输入流
     */
    void close();

}