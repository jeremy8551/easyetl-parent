package icu.etl.io;

import java.io.File;

import icu.etl.util.Charset;

/**
 * 文本文件接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-11
 */
public interface TextFile extends LineSeparator, Charset {

    /**
     * 设置文件路径 <br>
     * 此方法只负责设置参数,不应对参数进行检查及抛出异常
     *
     * @param filepath 绝对路径
     */
    void setAbsolutePath(String filepath);

    /**
     * 返回文件路径 <br>
     * 即使数据文件关闭后, 可返回最后设置值
     *
     * @return
     */
    String getAbsolutePath();

    /**
     * 设置文件默认的换行符
     *
     * @param str
     */
    void setLineSeparator(String str);

    /**
     * 返回数据文件
     *
     * @return
     */
    File getFile();

    /**
     * 删除文件
     *
     * @return
     */
    boolean delete();

}
