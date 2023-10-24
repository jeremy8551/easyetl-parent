package icu.etl.ioc;

import java.lang.annotation.Annotation;

/**
 * 组件变更（添加实现类或删除实现类）的事件
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-04-15
 */
public interface BeanEvent {

    /**
     * 返回组件工厂的上下文信息
     *
     * @return 上下文信息
     */
    EasyetlContext getContext();

    /**
     * 此时事件相关的实现类信息
     *
     * @param <E> 组件实现类
     * @return 组件实现类
     */
    <E> Class<E> getImplementClass();

    /**
     * 此次事件相关的实现类上的注解信息
     *
     * @return 组件或组件实现类上的注解
     */
    Annotation getAnnotation();

}
