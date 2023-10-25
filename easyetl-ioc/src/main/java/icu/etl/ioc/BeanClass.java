package icu.etl.ioc;

import icu.etl.annotation.EasyBean;

public interface BeanClass {

    /**
     * 组件类信息
     *
     * @return 类信息
     */
    <E> Class<E> getBeanClass();

    /**
     * 返回组件类上配置的注解信息
     *
     * @return 注解信息
     */
    EasyBean getAnnotation();

    /**
     * 组件管理模式
     *
     * @return 详见 {@linkplain EasyBean#singleton()}
     */
    boolean isSingleton();
    
}
