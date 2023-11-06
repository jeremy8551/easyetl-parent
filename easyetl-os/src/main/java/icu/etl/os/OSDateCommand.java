package icu.etl.os;

import java.util.Date;

/**
 * 此接口用于描述操作系统的日期时间功能。 <br>
 * 操作系统可以是本地操作系统，也可以是远程linux，windows，unix，macos
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-09-06
 */
public interface OSDateCommand {

    /**
     * 返回操作系统当前日期与时间
     *
     * @return 日期时间对象
     */
    Date getDate();

    /**
     * 修改操作系统日期时间
     *
     * @param date 日期时间对象
     * @return 返回true表示设置成功
     */
    boolean setDate(Date date);

}
