package icu.etl.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 组件注解
 * IOC容器启动时会扫描带 {@linkplain EasyBean} 注解的类
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EasyBean {

    /**
     * 种类信息, 用于区分相同接口的不同实现类
     *
     * @return 种类信息
     */
    String kind() default "";

    /**
     * 模式信息, 用于区分相同种类的实现类
     *
     * @return 模式信息
     */
    String mode() default "";

    /**
     * 大版本号
     *
     * @return 大版本号
     */
    String major() default "";

    /**
     * 小版本号
     *
     * @return 小版本号
     */
    String minor() default "";

    /**
     * 描述信息
     *
     * @return 描述信息
     */
    String description() default "";

}
