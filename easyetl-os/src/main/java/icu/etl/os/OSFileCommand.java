package icu.etl.os;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import icu.etl.util.Charset;

/**
 * 该接口用于描述操作系统的文件操作功能接口<br>
 * 操作系统可以是本地操作系统，也可以是远程linux，windows，unix，macos
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OSFileCommand extends Charset {

    /**
     * 使用操作系统当前用户进入到操作系统上指定目录
     *
     * @param filepath 文件目录的绝对路径
     * @return 返回true表示进入目录成功 返回false表示操作失败
     */
    boolean cd(String filepath);

    /**
     * 返回操作系统当前用户所在目录的绝对路径
     *
     * @return 返回当前目录的绝对路径
     */
    String pwd();

    /**
     * 判断在操作系统上是否存在文件或目录
     *
     * @param filepath 文件或目录的绝对路径
     * @return 返回true表示文件或目录存在 返回false表示文件或目录不存在
     */
    boolean exists(String filepath);

    /**
     * 判断在操作系统上是否存在文件
     *
     * @param filepath 文件的绝对路径
     * @return 返回true表示文件存在 返回false表示非文件
     */
    boolean isFile(String filepath);

    /**
     * 判断在操作系统上是否存在目录
     *
     * @param filepath 目录的绝对路径
     * @return 返回true表示目录存在 返回false表示非目录
     */
    boolean isDirectory(String filepath);

    /**
     * 在远程服务器上创建一个目录
     *
     * @param filepath 目录的绝对路径
     * @return 返回true表示创建目录成功 返回false表示创建目录失败
     * @throws IOException
     */
    boolean mkdir(String filepath) throws IOException;

    /**
     * 删除操作系统上的文件或目录
     *
     * @param filepath 文件或目录的绝对路径
     * @return 返回true表示删除成功 返回false表示删除失败
     * @throws IOException
     */
    boolean rm(String filepath) throws IOException;

    /**
     * 显示操作系统上文件或目录的详细信息
     *
     * @param filepath 文件或目录的绝对路径
     * @return 返回文件列表信息集合
     * @throws IOException
     */
    List<OSFile> ls(String filepath) throws IOException;

    /**
     * 上传文件或目录到操作系统上的指定目录下
     *
     * @param localFile 文件或目录
     * @param filepath  操作系统上的目录绝对路径
     * @return 返回true表示上传文件成功 返回false表示上传文件失败
     * @throws IOException
     */
    boolean upload(File localFile, String filepath) throws IOException;

    /**
     * 上传文件到操作系统上的指定目录下
     *
     * @param in
     * @param remote
     * @return
     * @throws IOException
     */
    boolean upload(InputStream in, String remote) throws IOException;

    /**
     * 从操作系统上下载一个文件或目录到指定目录下
     *
     * @param filepath 在操作系统上的文件或目录的绝对路径
     * @param localDir 本地目录
     * @return 返回true表示下载文件成功 返回false表示下载文件失败
     * @throws IOException
     */
    File download(String filepath, File localDir) throws IOException;

    /**
     * 从操作系统上下载文件
     *
     * @param remote
     * @param out
     * @return
     * @throws IOException
     */
    boolean download(String remote, OutputStream out) throws IOException;

    /**
     * 对操作系统上的文件进行重命名
     *
     * @param filepath    在操作系统上的文件或目录的绝对路径
     * @param newfilepath 重命名后的文件或目录的绝对路径
     * @return 返回true表示重命名文件成功 返回false表示重命名文件失败
     * @throws IOException
     */
    boolean rename(String filepath, String newfilepath) throws IOException;

    /**
     * 在操作系统上查找信息
     *
     * @param directory 查找的根目录
     * @param filename  文件名或通配符(e.g: *.txt, pid.txt)
     * @param type      类型：d-表示目录 f-表示文件
     * @param filter    表示文件过滤器
     * @return 返回符合查询条件的文件集合
     * @throws IOException
     */
    List<OSFile> find(String directory, String filename, char type, OSFileFilter filter) throws IOException;

    /**
     * 读取操作系统上一个文件的内容
     *
     * @param filepath    文件的绝对路径
     * @param charsetName 文件的字符集
     * @param lineno      读取的行号, 0 表示读取全部内容, -1 表示读取最后一行
     * @return 返回读取文件的内容
     * @throws IOException
     */
    String read(String filepath, String charsetName, int lineno) throws IOException;

    /**
     * 向操作系统上指定文件中写入字符信息
     *
     * @param filepath    文件的绝对路径
     * @param charsetName 文件的字符集编码
     * @param append      <code>true</code>表示向文件中追加写入，false表示覆盖文件内容
     * @param content     字符信息
     * @return 返回true表示写文件成功 返回false表示写入失败
     * @throws IOException
     */
    boolean write(String filepath, String charsetName, boolean append, CharSequence content) throws IOException;

    /**
     * 在操作系统上复制文件或目录到指定的目录下
     *
     * @param filepath  文件的绝对路径
     * @param directory 操作系统上的目录
     * @return 返回true表示复制成功 返回false表示复制失败
     * @throws IOException
     */
    boolean copy(String filepath, String directory) throws IOException;

}
