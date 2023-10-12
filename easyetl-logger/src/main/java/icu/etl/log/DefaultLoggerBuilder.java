package icu.etl.log;

import java.io.PrintStream;

/**
 * @author jeremy8551@qq.com
 * @createtime 2012-06-28
 */
public class DefaultLoggerBuilder implements LogBuilder {

    public Log create(LogFactory factory, Class<?> cls, PrintStream out, PrintStream err, String level) throws Exception {
        return new DefaultLogger(factory, cls, out, err, level);
    }

}
