package icu.etl.os;

import java.util.List;

/**
 * 用于描述操作系统网络配置信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OSNetwork {

    /**
     * 返回操作系统上网卡集合
     *
     * @return
     */
    List<OSNetworkCard> getOSNetworkCards();

}
