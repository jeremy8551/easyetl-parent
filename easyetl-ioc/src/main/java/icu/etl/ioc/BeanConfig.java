package icu.etl.ioc;

import java.lang.annotation.Annotation;

import icu.etl.annotation.EasyBeanClass;

/**
 * 组件的配置信息
 *
 * @param <E> 组件类信息
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class BeanConfig<E> {

    /** 组件类 */
    private Class<E> type;

    /** 组件实现类 */
    private Class<E> impl;

    /** 组件实现类上的注解 */
    private Annotation annotation;

    /**
     * 初始化
     *
     * @param type       组件类
     * @param impl       组件实现类
     * @param annotation 组件实现类上的注解
     */
    public BeanConfig(Class<E> type, Class<E> impl, Annotation annotation) {
        if (type == null) {
            throw new NullPointerException();
        }
        if (impl == null) {
            throw new NullPointerException();
        }
        if (annotation == null) {
            throw new NullPointerException();
        }

        this.type = type;
        this.impl = impl;
        this.annotation = annotation;
    }

    /**
     * 组件类
     *
     * @return 类信息
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * 组件的实现类
     *
     * @return 类信息
     */
    public Class<E> getImplementClass() {
        return this.impl;
    }

    /**
     * 实现类上的注解信息
     *
     * @return 注解
     */
    public Annotation getAnnotation() {
        return this.annotation;
    }

    /**
     * 返回组件的实现类
     *
     * @return 组件实现类
     */
    public EasyBeanClass getAnnotationAsImplement() {
        if (this.annotation instanceof EasyBeanClass) {
            return (EasyBeanClass) this.annotation;
        } else {
            return null;
        }
    }

}
