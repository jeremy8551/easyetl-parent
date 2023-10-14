package icu.etl.os;

/**
 * 在操作系统上过滤文件时，使用的文件过滤规则
 */
public interface OSFileFilter {

    /**
     * 判断是否应该过滤文件
     *
     * @param file
     * @return 返回 <code>true</code> 表示不应该过滤文件 <code>false</code> 表示应该过滤文件.
     */
    boolean accept(OSFile file);

}
