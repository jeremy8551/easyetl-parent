package icu.etl.ioc;

import java.util.List;
import java.util.Set;

import icu.etl.annotation.EasyBean;

public interface EasyetlContext extends BeanRegister {

    <E> E get(String key);

    <E> E put(String key, Object value);

    /**
     * 创建指定类型的实例对象
     *
     * @param <E>   类型
     * @param clazz 接口信息 或 模版类信息
     * @param array 如果存在多个实现类时，会根据参数数组 array 中的值判断使用那个实现类 <br>
     *              第一个参数对应 {@link EasyBean#kind()} 属性，表示一级分类 <br>
     *              第二个参数对应 {@link EasyBean#mode()} 属性，表示一级分类下的子分类信息 <br>
     *              第三个参数对应 {@link EasyBean#major()} 属性，表示大版本号，可以为空 <br>
     *              第四个参数对应 {@link EasyBean#minor()} 属性，表示小版本号，可以为空 <br>
     * @return 实例对象
     */
    <E> E get(Class<E> clazz, Object... array);

    /**
     * 返回类加载器
     *
     * @return 类加载器
     */
    ClassLoader getClassLoader();

    /**
     * 返回启动参数
     *
     * @return 启动参数数组
     */
    String[] getArgument();

    /**
     * 返回所有组件工厂的类信息（按组件添加的顺序）
     *
     * @return 返回组件工程集合
     */
    Set<Class<?>> getBuilderClass();

    /**
     * 查询接口信息对应的工厂
     *
     * @param type 接口信息
     * @return 组件工厂的类信息
     */
    BeanBuilder<?> getBuilder(Class<?> type);

    /**
     * 删除接口信息对应的工厂
     *
     * @param type 接口信息
     * @return 组件工厂实例
     */
    BeanBuilder<?> removeBuilder(Class<?> type);

    /**
     * 判断接口的实现类是否存在
     *
     * @param type 接口信息
     * @param impl 接口的实现信息
     * @return true表示存在组件实现类
     */
    boolean containsImplement(Class<?> type, Class<?> impl);

    /**
     * 删除接口信息对应的实现类集合
     *
     * @param type 组件信息
     * @return 组件实现类的集合
     */
    List<BeanConfig> removeImplement(Class<?> type);

    /**
     * 返回所有组件类信息（按组件添加的顺序）
     *
     * @return 组件种类的集合
     */
    Set<Class<?>> getImplements();

    /**
     * 查找类信息对应的实现类集合
     *
     * @param type 类信息
     * @return 组件对应的所有实现类
     */
    List<BeanConfig> getImplements(Class<?> type);

    /**
     * 查询接口信息对应的实现类
     *
     * @param type 接口信息
     * @param args 查询参数 <br>
     *             数组中第一个字符串对应 {@linkplain EasyBean#kind()} <br>
     *             数组中第二个字符串对应 {@linkplain EasyBean#mode()} <br>
     *             数组中第三个字符串对应 {@linkplain EasyBean#major()} <br>
     *             数组中第四个字符串对应 {@linkplain EasyBean#minor()} <br>
     * @param <E>  组件类信息
     * @return 组件的实现类
     */
    <E> Class<E> getImplement(Class<E> type, Object... args);

}
