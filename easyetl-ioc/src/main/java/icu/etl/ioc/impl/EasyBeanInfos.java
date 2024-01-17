package icu.etl.ioc.impl;

import icu.etl.annotation.EasyBean;
import icu.etl.ioc.EasyContext;
import icu.etl.util.ClassUtils;
import icu.etl.util.StrAsIntComparator;
import icu.etl.util.StrAsNumberComparator;
import icu.etl.util.StringComparator;

/**
 * 注册工具包、并发包、表达式包中的组件信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2024/1/16 14:41
 */
@EasyBean(singleton = true, lazy = false, description = "加载组件")
public class EasyBeanInfos {

    public EasyBeanInfos(EasyContext context) {
        EasyBeanInfoImpl bean1 = new EasyBeanInfoImpl(StrAsIntComparator.class);
        bean1.setName("int");
        bean1.setDescription("将字符串作为整数来比较");
        context.addBean(bean1);

        EasyBeanInfoImpl bean2 = new EasyBeanInfoImpl(StrAsNumberComparator.class);
        bean2.setName("number");
        bean2.setDescription("将字符串作为浮点数来比较");
        context.addBean(bean2);

        EasyBeanInfoImpl bean3 = new EasyBeanInfoImpl(StringComparator.class);
        bean3.setName("string");
        bean3.setDescription("字符串比较规则");
        context.addBean(bean3);

        // 加载线程池
        Class<Object> type1 = ClassUtils.forName("icu.etl.concurrent.ThreadSourceImpl");
        if (type1 != null) {
            EasyBeanInfoImpl bean = new EasyBeanInfoImpl(type1);
            bean.setSingleton(true);
            bean.setLazy(true);
            context.addBean(bean);
        }

        Class<Object> type2 = ClassUtils.forName("icu.etl.expression.AnalysisImpl");
        if (type2 != null) {
            EasyBeanInfoImpl bean = new EasyBeanInfoImpl(type2);
            context.addBean(bean);
        }
    }

}
