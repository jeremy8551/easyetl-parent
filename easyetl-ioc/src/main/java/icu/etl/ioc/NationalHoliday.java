package icu.etl.ioc;

import java.util.Date;
import java.util.Set;

import icu.etl.annotation.EasyBean;
import icu.etl.annotation.EasyBeanClass;

/**
 * 国家法定节假日
 * <p>
 * 实现类注解的填写规则:
 * {@linkplain EasyBeanClass#kind()} 属性表示语言, 如: zh
 * {@linkplain EasyBeanClass#mode()} 属性表示国家, 如: cn
 * {@linkplain EasyBeanClass#major()} 属性未使用, 填空字符串
 * {@linkplain EasyBeanClass#minor()} 属性未使用, 填空字符串
 * {@linkplain EasyBeanClass#description()} 属性表示描述信息
 * {@linkplain EasyBeanClass#type()} 属性必须填写 {@linkplain NationalHoliday}.class
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-04-15
 */
@EasyBean(builder = NationalHolidayBuilder.class)
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

}