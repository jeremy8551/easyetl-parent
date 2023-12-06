package icu.etl.ioc;

/**
 * 组件实例接口
 */
public interface EasyBeanValue {

    /**
     * 返回实例对象
     *
     * @param <E> 类信息
     * @return 单例对象
     */
    <E> E getBean();

    /**
     * 保存实例对象
     *
     * @param bean 实例对象
     */
    void setBean(Object bean);

}
