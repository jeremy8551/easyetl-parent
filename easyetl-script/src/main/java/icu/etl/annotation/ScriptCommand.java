package icu.etl.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 脚本引擎命令的工厂类配置注解
 *
 * @author jeremy8551@qq.com
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ScriptCommand {

    /**
     * 脚本命令前缀，如: echo
     *
     * @return
     */
    String[] name();

    /**
     * 关键字 <br>
     * 被定义为关键字后不能在变量名中使用
     *
     * @return
     */
    String[] keywords() default {};

}
