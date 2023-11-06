package icu.etl.os;

/**
 * 操作系统帐号信息
 */
public interface OSAccount extends Cloneable {

    /**
     * 数据库用户名
     *
     * @return
     */
    String getUsername();

    /**
     * 密码
     *
     * @return
     */
    String getPassword();

    /**
     * 返回 true 表示管理员用户
     *
     * @return
     */
    boolean isAdmin();

    /**
     * 返回一个副本
     *
     * @return
     */
    OSAccount clone();

}
