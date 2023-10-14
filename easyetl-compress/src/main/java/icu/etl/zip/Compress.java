package icu.etl.zip;

import java.io.File;
import java.io.IOException;
import java.util.List;

import icu.etl.annotation.EasyBean;
import icu.etl.annotation.EasyBeanClass;

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
 * <br>
 * <br>
 * 实现类注解的填写规则: <br>
 * {@linkplain EasyBeanClass#kind()} 属性表示语言, 如: gz, zip, rar <br>
 * {@linkplain EasyBeanClass#mode()} 属性未使用, 填空字符串 <br>
 * {@linkplain EasyBeanClass#major()} 属性未使用, 填空字符串 <br>
 * {@linkplain EasyBeanClass#minor()} 属性未使用, 填空字符串 <br>
 * {@linkplain EasyBeanClass#description()} 属性表示描述信息 <br>
 * {@linkplain EasyBeanClass#type()} 属性必须填写 {@linkplain Compress}.class <br>
 * <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-07-23 16:21:38
 */
@EasyBean(builder = CompressBuilder.class)
public interface Compress extends java.io.Closeable {

    /**
     * zip文件格式
     */
    public final static String ZIP_FORMAT_FILE = "ZIP";

    /**
     * tar文件格式
     */
    public final static String TAR_FORMAT_FILE = "TAR";

    /**
     * rar文件格式
     */
    public final static String RAR_FORMAT_FILE = "RAR";

    /**
     * tar.gz 文件格式
     */
    public final static String TARGZ_FORMAT_FILE = "TAR.GZ";

    /**
     * .gz 文件格式
     */
    public final static String GZ_FORMAT_FILE = "GZ";

    /**
     * 设置压缩文件
     *
     * @param file
     */
    public void setFile(File file);

    /**
     * 把文件或目录压缩添加到压缩文件的指定目录（默认使用file.encoding字符集作为默认字符集）
     *
     * @param file 文件
     * @param dir  文件在压缩文件中的目录 <br>
     *             null或空字符串表zip文件的根目录 <br>
     *             字符串的第一个字符不能是 ‘/’ 符号 <br>
     * @throws IOException
     */
    public void archiveFile(File file, String dir) throws IOException;

    /**
     * 添加文件到压缩包中指定目录
     *
     * @param file    文件
     * @param dir     文件在压缩文件中的目录 <br>
     *                null或空字符串表zip文件的根目录 <br>
     *                字符串的第一个字符不能是 ‘/’ 符号 <br>
     * @param charset 文件的字符集编码
     * @throws IOException
     */
    public void archiveFile(File file, String dir, String charset) throws IOException;

    /**
     * 解压压缩包到指定目录
     *
     * @param outputDir   解压目录
     * @param charsetName zip文件字符编码(如： UTF-8等)
     * @throws IOException
     */
    public void extract(String outputDir, String charsetName) throws IOException;

    /**
     * 解压压缩包到指定目录
     *
     * @param outputDir   解压目录
     * @param charsetName zip文件字符编码(如： UTF-8等)
     * @param entryName   entry名（如：zipfile/test.txt）
     * @throws IOException
     */
    public void extract(String outputDir, String charsetName, String entryName) throws IOException;

    /**
     * 在压缩包中搜索指定文件
     *
     * @param charsetName 文件字符集
     * @param filename    文件名
     * @param ignoreCase  true忽略文件名的大小写
     * @return
     */
    public List<?> getEntrys(String charsetName, String filename, boolean ignoreCase);

    /**
     * 删除压缩包中的文件
     *
     * @param charsetName 文件字符集
     * @param entryName   待删除文件名（如： cshi/SEC.sql）
     * @return
     * @throws IOException
     */
    public boolean removeEntry(String charsetName, String... entryName) throws IOException;

    /**
     * 终止压缩文件或解压文件操作
     */
    public void terminate();

    /**
     * 返回 true 表示已终止操作
     *
     * @return
     */
    public boolean isTerminate();

}