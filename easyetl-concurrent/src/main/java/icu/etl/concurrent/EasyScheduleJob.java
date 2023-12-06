package icu.etl.concurrent;

import java.util.Date;

/**
 * 定时任务
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/30
 */
public interface EasyScheduleJob extends EasyJob {

    /**
     * 返回任务超时时间
     *
     * @return
     */
    long getTimeout();

    /**
     * 返回启动时间
     *
     * @return 时间
     */
    Date getStartTime();

    /**
     * 返回延迟执行时间
     *
     * @return 毫秒数
     */
    long getDelay();

    /**
     * 返回任务运行周期
     *
     * @return 毫秒数
     */
    long getPeriod();
}
