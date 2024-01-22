package icu.etl.ioc.impl;

import icu.etl.annotation.EasyBean;
import icu.etl.ioc.EasyBeanInfo;
import icu.etl.ioc.EasyBeanInfoValue;
import icu.etl.util.ClassUtils;
import icu.etl.util.Ensure;
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
        this.type = Ensure.notNull(type);
        EasyBean annotation = type.getAnnotation(EasyBean.class); // 取得类上配置的注解
        if (annotation != null) {
            this.setName(StringUtils.trimBlank(annotation.name()));
            this.setSingleton(annotation.singleton());
            this.setPriority(annotation.priority());
            this.setLazy(annotation.lazy());
            this.setDescription(annotation.description());
        } else {
            this.setName("");
            this.setSingleton(false);
            this.setPriority(0);
            this.setLazy(true);
            this.setDescription("");
        }
        this.setBean(null);
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

    /**
     * 设置是否使用单例模式
     *
     * @param singleton true表示使用单例模式 false表示原型模式
     */
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    /**
     * 设置组件名
     *
     * @param name 组件名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 设置权重，从0开始权重值逐增高
     *
     * @param priority 权重
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * 设置是否延迟加载
     *
     * @param lazy true表示延迟加载 false表示容器启动时加载
     */
    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    /**
     * 设置组件说明信息
     *
     * @param description 说明信息
     */
    public void setDescription(String description) {
        this.description = description;
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
