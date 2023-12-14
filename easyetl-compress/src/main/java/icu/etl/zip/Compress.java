package icu.etl.zip;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 压缩接口 <br>
 * <br>
 * <b>使用例子:</b> <br>
 * String input = "C:\\SEC.sql"; <br>
 * String outFile = "C:\\cshi.zip"; <br>
 * File file = new File(input); <br>
 * File zip = new File(outFile); <br>
 * <br>
 * <b>初始化操作</b> <br>
 * Compress jc = CompressFactory.getCompressImpl("zip"); <br>
 * <br>
 * <b>设置压缩文件</b> <br>
 * jc.setFile(zip); <br>
 * <br>
 * <b>把文件添加到压缩包中 test/dir 目录下</b> <br>
 * jc.archiveFile(file, "test/dir", null); <br>
 * <br>
 * <b>在压缩包中搜索执行文件名的文件</b> <br>
 * {@literal List<ZipEntry> list = jc.getZipEntrys("gbk", "SEC.sql", true); }<br>
 * <b>把文件添加到压缩包的根目录</b> <br>
 * jc.archiveFile(file, null); <br>
 * <br>
 * <b>删除压缩包根目录下的 SEC.sql 文件</b> <br>
 * jc.removeZipEntry(null, "SEC.sql"); <br>
 * <br>
 * <b>关闭并释放所有资源</b> <br>
 * jc.close(); <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-07-23 16:21:38
 */
public interface Compress extends Closeable {

    /**
     * 设置压缩文件
     *
     * @param file 压缩文件
     */
    void setFile(File file) throws IOException;

    /**
     * 把文件或目录压缩添加到压缩文件的指定目录（默认使用file.encoding字符集作为默认字符集）
     *
     * @param file 文件
     * @param dir  文件在压缩文件中的目录 <br>
     *             null或空字符串表zip文件的根目录 <br>
     *             字符串的第一个字符不能是 ‘/’ 符号 <br>
     * @throws IOException 文件访问错误
     */
    void archiveFile(File file, String dir) throws IOException;

    /**
     * 添加文件到压缩包中指定目录
     *
     * @param file        文件
     * @param dir         文件在压缩文件中的目录 <br>
     *                    null或空字符串表zip文件的根目录 <br>
     *                    字符串的第一个字符不能是 ‘/’ 符号 <br>
     * @param charsetName 文件的字符集编码
     * @throws IOException 文件访问错误
     */
    void archiveFile(File file, String dir, String charsetName) throws IOException;

    /**
     * 解压压缩包到指定目录
     *
     * @param outputDir   解压目录
     * @param charsetName zip文件字符编码(如： UTF-8等)
     * @throws IOException 文件访问错误
     */
    void extract(String outputDir, String charsetName) throws IOException;

    /**
     * 解压压缩包到指定目录
     *
     * @param outputDir   解压目录
     * @param charsetName zip文件字符编码(如： UTF-8等)
     * @param entryName   entry名（如：zipfile/test.txt）
     * @throws IOException 文件访问错误
     */
    void extract(String outputDir, String charsetName, String entryName) throws IOException;

    /**
     * 在压缩包中搜索指定文件
     *
     * @param charsetName 文件字符集
     * @param filename    文件名
     * @param ignoreCase  true忽略文件名的大小写
     * @return 集合
     */
    List<?> getEntrys(String charsetName, String filename, boolean ignoreCase) throws IOException;

    /**
     * 删除压缩包中的文件
     *
     * @param charsetName 文件字符集
     * @param entryName   待删除文件名（如： cshi/SEC.sql）
     * @return 返回true表示操作成功 false表示操作失败
     * @throws IOException 文件访问错误
     */
    boolean removeEntry(String charsetName, String... entryName) throws IOException;

    /**
     * 终止压缩文件或解压文件操作
     */
    void terminate();

    /**
     * 终止操作
     *
     * @return 返回true表示已终止操作 false表示还未执行终止操作
     */
    boolean isTerminate();

}