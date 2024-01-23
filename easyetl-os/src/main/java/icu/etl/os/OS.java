package icu.etl.os;

import java.util.List;

/**
 * 该接口用于描述操作系统的功能
 * 操作系统可以是本地操作系统，也可以是远程操作系统。可以是 linux，windows，unix，macos
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OS {

    /**
     * 返回操作系统名
     *
     * @return 操作系统名: linux, windows, unix, macos
     */
    String getName();

    /**
     * 127.0.0.1 表示本地操作系统 <br>
     * 其他表示是远程操作系统
     *
     * @return 操作系统host
     */
    String getHost();

    /**
     * 返回操作系统的发型版本号
     *
     * @return 发型版本号
     */
    String getReleaseVersion();

    /**
     * 返回操作系统的内核版本号
     *
     * @return 内核版本号
     */
    String getKernelVersion();

    /**
     * 操作系统默认的行间分隔符 <br>
     * windows 是 \r\n linxu 是 \n
     *
     * @return 行间分隔符
     */
    String getLineSeparator();

    /**
     * 返回操作系统的文件路径分隔符 <br>
     * windows 是 \\ linxu 是 /
     *
     * @return 文件路径分隔符
     */
    char getFolderSeparator();

    /**
     * 返回逻辑cpu信息
     *
     * @return 逻辑cpu信息
     */
    List<OSCpu> getOSCpus();

    /**
     * 返回存储信息（硬盘信息）
     *
     * @return 存储信息（硬盘信息）
     */
    List<OSDisk> getOSDisk();

    /**
     * 返回内存信息
     *
     * @return 内存信息
     */
    OSMemory getOSMemory();

    /**
     * 返回操作系统当前进程信息
     *
     * @param findStr 字符串
     * @return 当前进程信息
     */
    List<OSProcess> getOSProgressList(String findStr);

    /**
     * 根据进程编号查找对应的进程信息
     *
     * @param pid 进程编号
     * @return 进程信息
     */
    OSProcess getOSProgress(String pid);

    /**
     * 判断操作系统是否支持执行命令功能
     *
     * @return 返回true表示支持运行命令
     */
    boolean supportOSCommand();

    /**
     * 判断当前是否可以使用执行命令功能
     *
     * @return 返回true表示可以使用执行命令功能
     */
    boolean isEnableOSCommand();

    /**
     * 判断当前是否可以使用文件操作功能
     *
     * @return 返回true表示可以使用文件操作功能
     */
    boolean isEnableOSFileCommand();

    /**
     * 返回操作系统的命令功能接口，用于执行 shell 或 批处理语句
     *
     * @return 返回命令接口
     */
    OSCommand getOSCommand();

    /**
     * 打开操作系统的命令功能接口
     *
     * @return 返回true表示已打开命令接口 false打开命令接口失败
     */
    boolean enableOSCommand();

    /**
     * 关闭操作系统的命令功能接口
     */
    void disableOSCommand();

    /**
     * 判断是否支持查看操作系统网络配置信息
     *
     * @return 返回true表示支持网络配置
     */
    boolean supportOSNetwork();

    /**
     * 返回操作系统的网络配置信息
     *
     * @return 网络配置
     */
    OSNetwork getOSNetwork();

    /**
     * 判断操作系统是否支持文件功能接口
     *
     * @return 返回true表示支持文件功能
     */
    boolean supportOSFileCommand();

    /**
     * 返回操作系统的文件功能接口
     *
     * @return 文件功能接口
     */
    OSFileCommand getOSFileCommand();

    /**
     * 启用操作系统文件功能
     *
     * @return 返回true表示支持文件功能
     */
    boolean enableOSFileCommand();

    /**
     * 禁用操作系统的文件功能，禁用之后再使用文件功能会抛出异常
     */
    void disableOSFileCommand();

    /**
     * 关闭操作系统命令接口上打开的命令接口，ftp接口等需要连接的接口
     */
    void close();

    /**
     * 判断是否支持使用日期时间功能
     *
     * @return 返回true表示支持日期功能
     */
    boolean supportOSDateCommand();

    /**
     * 返回日期时间功能接口
     *
     * @return 日期功能接口
     */
    OSDateCommand getOSDateCommand();

    /**
     * 判断操作系统中是否已存在用户
     *
     * @param username 用户名
     * @return 返回true表示用户已存在
     */
    boolean hasUser(String username);

    /**
     * 返回操作系统当前使用的用户信息
     *
     * @return 用户信息
     */
    OSUser getUser();

    /**
     * 根据用户名查找对应的用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    OSUser getUser(String username);

    /**
     * 返回操作系统中所有用户信息
     *
     * @return 用户信息集合
     */
    List<OSUser> getUsers();

    /**
     * 返回操作系统中所有端口服务信息
     *
     * @return 服务信息集合
     */
    List<OSService> getOSServices();

    /**
     * 根据端口号查找对应的服务信息
     *
     * @param port 服务端口
     * @return 服务信息
     */
    OSService getOSService(int port);

    /**
     * 根据服务名查找对应的服务信息
     *
     * @param name 服务名
     * @return 服务信息集合
     */
    List<OSService> getOSService(String name);

    /**
     * 在操作系统中添加一个用户
     *
     * @param username 用户名
     * @param password 登录密码
     * @param group    用户所属组名
     * @param home     用户所在目录
     * @param shell    用户的shell
     * @return 返回true表示添加用户成功
     */
    boolean addUser(String username, String password, String group, String home, String shell);

    /**
     * 删除操作系统用户
     *
     * @param username 用户名
     * @return 返回true表示删除成功
     */
    boolean delUser(String username);

    /**
     * 修改操作系统中指定用户的密码
     *
     * @param username 用户名
     * @param password 新密码
     * @return 返回true表示修改密码成功
     */
    boolean changePassword(String username, String password);

    /**
     * 返回操作系统中所有用户组信息
     *
     * @return 用户组信息集合
     */
    List<OSUserGroup> getGroups();

}