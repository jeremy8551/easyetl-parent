package icu.etl.ioc;

public interface EasyetlContextAware {

    /**
     * 容器工厂实例化一个对象后，如果对象实现了这个接口，则自动调用接口注入上下文信息
     *
     * @param context 容器上下文信息
     */
    public void set(EasyetlContext context);

}
