package icu.etl.ioc;

/**
 * 某个组件的工厂接口
 *
 * @param <E> 组件的类信息
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public interface EasyBeanBuilder<E> {

    /**
     * 生成一个组件的实例对象
     *
     * @param context 容器的上下文信息
     * @param args    参数数组
     * @return 实例对象
     * @throws Exception 生成实例对象发生错误
     */
    E getBean(EasyContext context, Object... args) throws Exception;

}
