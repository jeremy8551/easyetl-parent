package icu.etl.ioc;

/**
 * 容器上下文信息
 *
 * @author jeremy8551@qq.com
 */
public interface EasyContext extends BeanContext, BeanRegister, BeanInfoContext, BeanBuilderContext, BeanFactory {

    /**
     * 添加容器实例
     *
     * @param ioc 容器实例
     * @return 如果容器重名，则会替换掉重名容器，并返回替换掉的容器
     */
    IocContext addIoc(IocContext ioc);

    /**
     * 删除容器实例对象
     *
     * @param name 容器名
     * @return 被删除的容器
     */
    IocContext removeIoc(String name);

    /**
     * 返回容器使用的类加载器
     *
     * @return 类加载器
     */
    ClassLoader getClassLoader();

    /**
     * 返回容器启动的参数
     *
     * @return 启动参数数组
     */
    String[] getArgument();

}
