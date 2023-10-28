package icu.etl.ioc;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import icu.etl.annotation.EasyBean;
import icu.etl.util.ArrayUtils;
import icu.etl.util.StringUtils;

/**
 * 国家法定假日工厂类 <br>
 * 根据 Locale 参数返回对应的国家法定假日 <br>
 * 如果参数为空默认取虚拟机默认国家的法定假日
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-04-15
 */
@EasyBean
public class NationalHolidayBuilder implements BeanBuilder<NationalHoliday>, BeanEventListener {

    private Map<String, NationalHolidaySet> map;

    /** 容器上下文信息 */
    private EasyContext context;

    /**
     * 初始化
     */
    public NationalHolidayBuilder(EasyContext context) {
        this.context = context;
        this.map = new HashMap<String, NationalHolidaySet>();
        this.addAll(context);
    }

    public void addAll(EasyContext context) {
        List<BeanInfo> list = context.getBeanInfoList(NationalHoliday.class);
        for (BeanInfo beanInfo : list) {
            this.add(context, beanInfo);
        }
    }

    /**
     * 添加组件实现类
     *
     * @param context  容器上下文信息
     * @param beanInfo 组件信息
     */
    protected synchronized void add(EasyContext context, BeanInfo beanInfo) {
        if (NationalHoliday.class.isAssignableFrom(beanInfo.getType())) {
            String key = beanInfo.getName(); // zh, zh_CN, ch_CN_POSIX

            // 根据区域环境信息查询对应的集合
            NationalHolidaySet set = this.map.get(key);
            if (set == null) {
                set = new NationalHolidaySet();
                this.map.put(key, set);
            }

            // 在集合中添加法定假日
            NationalHoliday holiday = context.createBean(beanInfo.getType());
            set.add(holiday);
        }
    }

    public NationalHoliday getBean(EasyContext context, Object... args) throws Exception {
        // 使用当前默认国家语言信息
        if (args.length == 0) {
            return this.map.get(this.toKey(Locale.getDefault()));
        }

        // 查询指定国家语言信息
        Locale locale = ArrayUtils.indexOf(args, Locale.class, 0);
        if (locale != null) {
            return this.map.get(this.toKey(locale));
        }

        // 拼接字符串
        return this.map.get(StringUtils.join(args, "_"));
    }

    /**
     * 将字符串解析为地区信息
     *
     * @param locale 国家语言信息
     * @return 字符串，如: zh, zh_CN, ch_CN_POSIX
     */
    protected String toKey(Locale locale) {
        StringBuilder buf = new StringBuilder(15);
        buf.append(locale.getLanguage());
        if (StringUtils.isNotBlank(locale.getCountry())) {
            buf.append('_').append(locale.getCountry());
        }
        return buf.toString();
    }

    public void addBean(BeanEvent event) {
        BeanInfoRegister beanInfo = event.getBeanInfo();
        if (NationalHoliday.class.isAssignableFrom(beanInfo.getType())) {
            this.add(event.getContext(), beanInfo);
        }
    }

    public void removeBean(BeanEvent event) {
    }

}
