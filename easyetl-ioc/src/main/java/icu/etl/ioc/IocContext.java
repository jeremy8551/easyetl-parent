package icu.etl.ioc;

/**
 * 容器接口
 */
public interface IocContext extends BeanContext {

    /**
     * 容器名，必须唯一
     *
     * @return 容器名
     */
    String getName();

}