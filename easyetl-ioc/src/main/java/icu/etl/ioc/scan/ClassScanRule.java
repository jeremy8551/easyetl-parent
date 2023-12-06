package icu.etl.ioc.scan;

import icu.etl.ioc.EasyBeanRegister;

/**
 * 类扫描器的处理规则
 *
 * @author jeremy8551@qq.com
 */
public interface ClassScanRule {

    /**
     * 处理类加载器传递过来的类信息
     *
     * @param cls      类信息（类扫描器加载的类信息）
     * @param register 组件实现类的添加接口
     * @return 返回true表示类信息 {@code cls} 被使用, false表示未使用类
     */
    boolean process(Class<?> cls, EasyBeanRegister register);

}
