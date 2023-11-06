package icu.etl.jdk;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;

/**
 * JDK 方言接口
 *
 * @author jeremy8551@qq.com
 */
public interface JavaDialect {

    /**
     * 返回数据库连接网络的超时时间
     *
     * @param conn 数据库连接
     * @return 超时时间
     * @throws SQLException 数据库错误
     */
    int getNetworkTimeout(Connection conn) throws SQLException;

    /**
     * 设置数据库连接配置信息
     *
     * @param conn 数据库连接
     * @param p    数据库连接的属性
     * @throws SQLException 数据库错误
     */
    void setClientInfo(Connection conn, Properties p) throws SQLException;

    /**
     * 通过数据库连接查询属性信息
     *
     * @param conn 数据库连接
     * @return 数据库连接的属性
     * @throws SQLException 数据库错误
     */
    Properties getClientInfo(Connection conn) throws SQLException;

    /**
     * 返回 true 表示可以执行文件
     *
     * @param file 文件
     * @return true表示文件可执行
     */
    boolean canExecute(File file);

    /**
     * 返回当前可用的线程数
     *
     * @return 线程数
     */
    int getAvailableThreads();

    /**
     * 将Jdbc参数 object 转为脚本引擎内部类型
     *
     * @param obj 对象
     * @return 脚本引擎内部类型的对象
     * @throws IOException  IO错误
     * @throws SQLException 数据库错误
     */
    Object parseJdbcObject(Object obj) throws IOException, SQLException;

    /**
     * 返回 true 表示参数 Statement 对象已关闭
     *
     * @param statement Statement对象
     * @return true表示 Statement 已关闭
     * @throws SQLException 数据库错误
     */
    boolean isStatementClosed(Statement statement) throws SQLException;

    /**
     * 返回 true 表示字符参数 ub 是一个汉字
     *
     * @param ub 字符子集
     * @return true表示字符是中文
     */
    boolean isChineseLetter(Character.UnicodeBlock ub);

    /**
     * 如果文件是一个链接，则返回链接文件的绝对路径
     * 如果文件不是链接，则返回null
     *
     * @param file 文件
     * @return null表示文件不是链接，返回链接文件的绝对路径
     */
    String getLink(File file);

    /**
     * 返回文件的创建时间
     *
     * @param filepath 文件绝对路径
     * @return 文件的创建时间
     */
    Date getCreateTime(String filepath);

    /**
     * 生成类似于 unix 中组用户和其他用的读写执行权限
     *
     * @param file
     * @return 6位的字符，前三位是组用户的读写执行权限，后三位是其他用户的读写执行权限
     */
    String toLongname(File file);

}
