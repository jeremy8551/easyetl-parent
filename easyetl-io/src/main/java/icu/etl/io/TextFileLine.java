package icu.etl.io;

/**
 * 表示文件中的一行记录
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-12
 */
public interface TextFileLine extends LineSeparator {

    /**
     * 记录内容
     *
     * @return 记录内容
     */
    String getContent();

    /**
     * 设置当前行记录内容
     */
    void setContext(String line);

}
