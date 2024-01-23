package icu.etl.os;

/**
 * 操作系统帐号信息
 */
public interface OSAccount extends Cloneable {

    /**
     * 数据库用户名
     *
     * @return 用户名
     */
    String getUsername();

    /**
     * 密码
     *
     * @return 密码
     */
    String getPassword();

    /**
     * 判断账号是否是管理员账号
     *
     * @return 返回 true 表示管理员用户
     */
    boolean isAdmin();

    /**
     * 返回一个副本
     *
     * @return 副本
     */
    OSAccount clone();

}
