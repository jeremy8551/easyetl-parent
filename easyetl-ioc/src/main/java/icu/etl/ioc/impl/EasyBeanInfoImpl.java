package icu.etl.ioc.impl;

import icu.etl.annotation.EasyBean;
import icu.etl.ioc.EasyBeanInfo;
import icu.etl.ioc.EasyBeanInfoValue;
import icu.etl.util.ClassUtils;
import icu.etl.util.StringUtils;

/**
 * 接口实现类
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class EasyBeanInfoImpl implements EasyBeanInfoValue {

    /** 组件类 */
    protected Class<?> type;

    /** 组件管理的模式 */
    protected boolean singleton;

    /** 组件名 */
    protected String name;

    /** 排序优先级 */
    protected int priority;

    /** 是否延迟加载 */
    protected boolean lazy;

    /** 说明信息 */
    protected String description;

    /** 组件的实例对象 */
    protected Object instance;

    /**
     * 初始化
     *
     * @param type 组件类信息
     */
    public EasyBeanInfoImpl(Class<?> type) {
        if (type == null) {
            throw new NullPointerException();
        }

        this.type = type;
        this.instance = null;
        EasyBean annotation = type.getAnnotation(EasyBean.class); // 取得类上配置的注解
        if (annotation != null) {
            this.name = StringUtils.trimBlank(annotation.name());
            this.singleton = annotation.singleton();
            this.priority = annotation.priority();
            this.lazy = annotation.lazy();
            this.description = annotation.description();
        } else {
            this.name = "";
            this.singleton = false;
            this.priority = 0;
            this.lazy = true;
            this.description = "";
        }
    }

    /**
     * 初始化
     *
     * @param beanInfo 组件信息
     */
    public EasyBeanInfoImpl(EasyBeanInfo beanInfo) {
        if (beanInfo == null) {
            throw new NullPointerException();
        }

        this.instance = null;
        this.type = beanInfo.getType();
        this.name = beanInfo.getName();
        this.singleton = beanInfo.singleton();
        this.priority = beanInfo.getPriority();
        this.lazy = beanInfo.isLazy();
        this.description = beanInfo.getDescription();
    }

    @SuppressWarnings("unchecked")
    public <E> Class<E> getType() {
        return (Class<E>) this.type;
    }

    public String getName() {
        return this.name;
    }

    public boolean singleton() {
        return singleton;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isLazy() {
        return lazy;
    }

    public String getDescription() {
        return description;
    }

    public boolean equals(Class<?> cls) {
        return ClassUtils.equals(this.type, cls);
    }

    public boolean equals(String name) {
        return this.name.equalsIgnoreCase(name);
    }

    public boolean equals(EasyBeanInfo beanInfo) {
        return this.equals(beanInfo.getType());
    }

    @SuppressWarnings("unchecked")
    public <E> E getBean() {
        return (E) this.instance;
    }

    public void setBean(Object instance) {
        this.instance = instance;
    }

    public int compare(EasyBeanInfo o1, EasyBeanInfo o2) {
        int v = o1.getName().compareTo(o2.getName());
        if (v != 0) {
            return v;
        } else {
            return o1.getPriority() - o2.getPriority(); // 倒序排序
        }
    }
}
