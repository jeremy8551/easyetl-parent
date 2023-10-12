package icu.etl.log;

import java.io.PrintStream;

/**
 * 日志接口工厂
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-06-28
 */
public interface LogBuilder {

    /**
     * 创建一个日志输出接口
     *
     * @param factory 日志工厂对象
     * @param cls     类信息
     * @param out     标准信息输出接口
     * @param err     错误信息输出接口
     * @param level   日志输出级别
     * @return 日志接口
     * @throws Exception 创建日志发生错误
     */
    Log create(LogFactory factory, Class<?> cls, PrintStream out, PrintStream err, String level) throws Exception;

}
