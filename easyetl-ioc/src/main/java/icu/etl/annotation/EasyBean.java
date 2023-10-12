package icu.etl.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import icu.etl.ioc.BeanBuilder;

/**
 * 组件注解，被标记的类是一个组件
 * <p>
 * IOC容器启动时会扫描带 {@linkplain EasyBean} 与 {@linkplain EasyBeanClass} 注解的类
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EasyBean {

    /**
     * 组件的工厂类（工厂类中必须有无参数的构造方法）
     *
     * @return 组件工厂类
     */
    Class<? extends BeanBuilder<?>> builder();

}
