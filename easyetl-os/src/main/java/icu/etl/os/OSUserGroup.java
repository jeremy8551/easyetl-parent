package icu.etl.os;

import java.util.List;

/**
 * 接口操作系统的用户组信息 <br>
 * 操作系统可以是本地操作系统，也可以是远程linux，windows，unix，macos
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OSUserGroup {

    /**
     * 操作系统用户组名
     *
     * @return
     */
    String getName();

    /**
     * 返回组编号
     *
     * @return
     */
    String getGid();

    /**
     * 返回归属组的所有用户名集合
     *
     * @return
     */
    List<String> getUsers();

}