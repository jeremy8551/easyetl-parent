package icu.etl.ioc;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public interface BeanFilter {

    boolean accept(BeanInfo beanInfo);
}
