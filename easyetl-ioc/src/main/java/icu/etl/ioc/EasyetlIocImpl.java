package icu.etl.ioc;

import java.util.Objects;

import icu.etl.util.ResourcesUtils;

public class EasyetlIocImpl implements EasyetlIoc {

    /** 上下文信息 */
    private AnnotationEasyetlContext context;

    /**
     * 初始化
     *
     * @param context 容器上下文信息
     */
    public EasyetlIocImpl(AnnotationEasyetlContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public String getName() {
        return EasyetlIoc.class.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    public <E> E getBean(Class<?> type, Object[] args) {
        // 优先使用接口工厂生成实例对象
        BeanBuilder<?> factory = this.context.getBeanBuilder(type);
        if (factory != null) {
            try {
                return (E) factory.getBean(this.context, args);
            } catch (Throwable e) {
                throw new RuntimeException(ResourcesUtils.getClassMessage(12, type.getName()), e);
            }
        }

        // 按组件类与组件名查询
        BeanArgument argument = new BeanArgument(args);
        BeanInfoRegister beanInfo = this.context.getBeanInfo(type, argument.getName());
        if (beanInfo == null) {
            // 尝试创建类的实例对象
            try {
                return this.context.createBean(type, args);
            } catch (Throwable e) {
                return null;
            }
        }

        // 防止多线程同时访问同一个组件信息
        synchronized (beanInfo) {
            // 单例模式
            if (beanInfo.singleton()) {
                if (beanInfo.getBean() == null) {
                    beanInfo.setBean(this.context.createBean(beanInfo.getType(), argument.getArgs()));
                }
                return beanInfo.getBean();
            }

            // 原型模式
            if (beanInfo.getBean() != null) {
                E bean = beanInfo.getBean();
                beanInfo.setBean(null); // 原型模式需要删除存储的实例对象，防止被重复使用
                return bean;
            }
        }

        return this.context.createBean(beanInfo.getType(), argument.getArgs());
    }

}
