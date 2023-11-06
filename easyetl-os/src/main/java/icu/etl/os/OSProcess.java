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
     * @return
     */
    String getPid();

    /**
     * 父进程号
     *
     * @return
     */
    String getPPid();

    /**
     * 占用cpu百分比
     *
     * @return
     */
    BigDecimal getCpu();

    /**
     * 占用内存大小, 单位是pages，1个内存页是4096Bytes
     *
     * @return
     */
    long getMemory();

    /**
     * 进程名
     *
     * @return
     */
    String getName();

    /**
     * 返回进程运行的指令
     *
     * @return
     */
    String getCmd();

    /**
     * 杀掉操作系统进程
     *
     * @return 返回true表示已成功终止进程
     */
    boolean kill();

}
