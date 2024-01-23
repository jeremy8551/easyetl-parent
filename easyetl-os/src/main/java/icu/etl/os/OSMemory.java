package icu.etl.os;

import java.math.BigDecimal;

/**
 * 用于描述操作系统中的内存容量信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OSMemory {

    /**
     * 操作系统上内存的总容量（单位字节）
     *
     * @return 总容量（单位字节）
     */
    BigDecimal total();

    /**
     * 操作系统上当前剩余内存容量（单位字节）
     *
     * @return 剩余内存容量（单位字节）
     */
    BigDecimal free();

    /**
     * 操作系统最近使用的内存容量（单位字节）
     *
     * @return 内存容量（单位字节）
     */
    BigDecimal active();

}
