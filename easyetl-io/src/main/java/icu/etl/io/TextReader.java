package icu.etl.io;

import java.io.IOException;

public interface TextReader {

    /**
     * 读取下一行
     *
     * @return 返回 null 表示已读取到结尾
     * @throws IOException
     */
    String readLine() throws IOException;

}
