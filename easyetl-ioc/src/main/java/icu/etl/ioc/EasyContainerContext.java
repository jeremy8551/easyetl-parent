package icu.etl.ioc;

import icu.etl.annotation.EasyBean;

/**
 * IOC容器的上下文接口
 */
public interface EasyContainerContext {

    /**
     * 容器名，必须唯一
     *
     * @return 容器名
     */
    String getName();

    /**
     * 查找并创建类或接口对应的组件
     *
     * @param <E>  类或接口
     * @param type 查询条件，类或接口
     * @param args 查询参数
     *             第一个参数，对应组件上的注解属性 {@link EasyBean#name()}
     * @return 组件
     */
    <E> E getBean(Class<E> type, Object... args);
}
