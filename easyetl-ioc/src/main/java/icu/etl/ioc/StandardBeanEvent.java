package icu.etl.ioc;

import java.lang.annotation.Annotation;

/**
 * 增加组件实现类 或 删除组件实现类的事件信息
 */
public class StandardBeanEvent implements BeanEvent {

    /** 实现类信息 */
    private Class<?> cls;

    /** 实现类上的注解信息 */
    private Annotation anno;

    /** 组件工厂上下文信息 */
    private BeanContext context;

    /**
     * 组件变化事件
     *
     * @param context 上下文信息
     * @param cls     类信息
     * @param anno    注解
     */
    public StandardBeanEvent(BeanContext context, Class<?> cls, Annotation anno) {
        this.context = context;
        this.cls = cls;
        this.anno = anno;
    }

    @SuppressWarnings("unchecked")
    public Class<?> getImplementClass() {
        return cls;
    }

    public Annotation getAnnotation() {
        return anno;
    }

    public BeanContext getContext() {
        return context;
    }

}
