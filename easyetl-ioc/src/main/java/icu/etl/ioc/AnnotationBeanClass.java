package icu.etl.ioc;

import java.util.Objects;

import icu.etl.annotation.EasyBean;

/**
 * 组件的配置信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class AnnotationBeanClass implements BeanClass {

    /** 组件类 */
    private Class<?> cls;

    /** 组件类上的注解 */
    private EasyBean annotation;

    /** 组件管理的模式 */
    private boolean singleton;

    /**
     * 初始化
     *
     * @param cls 组件类信息
     */
    public AnnotationBeanClass(Class<?> cls) {
        if (cls == null) {
            throw new NullPointerException();
        }

        this.cls = cls;
        this.annotation = cls.getAnnotation(EasyBean.class); // 取得类上配置的注解
        if (this.annotation == null) {
            this.annotation = EmptyEasyBean.class.getAnnotation(EasyBean.class); // 设置一个空注解
            System.out.println(cls.getName() + " 上没有使用EasyBean注解，自动设置默认 " + this.annotation);
        }
        Objects.requireNonNull(this.annotation);
        this.singleton = this.annotation.singleton() || this.annotation.value();
    }

    public <E> Class<E> getBeanClass() {
        return (Class<E>) this.cls;
    }

    public EasyBean getAnnotation() {
        return this.annotation;
    }

    public boolean isSingleton() {
        return singleton;
    }
}
