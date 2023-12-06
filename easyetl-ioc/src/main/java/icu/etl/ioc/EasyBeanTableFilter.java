package icu.etl.ioc;

/**
 * 组件实现类的查询条件
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public interface EasyBeanTableFilter {

    /**
     * 过滤条件
     *
     * @param beanInfo 组件信息
     * @return 返回true表示满足查询条件，false表示不满足查询条件
     */
    boolean accept(EasyBeanInfoValue beanInfo);
}
