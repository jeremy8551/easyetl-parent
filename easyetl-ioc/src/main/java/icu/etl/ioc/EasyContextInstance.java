package icu.etl.ioc;

/**
 * 容器实例
 *
 * @author jeremy8551@qq.com
 * @createtime 2024/2/8 11:02
 */
public class EasyContextInstance {

    /** 容器的单例模式，默认使用第一个创建的容器作为单例 */
    private static volatile EasyContext value;

    /**
     * 返回实例对象
     *
     * @return 容器上下文信息
     */
    public static EasyContext get() {
        return value;
    }

    /**
     * 设置实例对象
     *
     * @param value 容器上下文信息
     */
    public static void set(EasyContext value) {
        EasyContextInstance.value = value;
    }
}
