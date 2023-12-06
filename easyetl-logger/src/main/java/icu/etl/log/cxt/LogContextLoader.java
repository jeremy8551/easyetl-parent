package icu.etl.log.cxt;

import icu.etl.log.LogContext;
import icu.etl.log.LogFactory;
import icu.etl.log.LogLevel;
import icu.etl.log.apd.ConsoleAppender;
import icu.etl.log.apd.DefaultLogBuilder;
import icu.etl.log.slf4j.Slf4jLogBuilder;
import icu.etl.util.StringUtils;

/**
 * 日志配置信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/21
 */
public class LogContextLoader {

    private LogContext context;

    public LogContextLoader(LogContext context) {
        this.context = context;
    }

    /**
     * 解析日志配置信息
     *
     * @param str 日志配置信息
     * @return 返回true表示解析成功 false表示失败
     */
    public boolean parse(String str) {
        if (str == null || str.indexOf(':') == -1) {
            return false;
        }

        String[] array = StringUtils.split(str, ':');
        if (array.length > 2) {
            throw new IllegalArgumentException(str);
        }

        // 解析日志工厂配置
        this.parseBuilder(array);

        // 如果第一个元素是日志级别
        if (LogLevel.is(array[0])) {
            this.context.updateLevel(array[1], LogLevel.of(array[0]));
            return true;
        }

        if (array.length >= 2 && LogLevel.is(array[1])) {
            this.context.updateLevel(array[0], LogLevel.of(array[1]));
            return true;
        }
        return true;
    }

    /**
     * 解析参数中的日志工厂
     *
     * @param array 配置信息
     */
    private void parseBuilder(String[] array) {
        for (int i = 0; i < array.length; i++) {
            String str = array[i];

            // 使用控制台输出
            int length = str.length();
            if (length >= "sout".length()  //
                    && (str.charAt(0) == 's' || str.charAt(0) == 'S') //
                    && (str.charAt(1) == 'o' || str.charAt(1) == 'O') //
                    && (str.charAt(2) == 'u' || str.charAt(2) == 'U') //
                    && (str.charAt(3) == 't' || str.charAt(3) == 'T') //
            ) {
                this.context.setBuilder(new DefaultLogBuilder());
                array[i] = "";

                String pattern;
                if (length >= "sout+".length() && str.charAt(4) == '+') {
                    String trim = str.substring("sout+".length()).trim();
                    pattern = LogFactory.getPattern(trim, true);
                } else {
                    pattern = "";
                }

                new ConsoleAppender(pattern).setup(this.context);
                continue;
            }

            // 使用slf4j输出
            if ("slf4j".equalsIgnoreCase(str)) {
                this.context.setBuilder(new Slf4jLogBuilder());
                array[i] = "";
                continue;
            }
        }
    }

}
