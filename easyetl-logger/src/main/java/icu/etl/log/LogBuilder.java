package icu.etl.log;

/**
 * 日志工厂接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-06-28
 */
public interface LogBuilder {

    /**
     * 创建一个日志输出接口
     *
     * @param context         日志工厂对象
     * @param type            日志归属的类
     * @param fqcn            用于定位输出日志的代码位置信息的标识符
     * @param dynamicCategory true表示动态生成日志接口归属的类名, false表示使用 {@code type} 作为日志接口归属的类名
     * @return 日志接口
     * @throws Exception 创建日志发生错误
     */
    Log create(LogContext context, Class<?> type, String fqcn, boolean dynamicCategory) throws Exception;
}
