package icu.etl.os;

import java.math.BigDecimal;

/**
 * 用于描述操作系统中的硬盘信息或逻辑分区信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OSDisk {

    /**
     * 分区编号 /dev/mapper/VolGroup00-LogVol00
     *
     * @return
     */
    String getId();

    /**
     * 挂在位置 /boot
     *
     * @return
     */
    String getAmount();

    /**
     * 分区格式信息：ext3, tmpfs, iso9660
     *
     * @return
     */
    String getType();

    /**
     * 硬盘或分区的总容量
     *
     * @return
     */
    BigDecimal total();

    /**
     * 硬盘或分区的可用容量
     *
     * @return
     */
    BigDecimal free();

    /**
     * 硬盘或分区的已用容量
     *
     * @return
     */
    BigDecimal used();

}
