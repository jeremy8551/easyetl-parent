package icu.etl.ioc;

import java.util.Comparator;

import icu.etl.annotation.EasyBean;

public interface BeanInfo extends Comparator<BeanInfo> {

    /**
     * 组件类
     *
     * @return 类信息
     */
    <E> Class<E> getType();

    /**
     * 组件名
     * <p>
     * 组件类信息相同时，使用组件名区分不通组件
     *
     * @return 组件名
     */
    String getName();

    /**
     * 组件管理模式
     *
     * @return 详见 {@linkplain EasyBean#singleton()}
     */
    boolean singleton();

    /**
     * 权重，从0开始权重值逐增高
     * <p>
     * 组件重名时，优先使用权重高的组件
     *
     * @return 详见 {@linkplain EasyBean#level()}
     */
    int getPriority();

    /**
     * 描述组件是否需要延迟加载
     *
     * @return 返回true表示延迟加载（使用时初始化一个实例对象），返回false表示容器启动先初始化一个实例对象，并注册到容器中
     */
    boolean isLazy();

    /**
     * 组件说明信息
     *
     * @return 组件说明信息
     */
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

}
