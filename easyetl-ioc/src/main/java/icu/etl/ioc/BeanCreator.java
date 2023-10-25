package icu.etl.ioc;

public interface BeanCreator {

    /**
     * 如果第一个参数是接口类型则根据参数数组中的信息自动选择对应的实现类 <br>
     * 如果第一个参数是非接口则默认使用类模版生成对象
     *
     * @param <E>  组件类型
     * @param cls  类模版信息
     * @param args 查询参数
     * @return 组件实例对象
     */
    <E> E getBean(Class<E> cls, Object... args);

}