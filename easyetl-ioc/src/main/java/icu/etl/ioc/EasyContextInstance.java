package icu.etl.ioc;

/**
 * @author jeremy8551@qq.com
 * @createtime 2024/2/8 11:02
 */
public class EasyContextInstance {

    /** 容器的单例模式，默认使用第一个创建的容器作为单例 */
    public static volatile EasyContext INSTANCE;
    
}
