package icu.etl.ioc;

/**
 * 容器接口
 */
public interface EasyetlIoc {

    String getName();

    /**
     * 返回组件的实例对象
     *
     * @param <E>  组件（类或接口）
     * @param cls  组件（类或接口）
     * @param args 查询参数
     * @return 组件实例对象
     */
    <E> E getBean(Class<E> cls, Object[] args);

}