package icu.etl.io;

/**
 * 转义字符接口
 *
 * @author jeremy8551@qq.com
 */
public interface Escape {

    /**
     * 设置转义字符 <br>
     * 此方法只负责设置参数,不应对参数进行检查及抛出异常
     *
     * @param c
     */
    void setEscape(char c);

    /**
     * 转义字符<br>
     * 即使数据文件关闭后, 可返回最后设置值
     *
     * @return
     */
    char getEscape();

    /**
     * true表示已设置转义字符
     *
     * @return
     */
    boolean existsEscape();

    /**
     * 移除转义字符设置
     */
    void removeEscape();

}
