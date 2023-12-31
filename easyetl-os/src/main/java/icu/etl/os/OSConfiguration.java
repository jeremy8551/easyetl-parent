package icu.etl.os;

import java.util.Collection;
import java.util.List;

public interface OSConfiguration {
    /**
     * 设置 IP 地址
     *
     * @param str IP或Host地址
     */
    void setHostname(String str);

    /**
     * 返回 IP 地址
     *
     * @return
     */
    String getHostname();

    /**
     * 返回 SSH 协议的端口号
     *
     * @return
     */
    int getSSHPort();

    /**
     * 添加一个数据库用户
     *
     * @param username 用户名
     * @param password 登录密码
     * @param isAdmin  true表示管理员帐号
     * @return
     */
    boolean addAccount(String username, String password, boolean isAdmin);

    /**
     * 添加一个 SSH 协议用户
     *
     * @param username 用户名
     * @param password 登录密码
     * @return
     */
    boolean addSSHAccount(String username, String password);

    /**
     * 返回默认的SSH账户
     *
     * @return
     */
    OSAccount getSSHAccount();

    /**
     * 返回账户信息集合
     *
     * @return
     */
    Collection<String> getAccountNames();

    /**
     * 返回账号信息
     *
     * @param name 账号名
     * @return
     */
    OSAccount getAccount(String name);

    /**
     * 返回账号集合（表示按用户权限从大到小排序）
     *
     * @return 账号集合
     */
    List<OSAccount> getAccounts();

    /**
     * 返回第一个添加的账号信息
     *
     * @return
     */
    OSAccount getAccount();

    /**
     * 返回一个 JDBC 配置信息副本
     *
     * @return 数据库配置信息
     */
    OSConfiguration clone();
}
