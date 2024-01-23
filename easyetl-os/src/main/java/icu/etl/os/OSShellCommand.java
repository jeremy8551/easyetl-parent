package icu.etl.os;

import icu.etl.util.CharsetName;

/**
 * Shell命令接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-12 04:47:54
 */
public interface OSShellCommand extends OSConnectCommand, OSCommand, CharsetName {

    /** Shell配置 */
    String profiles = "profiles";
    String sshPort = "ssh";
    String sshUser = "sshuser";
    String sshUserPw = "sshUserPw";

}
