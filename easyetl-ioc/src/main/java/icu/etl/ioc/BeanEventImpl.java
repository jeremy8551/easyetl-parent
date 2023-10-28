package icu.etl.ioc;

/**
 * 增加组件实现类 或 删除组件实现类的事件信息
 */
public class BeanEventImpl implements BeanEvent {

    /** 实现类信息 */
    private BeanInfoRegister beanInfo;

    /** 容器上下文信息 */
    private EasyContext context;

    /**
     * 组件变化事件
     *
     * @param context  容器上下文信息
     * @param beanInfo 组件信息
     */
    public BeanEventImpl(EasyContext context, BeanInfoRegister beanInfo) {
        this.context = context;
        this.beanInfo = beanInfo;
    }

    public EasyContext getContext() {
        return context;
    }

    public BeanInfoRegister getBeanInfo() {
        return beanInfo;
    }

}
