package icu.etl.os;

import java.util.List;

/**
 * 接口操作系统的用户信息。<br>
 * 操作系统可以是本地操作系统，也可以是远程linux，windows，unix，macos
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OSUser {

    /**
     * 操作系统用户名
     *
     * @return 用户名
     */
    String getName();

    /**
     * 操作系统用户登录密码
     *
     * @return 密码
     */
    String getPassword();

    /**
     * 修改用户的密码
     *
     * @param password 新密码
     */
    void setPassword(String password);

    /**
     * 返回操作系统用户的编号
     *
     * @return 用户的编号
     */
    String getId();

    /**
     * 返回操作系统归属组的编号
     *
     * @return 归属组的编号
     */
    String getGroup();

    /**
     * 返回操作系统用户的说明信息
     *
     * @return 说明信息
     */
    String getMemo();

    /**
     * 返回操作系统用户的根目录的绝对路径
     *
     * @return 根目录的绝对路径
     */
    String getHome();

    /**
     * 返回操作系统用户使用的命令类型
     *
     * @return 命令类型
     */
    String getShell();

    /**
     * 判断当前操作系统用户是否是 root 用户
     *
     * @return 返回true表示是 root 用户
     */
    boolean isRoot();

    /**
     * 返回操作系统用户的配置文件集合
     *
     * @return 配置文件集合
     */
    List<String> getProfiles();

}