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
public class EasyBeanInfo implements BeanInfoRegister {

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
    public EasyBeanInfo(Class<?> type) {
        if (type == null) {
            throw new NullPointerException();
        }

        this.type = type;
        this.instance = null;
        EasyBean annotation = type.getAnnotation(EasyBean.class); // 取得类上配置的注解
        if (annotation != null) {
            this.name = StringUtils.trimBlank(annotation.name());
            this.singleton = annotation.singleton();
            this.order = annotation.level();
            this.lazy = annotation.lazy();
            this.description = annotation.description();
        } else {
            this.name = "";
            this.singleton = false;
            this.order = 0;
            this.lazy = true;
            this.description = "";
        }
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
    public <E> E getBean() {
        return (E) instance;
    }

    public void setBean(Object instance) {
        this.instance = instance;
    }

    public int compare(BeanInfo o1, BeanInfo o2) {
        int tc = o1.getType().getName().compareTo(o2.getType().getName());
        if (tc != 0) {
            return tc;
        }

        int nc = o1.getName().compareTo(o2.getName());
        if (nc != 0) {
            return nc;
        }

        return o2.getPriority() - o1.getPriority(); // 倒序排序
    }
}
