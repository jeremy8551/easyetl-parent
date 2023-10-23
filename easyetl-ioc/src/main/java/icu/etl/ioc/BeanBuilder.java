package icu.etl.ioc;

/**
 * 组件的工厂
 *
 * @param <E> 组件的类信息
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public interface BeanBuilder<E> {

    /**
     * 生成一个实例对象
     *
     * @param context 对象工厂的上下文信息
     * @param array   创建实例对象的参数
     * @return 实例对象
     * @throws Exception 生成实例对象发生错误
     */
    E build(EasyetlContext context, Object... array) throws Exception;

}
