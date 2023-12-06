package icu.etl.ioc.impl;

import icu.etl.ioc.EasyBeanEvent;
import icu.etl.ioc.EasyBeanInfoValue;
import icu.etl.ioc.EasyContext;

/**
 * 接口实现类
 */
public class EasyBeanEventImpl implements EasyBeanEvent {

    /** 实现类信息 */
    private EasyBeanInfoValue beanInfo;

    /** 容器上下文信息 */
    private EasyContext context;

    /**
     * 组件变化事件
     *
     * @param context  容器上下文信息
     * @param beanInfo 组件信息
     */
    public EasyBeanEventImpl(EasyContext context, EasyBeanInfoValue beanInfo) {
        this.context = context;
        this.beanInfo = beanInfo;
    }

    public EasyContext getContext() {
        return context;
    }

    public EasyBeanInfoValue getBeanInfo() {
        return beanInfo;
    }

}
