package icu.etl.ioc;

/**
 * 组件变更（添加或删除实现类）的事件
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-04-15
 */
public interface EasyBeanEvent {

    /**
     * 返回容器上下文信息
     *
     * @return 容器上下文信息
     */
    EasyContext getContext();

    /**
     * 此时事件相关的实现类信息
     *
     * @return 组件实现类
     */
    EasyBeanInfoValue getBeanInfo();

}
