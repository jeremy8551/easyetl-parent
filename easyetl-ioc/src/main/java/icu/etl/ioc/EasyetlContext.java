package icu.etl.ioc;

import java.util.List;

import icu.etl.annotation.EasyBean;

/**
 * 容器上下文信息
 *
 * @author jeremy8551@qq.com
 */
public interface EasyetlContext extends BeanRegister {

    /**
     * 添加容器实例
     *
     * @param ioc 容器实例
     * @return 如果容器重名，则会替换掉重名容器，并返回替换掉的容器
     */
    EasyetlIoc addIoc(EasyetlIoc ioc);

    /**
     * 删除容器实例对象
     *
     * @param name 容器名
     * @return 被删除的容器
     */
    EasyetlIoc removeIoc(String name);

    /**
     * 返回容器使用的类加载器
     *
     * @return 类加载器
     */
    ClassLoader getClassLoader();

    /**
     * 返回容器启动的参数
     *
     * @return 启动参数数组
     */
    String[] getArgument();

    /**
     * 返回容器中存储的所有组件的查询条件（按组件添加的顺序）
     *
     * @return 组件类信息的集合（按组件添加的顺序）
     */
    List<Class<?>> getTypes();

    /**
     * 创建 {@code type} 的实例对象
     *
     * @param type
     * @param args
     * @param <E>
     * @return
     */
    <E> E createBean(Class<?> type, Object... args);

    /**
     * 查找并创建类或接口对应的组件对象
     *
     * @param <E>  类或接口
     * @param type 类或接口
     * @param args 查询参数, 如果存在多个实现类时，会根据参数数组 args 中的值判断使用那个实现类
     *             第一个参数对应组件类上的注解属性 {@link EasyBean#name()}
     * @return 组件实例
     */
    <E> E getBean(Class<E> type, Object... args);

    /**
     * 查询接口信息对应的实现类
     *
     * @param type 组件类信息
     * @param name 组件名
     * @return 组件类信息
     */
    BeanInfo getBeanInfo(Class<?> type, String name);

    /**
     * 查找类或接口对应的（所有）组件信息
     *
     * @param type 类或接口
     * @return 组件对应的所有实现类
     */
    List<BeanInfo> getBeanInfoList(Class<?> type);

    /**
     * 查找类信息对应的实现类集合
     *
     * @param type 组件类信息
     * @param name 组件名
     * @return 组件对应的所有实现类
     */
    List<BeanInfo> getBeanInfoList(Class<?> type, String name);

    /**
     * 判断组件 {@code type} 的实现类 {@code impl} 是否已注册
     *
     * @param type 组件信息
     * @param cls  实现类
     * @return true表示存在组件实现类
     */
    boolean containsBeanInfo(Class<?> type, Class<?> cls);

    /**
     * 删除接口信息对应的实现类集合
     *
     * @param type 组件类信息
     * @return 组件的实现类集合
     */
    List<BeanInfo> removeBeanInfoList(Class<?> type);

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
    BeanBuilder<?> getBeanBuilder(Class<?> type);

    /**
     * 删除接口信息对应的工厂
     *
     * @param type 组件类信息
     * @return 组件工厂实例
     */
    BeanBuilder<?> removeBeanBuilder(Class<?> type);

    /**
     * 注册组件工厂
     *
     * @param type    类或接口
     * @param builder 组件工厂
     * @return 返回true表示添加成功
     */
    boolean addBeanBuilder(Class<?> type, BeanBuilder<?> builder);

}
