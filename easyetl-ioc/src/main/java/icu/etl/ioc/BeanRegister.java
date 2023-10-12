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
     * @param comparator 排序规则（不能重复添加组件）
     */
    void add(BeanConfig bean, Comparator<BeanConfig> comparator);

}
