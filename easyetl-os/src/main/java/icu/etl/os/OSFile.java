package icu.etl.os;

import java.util.Date;

/**
 * 该接口用于描述操作系统上的文件功能接口。 <br>
 * 操作系统可以是本地操作系统，也可以是远程linux，windows，unix，macos
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OSFile {

    /**
     * 返回文件在操作系统上的绝对路径
     *
     * @return
     */
    String getAbsolutePath();

    /**
     * 返回文件在操作系统上的名字
     *
     * @return
     */
    String getName();

    /**
     * 返回文件在操作系统上父目录的绝对路径
     *
     * @return
     */
    String getParent();

    /**
     * 返回文件在操作系统上详细信息 <br>
     * 如: -rw-r--r--@ 1 root root 111000 4 2 14:56 filename.txt
     *
     * @return
     */
    String getLongname();

    /**
     * 返回文件在操作系统上的占用空间，单位字节
     *
     * @return 字节大小
     */
    long length();

    /**
     * 返回文件在操作系统上的创建时间
     *
     * @return 日期时间
     */
    Date getCreateDate();

    /**
     * 返回文件在操作系统上最后一次修改时间
     *
     * @return
     */
    Date getModifyDate();

    /**
     * 判断文件在操作系统上是否是一个目录
     *
     * @return 日期时间
     */
    boolean isDirectory();

    /**
     * 判断文件在操作系统上是否是一个有效文件
     *
     * @return
     */
    boolean isFile();

    /**
     * 判断文件在操作系统上是否是一个链接文件
     *
     * @return
     */
    boolean isLink();

    /**
     * 如果文件在操作系统上是一个链接文件，则返回链接文件的地址
     *
     * @return
     */
    String getLink();

    /**
     * 判断文件在操作系统上是否是一个块设备
     *
     * @return
     */
    boolean isBlockDevice();

    /**
     * 判断文件在操作系统上是否是一个管道文件
     *
     * @return
     */
    boolean isPipe();

    /**
     * 判断文件在操作系统上是否是一个 socket 文件
     *
     * @return
     */
    boolean isSock();

    /**
     * 判断文件在操作系统上是否是一个字符设备
     *
     * @return
     */
    boolean isCharDevice();

    /**
     * 判断文件在操作系统上是否是有读权限
     *
     * @return
     */
    boolean canRead();

    /**
     * 判断文件在操作系统上是否是有写权限
     *
     * @return
     */
    boolean canWrite();

    /**
     * 判断文件在操作系统上是否是有执行权限
     *
     * @return
     */
    boolean canExecute();

}