package icu.etl.ioc;

import java.lang.annotation.Annotation;

import icu.etl.annotation.EasyBean;

/**
 * 组件的配置信息
 *
 * @param <E> 组件类信息
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class BeanConfig<E> {

    /** 组件类 */
    private Class<E> cls;

    /** 组件类上的注解 */
    private Annotation annotation;

    /**
     * 初始化
     *
     * @param cls        组件实现类
     * @param annotation 组件实现类上的注解
     */
    public BeanConfig(Class<E> cls, Annotation annotation) {
        if (cls == null) {
            throw new NullPointerException();
        }
        if (annotation == null) {
            throw new NullPointerException();
        }

        this.cls = cls;
        this.annotation = annotation;
    }

    /**
     * 组件的实现类
     *
     * @return 类信息
     */
    public Class<E> getBeanClass() {
        return this.cls;
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
    public EasyBean getAnnotationAsImplement() {
        if (this.annotation instanceof EasyBean) {
            return (EasyBean) this.annotation;
        } else {
            return null;
        }
    }

}
