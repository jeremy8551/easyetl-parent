package icu.etl.os;

import java.io.OutputStream;
import java.util.List;

import icu.etl.concurrent.Terminate;

/**
 * 该接口用于描述操作系统上的命令功能。 <br>
 * <br>
 * 例如，在linux，unix，macos上执行shell命令功能<br>
 * 或在Windows上执行cmd命令功能<br>
 * 操作系统可以是本地操作系统，也可以是远程linux，windows，unix，macos
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OSCommand extends Terminate {

    /**
     * 在操作系统上执行命令
     *
     * @param command 命令语句，比如 Linux 系统的 shell 语句或windows系统的批处理语句
     * @return 返回0表示正确，返回非0表示错误
     * @throws OSCommandException
     */
    int execute(String command) throws OSCommandException;

    /**
     * 在操作系统上执行命令, 如果越过了超时时间自动终止执行命令
     *
     * @param command 命令语句，比如 Linux 系统的 shell 语句或windows系统的批处理语句
     * @param timeout 命令的超时时间 (单位毫秒)，0 表示未设置超时时间
     * @return 返回0表示正确，返回非0表示错误
     * @throws OSCommandException
     */
    int execute(String command, long timeout) throws OSCommandException;

    /**
     * 在操作系统上执行命令, 如果越过了超时时间自动终止执行命令
     *
     * @param command 命令语句，比如 Linux 系统的 shell 语句或windows系统的批处理语句
     * @param timeout 命令的超时时间 (单位毫秒)，0 表示未设置超时时间
     * @param stdout  用于输出命令标准信息的输出流
     * @param stderr  用于输出命令错误信息的输出流
     * @return 返回0表示正确，返回非0表示错误
     * @throws OSCommandException
     */
    int execute(String command, long timeout, OutputStream stdout, OutputStream stderr) throws OSCommandException;

    /**
     * 按集合中的先后顺序执行命令（忽略命令执行时发生错误继续向下执行），并返回每个命令的标准输出信息
     *
     * @param commands 命令数组
     * @return 返回命令对应的标准输出信息集合
     * @throws OSCommandException
     */
    OSCommandStdouts execute(String... commands) throws OSCommandException;

    /**
     * 按集合中的先后顺序执行命令（忽略命令执行时发生错误继续向下执行），并返回每个命令的标准输出信息
     *
     * @param commands 命令集合
     * @return 返回命令对应的标准输出信息集合
     * @throws OSCommandException
     */
    OSCommandStdouts execute(List<String> commands) throws OSCommandException;

    /**
     * 判断操作系统是否支持使用标准信息输出流
     *
     * @return
     */
    boolean supportStdout();

    /**
     * 判断操作系统是否支持使用错误信息输出流
     *
     * @return
     */
    boolean supportStderr();

    /**
     * 设置执行命令使用的标准信息输出流
     *
     * @param os 输出流
     */
    void setStdout(OutputStream os);

    /**
     * 设置执行命令使用的错误信息输出流
     *
     * @param os
     */
    void setStderr(OutputStream os);

    /**
     * 返回命令输出的标准信息
     *
     * @return
     */
    String getStdout();

    /**
     * 返回命令输出的错误信息
     *
     * @return
     */
    String getStderr();

    /**
     * 返回命令输出的标准信息
     *
     * @param charsetName 标准信息的字符集编码
     * @return
     */
    String getStdout(String charsetName);

    /**
     * 返回命令输出的错误信息
     *
     * @param charsetName 错误信息的字符集编码
     * @return
     */
    String getStderr(String charsetName);

    /**
     * 返回命令输出信息的默认字符集编码
     *
     * @return
     */
    String getCharsetName();

    /**
     * 返回执行属性值
     *
     * @param key 属性名
     * @return
     */
    Object getAttribute(String key);

}
