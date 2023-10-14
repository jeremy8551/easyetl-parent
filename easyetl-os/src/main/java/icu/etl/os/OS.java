package icu.etl.os;

import java.util.List;

import icu.etl.annotation.EasyBean;
import icu.etl.annotation.EasyBeanClass;

/**
 * 该接口用于描述操作系统的功能 <br>
 * 操作系统可以是本地操作系统，也可以是远程操作系统。可以是 linux，windows，unix，macos <br>
 * <br>
 * 实现类注解的填写规则: <br>
 * {@linkplain EasyBeanClass#kind()} 属性表示操作系统名, 如: linux，windows <br>
 * {@linkplain EasyBeanClass#mode()} 属性表示操作系统类型, 如: remote表示远程操作系统 local表示本地操作系统 <br>
 * {@linkplain EasyBeanClass#major()} 属性表示操作系统大版本号, 只能是整数 <br>
 * {@linkplain EasyBeanClass#minor()} 属性表示操作系统小版本号, 只能是整数 <br>
 * {@linkplain EasyBeanClass#description()} 属性表示描述信息 <br>
 * {@linkplain EasyBeanClass#type()} 属性必须填写 {@linkplain OS}.class <br>
 * <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
@EasyBean(builder = OSBuilder.class)
public interface OS {

    /**
     * 返回操作系统名: linux, windows, unix, macos
     *
     * @return
     */
    String getName();

    /**
     * 127.0.0.1 表示本地操作系统 <br>
     * 其他表示是远程操作系统
     *
     * @return
     */
    String getHost();

    /**
     * 返回操作系统的发型版本号
     *
     * @return
     */
    String getReleaseVersion();

    /**
     * 返回操作系统的内核版本号
     *
     * @return
     */
    String getKernelVersion();

    /**
     * 操作系统默认的行间分隔符 <br>
     * windows 是 \r\n linxu 是 \n
     *
     * @return
     */
    String getLineSeparator();

    /**
     * 返回操作系统的文件路径分隔符 <br>
     * windows 是 \\ linxu 是 /
     *
     * @return
     */
    char getFolderSeparator();

    /**
     * 返回逻辑cpu信息
     *
     * @return
     */
    List<OSCpu> getOSCpus();

    /**
     * 返回存储信息（硬盘信息）
     *
     * @return
     */
    List<OSDisk> getOSDisk();

    /**
     * 返回内存信息
     *
     * @return
     */
    OSMemory getOSMemory();

    /**
     * 返回操作系统当前进程信息
     *
     * @param findStr
     * @return
     */
    List<OSProcess> getOSProgressList(String findStr);

    /**
     * 根据进程编号查找对应的进程信息
     *
     * @param pid 进程编号
     * @return
     */
    OSProcess getOSProgress(String pid);

    /**
     * 判断操作系统是否支持执行命令功能
     *
     * @return
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
     * @return
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
     * @return
     */
    boolean supportOSNetwork();

    /**
     * 返回操作系统的网络配置信息
     *
     * @return
     */
    OSNetwork getOSNetwork();

    /**
     * 判断操作系统是否支持文件功能接口
     *
     * @return
     */
    boolean supportOSFileCommand();

    /**
     * 返回操作系统的文件功能接口
     *
     * @return
     */
    OSFileCommand getOSFileCommand();

    /**
     * 启用操作系统文件功能
     *
     * @return
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
     * @return
     */
    boolean supportOSDateCommand();

    /**
     * 返回日期时间功能接口
     *
     * @return
     */
    OSDateCommand getOSDateCommand();

    /**
     * 判断操作系统中是否已存在用户
     *
     * @param username 用户名
     * @return
     */
    boolean hasUser(String username);

    /**
     * 返回操作系统当前使用的用户信息
     *
     * @return
     */
    OSUser getUser();

    /**
     * 根据用户名查找对应的用户信息
     *
     * @param username 用户名
     * @return
     */
    OSUser getUser(String username);

    /**
     * 返回操作系统中所有用户信息
     *
     * @return
     */
    List<OSUser> getUsers();

    /**
     * 返回操作系统中所有端口服务信息
     *
     * @return
     */
    List<OSService> getOSServices();

    /**
     * 根据端口号查找对应的服务信息
     *
     * @param port
     * @return
     */
    OSService getOSService(int port);

    /**
     * 根据服务名查找对应的服务信息
     *
     * @param name
     * @return
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
     * @return
     */
    boolean changePassword(String username, String password);

    /**
     * 返回操作系统中所有用户组信息
     *
     * @return
     */
    List<OSUserGroup> getGroups();

}