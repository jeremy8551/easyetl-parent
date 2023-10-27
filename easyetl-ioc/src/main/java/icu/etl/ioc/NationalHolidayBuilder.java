package icu.etl.ioc;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import icu.etl.annotation.EasyBean;
import icu.etl.util.ArrayUtils;
import icu.etl.util.ClassUtils;
import icu.etl.util.Dates;

/**
 * 国家法定假日工厂类 <br>
 * 根据 Locale 参数返回对应的国家法定假日 <br>
 * 如果参数为空默认取虚拟机默认国家的法定假日
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-04-15
 */
@EasyBean
public class NationalHolidayBuilder implements BeanBuilder<NationalHoliday>, NationalHoliday, BeanEventListener {

    /** 工作日 */
    private Set<Date> work;

    /** 休息日 */
    private Set<Date> rest;

    /** 默认值 */
    private Locale locale;

    /** 初始化标志 */
    private AtomicBoolean init;

    /**
     * 初始化
     */
    public NationalHolidayBuilder() {
        this(Locale.getDefault());
    }

    /**
     * 初始化
     *
     * @param locale
     */
    public NationalHolidayBuilder(Locale locale) {
        if (locale == null) {
            throw new NullPointerException();
        }

        this.work = new LinkedHashSet<Date>();
        this.rest = new LinkedHashSet<Date>();
        this.init = new AtomicBoolean(false);
        this.locale = locale;
    }

    public NationalHoliday getBean(EasyetlContext context, Object... args) throws Exception {
        Locale locale = ArrayUtils.indexOf(args, Locale.class, 0);
        if (locale == null || locale.equals(this.locale)) { // 返回默认值
            if (!this.init.getAndSet(true)) {
                this.load(context);
            }
            return this;
        } else {
            NationalHolidayBuilder obj = new NationalHolidayBuilder(locale);
            obj.load(context);
            return obj;
        }
    }

    /**
     * 查找符合条件的国家法定假日类信息
     *
     * @param context 容器上下文信息
     */
    protected void load(EasyetlContext context) {
        List<BeanInfo> list = context.getBeanInfoList(NationalHoliday.class);
        for (BeanInfo beanInfo : list) { // 判断语言和国家信息是否相等
            this.add(beanInfo);
        }
    }

    /**
     * 添加组件实现类
     *
     * @param beanInfo 组件信息
     */
    protected void add(BeanInfo beanInfo) {
        if (beanInfo.equals(this.locale.getLanguage() + "_" + this.locale.getCountry())) {
            if (Ioc.out.isDebugEnabled()) {
                Ioc.out.debug("use " + beanInfo.getType().getName());
            }

            try {
                NationalHoliday obj = ClassUtils.newInstance(beanInfo.getType());
                this.work.addAll(obj.getWorkDays());
                this.rest.addAll(obj.getRestDays());
            } catch (Throwable e) {
                Ioc.out.warn("load " + beanInfo.getType().getName() + " error!", e);
            }
        }
    }

    public Set<Date> getRestDays() {
        return this.rest;
    }

    public Set<Date> getWorkDays() {
        return this.work;
    }

    public boolean isRestDay(Date date) {
        if (date == null) {
            return false;
        }

        if (this.getWorkDays().contains(date)) {
            return false;
        } else if (this.getRestDays().contains(date)) {
            return true;
        } else {
            return Dates.isWeekend(date);
        }
    }

    public boolean isWorkDay(Date date) {
        if (date == null) {
            return false;
        }

        if (this.getWorkDays().contains(date)) {
            return true;
        } else if (this.getRestDays().contains(date)) {
            return false;
        } else {
            return !Dates.isWeekend(date);
        }
    }

    public void addBean(BeanEvent event) {
        BeanInfoRegister beanInfo = event.getBeanInfo();
        if (NationalHoliday.class.isAssignableFrom(beanInfo.getType())) {
            this.add(beanInfo);
        }
    }

    public void removeBean(BeanEvent event) {
    }

}
