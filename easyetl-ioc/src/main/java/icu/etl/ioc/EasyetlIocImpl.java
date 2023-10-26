package icu.etl.ioc;

import java.util.Objects;

import icu.etl.util.ResourcesUtils;

public class EasyetlIocImpl implements EasyetlIoc {

    /** 上下文信息 */
    private EasyetlContext context;

    /**
     * 初始化
     *
     * @param context 容器上下文信息
     */
    public EasyetlIocImpl(EasyetlContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public String getName() {
        return EasyetlIoc.class.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    public <E> E getBean(Class<E> type, Object[] args) {
        // 优先使用接口工厂生成实例对象
        BeanBuilder<?> factory = this.context.getBeanBuilder(type);
        if (factory != null) {
            try {
                return (E) factory.getBean(this.context, args);
            } catch (Throwable e) {
                throw new RuntimeException(ResourcesUtils.getClassMessage(12, type.getName()), e);
            }
        }

        // 查询接口的实现类
        synchronized (this) {
            BeanArgument argument = new BeanArgument(args);
            BeanInfo beanInfo = this.context.getBeanInfo(type, argument.getName());
            if (beanInfo != null) {
                if (beanInfo.isSingleton() && beanInfo.getInstance() != null) {
                    return beanInfo.getInstance();
                }

                E obj = this.context.createBean(beanInfo.getType(), argument);
                if (beanInfo.isSingleton()) {
                    beanInfo.setInstance(obj);
                }
                return obj;
            }
        }

        return null;
    }

}
