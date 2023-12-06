package icu.etl.ioc;

import java.util.List;

/**
 * 组件工厂的上下文信息
 */
public interface EasyBeanBuilderContext {

    /**
     * 返回所有组件工厂的类信息（按组件添加的顺序）
     *
     * @return 返回组件工程集合
     */
    List<Class<?>> getBeanBuilderType();

    /**
     * 查询接口信息对应的工厂
     *
     * @param type 组件类信息
     * @return 组件工厂的类信息
     */
    EasyBeanBuilder<?> getBeanBuilder(Class<?> type);

    /**
     * 删除接口信息对应的工厂
     *
     * @param type 组件类信息
     * @return 组件工厂实例
     */
    EasyBeanBuilder<?> removeBeanBuilder(Class<?> type);

    /**
     * 注册组件工厂
     *
     * @param type    类或接口
     * @param builder 组件工厂
     * @return 返回true表示添加成功
     */
    boolean addBeanBuilder(Class<?> type, EasyBeanBuilder<?> builder);

}
