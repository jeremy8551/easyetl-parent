package icu.etl.cn;

import java.util.Date;
import java.util.Set;

/**
 * 国家法定节假日
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-04-15
 */
public interface NationalHoliday {

    /**
     * 返回法定假日集合
     *
     * @return 日期集合
     */
    Set<Date> getRestDays();

    /**
     * 返回法定工作日集合
     *
     * @return 日期集合
     */
    Set<Date> getWorkDays();

    /**
     * 是否为休息日(周末和法定假日, 不包含法定补休日) <br>
     *
     * @param date 日期
     * @return 返回true表示日期是休息日 false表示日期是工作日
     */
    boolean isRestDay(Date date);

    /**
     * 是否为工作日(不包含周末和法定假日, 包含法定补休日)
     *
     * @param date 日期
     * @return 返回true表示日期是工作日 false表示日期是休息日
     */
    boolean isWorkDay(Date date);

}
