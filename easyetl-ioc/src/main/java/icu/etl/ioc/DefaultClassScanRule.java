package icu.etl.ioc;

import java.util.Comparator;

import icu.etl.annotation.EasyBean;
import icu.etl.util.StringComparator;

/**
 * 类扫描器的扫描规则
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class DefaultClassScanRule implements ClassScanRule, Comparator<BeanConfig> {

    /**
     * 初始化，扫描类路径中所有被注解标记的类信息
     */
    public DefaultClassScanRule() {
    }

    public boolean process(Class<?> cls, BeanRegister register) {
        if (cls == null) {
            return false;
        }

        // 如果类上配置了注解
        if (cls.isAnnotationPresent(EasyBean.class)) {
            EasyBean anno = cls.getAnnotation(EasyBean.class);
            register.add(new BeanConfig(cls, anno), this);
            return true;
        }
        return false;
    }

    public int compare(BeanConfig o1, BeanConfig o2) {
        EasyBean b1 = o1.getAnnotationAsImplement();
        EasyBean b2 = o2.getAnnotationAsImplement();

        if (StringComparator.compareTo(b2.kind(), b1.kind()) == 0 //
                && StringComparator.compareTo(b2.mode(), b1.mode()) == 0 //
                && StringComparator.compareTo(b2.major(), b1.major()) == 0 //
                && StringComparator.compareTo(b2.minor(), b1.minor()) == 0 //
                && o1.getBeanClass().equals(o2.getBeanClass()) //
        ) {
            return 0;
        } else {
            return 1;
        }
    }

    public boolean equals(Object obj) {
        return obj != null && DefaultClassScanRule.class.equals(obj.getClass());
    }

}
