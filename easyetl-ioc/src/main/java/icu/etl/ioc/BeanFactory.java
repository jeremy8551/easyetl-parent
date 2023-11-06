package icu.etl.ioc;

/**
 * 组件工厂
 */
public interface BeanFactory {

    /**
     * 创建 {@code type} 的实例对象
     * <p>
     * 容器会向组件实例对象中自动注入属性（容器上下文信息等）
     *
     * @param type 类或接口
     * @param args 执行构造方法时，传入的参数
     * @param <E>  类型
     * @return 返回组件实例对象
     */
    <E> E createBean(Class<?> type, Object... args);

}
