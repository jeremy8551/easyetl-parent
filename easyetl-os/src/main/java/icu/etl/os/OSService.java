package icu.etl.os;

import java.util.List;

/**
 * 用于描述操作系统端口服务信息<br>
 * 操作系统可以是本地操作系统，也可以是远程linux，windows，unix，macos
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OSService {

    /**
     * 端口服务名
     *
     * @return 端口服务名
     */
    String getName();

    /**
     * 端口号
     *
     * @return 端口号
     */
    int getPort();

    /**
     * 使用的网络协议信息 tcp 或 udp
     *
     * @return 网络协议信息 tcp 或 udp
     */
    String getProtocal();

    /**
     * 网络服务别名
     *
     * @return 网络服务别名
     */
    List<String> getAliases();
}
