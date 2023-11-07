package icu.etl.ioc;

/**
 * 组件的注册接口
 */
public interface BeanRegister {

    /**
     * 注册组件
     *
     * @param bean 组件的类信息
     * @return 返回true表示注册成功 false表示注册失败（未添加组件）
     */
    boolean addBean(Class<?> bean);

    /**
     * 注册组件
     *
     * @param bean 组件信息
     * @return 返回true表示注册成功 false表示注册失败（未添加组件）
     */
    boolean addBean(BeanInfoRegister bean);

}
