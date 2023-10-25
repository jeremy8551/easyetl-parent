package icu.etl.ioc;

import java.util.Date;
import java.util.Set;

import icu.etl.annotation.EasyBean;

/**
 * 国家法定节假日
 * <p>
 * 实现类注解的填写规则:
 * {@linkplain EasyBean#kind()} 属性表示语言, 如: zh
 * {@linkplain EasyBean#mode()} 属性表示国家, 如: cn
 * {@linkplain EasyBean#major()} 属性未使用, 填空字符串
 * {@linkplain EasyBean#minor()} 属性未使用, 填空字符串
 * {@linkplain EasyBean#description()} 属性表示描述信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-04-15
 */
public interface NationalHoliday {

    /**
     * 返回法定假日集合
     *
     * @return
     */
    Set<Date> getRestDays();

    /**
     * 返回法定工作日集合
     *
     * @return
     */
    Set<Date> getWorkDays();

    /**
     * 是否为休息日(周末和法定假日, 不包含法定补休日) <br>
     *
     * @param date 日期
     * @return
     */
    boolean isRestDay(Date date);

    /**
     * 是否为工作日(不包含周末和法定假日, 包含法定补休日)
     *
     * @param date 日期
     * @return
     */
    boolean isWorkDay(Date date);

}
