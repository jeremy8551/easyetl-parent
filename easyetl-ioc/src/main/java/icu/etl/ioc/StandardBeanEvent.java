package icu.etl.ioc;

/**
 * 增加组件实现类 或 删除组件实现类的事件信息
 */
public class StandardBeanEvent implements BeanEvent {

    /** 实现类信息 */
    private BeanInfoRegister beanInfo;

    /** 容器上下文信息 */
    private EasyetlContext context;

    /**
     * 组件变化事件
     *
     * @param context  容器上下文信息
     * @param beanInfo 组件信息
     */
    public StandardBeanEvent(EasyetlContext context, BeanInfoRegister beanInfo) {
        this.context = context;
        this.beanInfo = beanInfo;
    }

    public EasyetlContext getContext() {
        return context;
    }

    public BeanInfoRegister getBeanInfo() {
        return beanInfo;
    }

}
