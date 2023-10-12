package icu.etl.ioc;

import java.util.Comparator;

import icu.etl.annotation.EasyBean;
import icu.etl.annotation.EasyBeanClass;
import icu.etl.log.STD;
import icu.etl.util.ClassUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringComparator;

/**
 * 类扫描器的扫描规则
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class DefaultClassScanRule implements ClassScanRule {

    /** 用于判断组件实现是否相等 */
    private Comparator<BeanConfig> comparator = new Comparator<BeanConfig>() {
        public int compare(BeanConfig o1, BeanConfig o2) {
            EasyBeanClass b1 = o1.getAnnotationAsImplement();
            EasyBeanClass b2 = o2.getAnnotationAsImplement();

            if (StringComparator.compareTo(b2.kind(), b1.kind()) == 0 //
                    && StringComparator.compareTo(b2.mode(), b1.mode()) == 0 //
                    && StringComparator.compareTo(b2.major(), b1.major()) == 0 //
                    && StringComparator.compareTo(b2.minor(), b1.minor()) == 0 //
                    && o1.getImplementClass().equals(o2.getImplementClass()) //
            ) {
                return 0;
            } else {
                return 1;
            }
        }
    };

    /**
     * 初始化，扫描类路径中所有被注解标记的类信息
     */
    public DefaultClassScanRule() {
    }

    public boolean process(Class<?> cls, BeanRegister register) {
        if (cls == null) {
            return false;
        }

        boolean success = false;

        // 添加接口工厂类
        if (cls.isAnnotationPresent(EasyBean.class)) {
            EasyBean anno = cls.getAnnotation(EasyBean.class);
            try {
                BeanBuilder<?> builder = ClassUtils.newInstance(anno.builder());
                BeanContext context = (BeanContext) register;

                if (context.addBuilder(cls, builder)) {
                    success = true;
                    if (STD.out.isDebugEnabled()) {
                        STD.out.debug(ResourcesUtils.getClassMessage(19, cls.getName(), builder.getClass().getName()));
                    }
                }
            } catch (Throwable e) {
                if (STD.out.isWarnEnabled()) {
                    STD.out.warn(cls.getName(), e);
                }
            }
        }

        // 添加接口实现类
        if (cls.isAnnotationPresent(EasyBeanClass.class)) {
            EasyBeanClass anno = cls.getAnnotation(EasyBeanClass.class);
            if (STD.out.isDebugEnabled()) {
                STD.out.debug(ResourcesUtils.getClassMessage(21, anno.type().getName(), cls.getName()));
            }
            register.add(new BeanConfig(anno.type(), cls, anno), this.comparator);
            success = true;
        }

        return success;
    }

    public boolean equals(Object obj) {
        return obj != null && DefaultClassScanRule.class.equals(obj.getClass());
    }

}
