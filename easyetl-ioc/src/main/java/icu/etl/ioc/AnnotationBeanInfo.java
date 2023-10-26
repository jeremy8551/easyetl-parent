package icu.etl.ioc;

import icu.etl.annotation.EasyBean;
import icu.etl.util.ClassUtils;
import icu.etl.util.StringUtils;

/**
 * 组件的配置信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class AnnotationBeanInfo implements BeanInfo {

    /** 组件类 */
    protected Class<?> type;

    /** 组件管理的模式 */
    protected boolean singleton;

    protected String name;

    protected int order;

    protected boolean lazy;

    protected String description;

    /** 组件单例对象 */
    protected Object instance;

    /**
     * 初始化
     *
     * @param type 组件类信息
     */
    public AnnotationBeanInfo(Class<?> type) {
        if (type == null) {
            throw new NullPointerException();
        }

        this.type = type;
        this.instance = null;

        EasyBean annotation = type.getAnnotation(EasyBean.class); // 取得类上配置的注解
        if (annotation != null) {
            this.singleton = annotation.singleton();
            this.name = StringUtils.trimBlank(annotation.name());
            this.order = annotation.level();
            this.lazy = annotation.lazy();
            this.description = annotation.description();
        } else {
            this.singleton = false;
            this.name = "";
            this.order = 0;
            this.lazy = true;
            this.description = "";
            System.out.println(type.getName() + " 上没有使用EasyBean注解，自动设置默认 " + annotation);
        }
    }

    @SuppressWarnings("unchecked")
    public <E> Class<E> getType() {
        return (Class<E>) this.type;
    }

    public String getName() {
        return this.name;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public int getOrder() {
        return order;
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

    @SuppressWarnings("unchecked")
    public <E> E getInstance() {
        return (E) instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }
}
