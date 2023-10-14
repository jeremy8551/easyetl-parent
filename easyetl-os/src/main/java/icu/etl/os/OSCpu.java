package icu.etl.os;

/**
 * 逻辑CPU信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OSCpu {

    /**
     * 所属物理CPU的编号
     *
     * @return
     */
    String getId();

    /**
     * 返回 CPU 型号信息
     *
     * @return
     */
    String getModelName();

    /**
     * 返回所属CPU的核数
     *
     * @return
     */
    int getCores();

}
