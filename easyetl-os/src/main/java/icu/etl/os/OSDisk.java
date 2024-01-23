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
     * @return 分区编号
     */
    String getId();

    /**
     * 挂载位置 /boot
     *
     * @return 挂载位置
     */
    String getAmount();

    /**
     * 分区格式信息：ext3, tmpfs, iso9660
     *
     * @return 分区格式信息
     */
    String getType();

    /**
     * 硬盘或分区的总容量
     *
     * @return 硬盘或分区的总容量
     */
    BigDecimal total();

    /**
     * 硬盘或分区的可用容量
     *
     * @return 硬盘或分区的可用容量
     */
    BigDecimal free();

    /**
     * 硬盘或分区的已用容量
     *
     * @return 硬盘或分区的已用容量
     */
    BigDecimal used();

}
