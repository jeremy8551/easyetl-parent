package icu.etl.ioc;

/**
 * 增加组件实现类 或 删除组件实现类的事件信息
 */
public class StandardBeanEvent implements BeanEvent {

    /** 实现类信息 */
    private BeanInfo beanInfo;

    /**
     * 组件变化事件
     *
     * @param beanInfo 组件信息
     */
    public StandardBeanEvent(BeanInfo beanInfo) {
        this.beanInfo = beanInfo;
    }

    public BeanInfo getBeanInfo() {
        return beanInfo;
    }

}
