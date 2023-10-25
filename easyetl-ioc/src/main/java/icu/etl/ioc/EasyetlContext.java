package icu.etl.ioc;

import java.util.List;
import java.util.Set;

import icu.etl.annotation.EasyBean;

/**
 * 容器接口
 *
 * @author jeremy8551@qq.com
 */
public interface EasyetlContext extends BeanRegister {

    /**
     * 查询属性值的类信息与参数 {@code cls} 匹配的所有属性值
     *
     * @param cls 要查找的类信息（类或接口）
     * @param <E> 类信息
     * @return 属性值集合
     */
    <E> List<E> getAttribute(Class<E> cls);

    /**
     * 创建指定类型的实例对象
     *
     * @param <E>  组件类
     * @param type 组件类信息
     * @param args 查询参数, 如果存在多个实现类时，会根据参数数组 args 中的值判断使用那个实现类
     *             第一个参数对应 {@link EasyBean#kind()} 属性，表示一级分类
     *             第二个参数对应 {@link EasyBean#mode()} 属性，表示一级分类下的子分类信息
     *             第三个参数对应 {@link EasyBean#major()} 属性，表示大版本号，可以为空
     *             第四个参数对应 {@link EasyBean#minor()} 属性，表示小版本号，可以为空
     * @return 组件类的实例对象
     */
    <E> E getBean(Class<E> type, Object... args);

    /**
     * 返回所有组件工厂的类信息（按组件添加的顺序）
     *
     * @return 返回组件工程集合
     */
    Set<Class<?>> getBeanBuilderClass();

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
     * 查询接口信息对应的实现类
     *
     * @param <E>  组件类
     * @param type 组件类信息
     * @param args 查询参数, 如果存在多个实现类时，会根据参数数组 args 中的值判断使用那个实现类
     *             第一个参数对应 {@link EasyBean#kind()} 属性，表示一级分类
     *             第二个参数对应 {@link EasyBean#mode()} 属性，表示一级分类下的子分类信息
     *             第三个参数对应 {@link EasyBean#major()} 属性，表示大版本号，可以为空
     *             第四个参数对应 {@link EasyBean#minor()} 属性，表示小版本号，可以为空
     * @param <E>  组件类信息
     * @return 组件类信息
     */
    <E> Class<E> getBeanClass(Class<E> type, Object... args);

    /**
     * 返回容器中存储的所有组件类信息（按组件添加的顺序）
     *
     * @return 组件类信息的集合（按组件添加的顺序）
     */
    Set<Class<?>> getBeanClasses();

    /**
     * 查找类信息对应的实现类集合
     *
     * @param type 组件类信息
     * @return 组件对应的所有实现类
     */
    List<BeanClass> getBeanClassList(Class<?> type);

    /**
     * 判断组件 {@code type} 的实现类 {@code impl} 是否已注册
     *
     * @param type 组件类信息
     * @param impl 组件的实现类
     * @return true表示存在组件实现类
     */
    boolean containsBeanClass(Class<?> type, Class<?> impl);

    /**
     * 删除接口信息对应的实现类集合
     *
     * @param type 组件类信息
     * @return 组件的实现类集合
     */
    List<BeanClass> removeBeanClass(Class<?> type);

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

}
