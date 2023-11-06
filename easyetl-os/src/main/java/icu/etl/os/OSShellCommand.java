package icu.etl.os;

import icu.etl.util.Charset;

/**
 * Shell命令接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-12 04:47:54
 */
public interface OSShellCommand extends OSConnectCommand, OSCommand, Charset {

    /** Shell配置 */
    public final static String profiles = "profiles";
    public final static String sshPort = "ssh";
    public final static String sshUser = "sshuser";
    public final static String sshUserPw = "sshUserPw";

}
