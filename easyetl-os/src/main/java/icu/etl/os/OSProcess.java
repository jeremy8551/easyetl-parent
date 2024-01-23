package icu.etl.os;

import java.math.BigDecimal;

/**
 * 本接口用于描述操作系统上的进程信息<br>
 * 操作系统可以是本地操作系统，也可以是远程linux，windows，unix，macos
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OSProcess {

    /**
     * 进程号
     *
     * @return 进程号
     */
    String getPid();

    /**
     * 父进程号
     *
     * @return 父进程号
     */
    String getPPid();

    /**
     * 占用cpu百分比
     *
     * @return cpu百分比
     */
    BigDecimal getCpu();

    /**
     * 占用内存大小, 单位是pages，1个内存页是4096Bytes
     *
     * @return 占用内存大小
     */
    long getMemory();

    /**
     * 进程名
     *
     * @return 进程名
     */
    String getName();

    /**
     * 返回进程运行的指令
     *
     * @return 进程运行的指令
     */
    String getCmd();

    /**
     * 杀掉操作系统进程
     *
     * @return 返回true表示已成功终止进程
     */
    boolean kill();

}
