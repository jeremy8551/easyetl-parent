package icu.etl.ioc;

import java.util.Comparator;

/**
 * 组件的注册接口
 */
public interface BeanRegister {

    /**
     * 注册组件
     *
     * @param bean       组件信息
     * @param comparator 排序规则，用于判断是否重复添加组件（在容器中，同一组件不能重复添加）
     * @return 返回true表示注册成功 false表示注册失败（未添加组件）
     */
    boolean addBean(BeanInfoRegister bean, Comparator<BeanInfoRegister> comparator);

}
