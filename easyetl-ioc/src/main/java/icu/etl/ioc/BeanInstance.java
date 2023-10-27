package icu.etl.ioc;

public interface BeanInstance {

    /**
     * 返回单例对象
     *
     * @param <E> 类信息
     * @return 单例对象
     */
    <E> E getBean();

    /**
     * 保存单例对象
     *
     * @param bean 单例对象
     */
    void setBean(Object bean);

}
