package icu.etl.ioc;

/**
 * 组件实现类变化的监听器
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-04-15
 */
public interface BeanEventListener {

    /**
     * 添加组件实现类的监听接口
     *
     * @param event 事件
     */
    void addBean(BeanEvent event);

    /**
     * 删除组件实现类的监听接口
     *
     * @param event 事件
     */
    void removeBean(BeanEvent event);

}
