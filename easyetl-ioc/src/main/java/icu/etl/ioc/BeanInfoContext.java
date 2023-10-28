package icu.etl.ioc;

import java.util.List;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/28
 */
public interface BeanInfoContext {

    /**
     * 返回容器中存储的所有组件的查询条件（按组件添加的顺序）
     *
     * @return 组件类信息的集合（按组件添加的顺序）
     */
    List<Class<?>> getBeanInfoTypes();

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

}
