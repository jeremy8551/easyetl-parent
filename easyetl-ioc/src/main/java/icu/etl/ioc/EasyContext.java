package icu.etl.ioc;

/**
 * 容器上下文信息
 *
 * @author jeremy8551@qq.com
 */
public interface EasyContext extends EasyContainerContext, EasyBeanRegister, EasyBeanInfoContext, EasyBeanBuilderContext, EasyBeanFactory {

    /**
     * 设置容器启动参数
     *
     * @param args 启动参数数组
     */
    void setArgument(String... args);

    /**
     * 返回容器启动的参数
     *
     * @return 启动参数数组
     */
    String[] getArgument();

    /**
     * 返回上级容器
     *
     * @return 上级容器对象
     */
    EasyContext getParent();

    /**
     * 设置上级容器
     *
     * @param parent 上级容器
     */
    void setParent(EasyContext parent);

    /**
     * 设置类加载器
     *
     * @param classLoader 类加载器
     */
    void setClassLoader(ClassLoader classLoader);

    /**
     * 返回容器使用的类加载器
     *
     * @return 类加载器
     */
    ClassLoader getClassLoader();

    /**
     * 添加容器实例
     *
     * @param ioc 容器实例
     * @return 如果容器重名，则会替换掉重名容器，并返回替换掉的容器
     */
    EasyContainerContext addIoc(EasyContainerContext ioc);

    /**
     * 删除容器实例对象
     *
     * @param name 容器名
     * @return 被删除的容器
     */
    EasyContainerContext removeIoc(String name);

    /**
     * 刷新组件信息
     */
    void refresh();
    
}
