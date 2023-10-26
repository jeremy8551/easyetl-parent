package icu.etl.ioc;

import icu.etl.annotation.EasyBean;

public interface BeanInfo {

    /**
     * 组件类信息
     *
     * @return 类信息
     */
    <E> Class<E> getType();

    /**
     * 组件名
     *
     * @return 组件名
     */
    String getName();

    /**
     * 组件管理模式
     *
     * @return 详见 {@linkplain EasyBean#singleton()}
     */
    boolean isSingleton();

    /**
     * 排序权重
     *
     * @return 详见 {@linkplain EasyBean#level()}
     */
    int getOrder();

    boolean isLazy();

    String getDescription();

    /**
     * 判断参数 {@code cls} 是否与组件的类信息相等
     *
     * @param cls 类信息
     * @return 返回true表示相等
     */
    boolean equals(Class<?> cls);

    /**
     * 判断组件名是否等于参数 {@code name}
     *
     * @param name 组件名
     * @return 返回true表示相等
     */
    boolean equals(String name);

    /**
     * 返回单例对象
     *
     * @param <E> 类信息
     * @return 单例对象
     */
    <E> E getInstance();

    /**
     * 保存单例对象
     *
     * @param instance 单例对象
     */
    void setInstance(Object instance);

}
