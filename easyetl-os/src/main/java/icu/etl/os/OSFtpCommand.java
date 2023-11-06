package icu.etl.os;

/**
 * FTP 协议接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-08-10
 */
public interface OSFtpCommand extends OSConnectCommand, OSFileCommand {

    /**
     * 终止正在进行的传输
     */
    void terminate();
}