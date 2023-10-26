package icu.etl.ioc;

/**
 * 组件变更（添加实现类或删除实现类）的事件
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-04-15
 */
public interface BeanEvent {

    /**
     * 此时事件相关的实现类信息
     *
     * @param <E> 组件实现类
     * @return 组件实现类
     */
    <E> BeanInfo getBeanInfo();

}
