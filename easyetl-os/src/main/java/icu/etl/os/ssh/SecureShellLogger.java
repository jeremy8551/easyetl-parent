package icu.etl.os.ssh;

import icu.etl.log.Log;
import icu.etl.log.STD;
import com.jcraft.jsch.Logger;

/**
 * jsch 包的日志适配器
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-03-22
 */
public class SecureShellLogger implements Logger {

    public SecureShellLogger() {
    }

    public Log getLog() {
        return STD.out;
    }

    public boolean isEnabled(int level) {
        switch (level) {
            case DEBUG:
                return STD.out.isDebugEnabled();

            case INFO:
                return STD.out.isInfoEnabled();

            case WARN:
                return STD.out.isWarnEnabled();

            case ERROR:
                return STD.out.isErrorEnabled();

            case FATAL:
                return STD.out.isFatalEnabled();

            default:
                return false;
        }
    }

    public void log(int level, String message) {
        switch (level) {
            case DEBUG:
//				if (log.isDebugEnabled()) {
//					log.debug(message);
//				}
                break;

            case INFO:
//				if (log.isInfoEnabled()) {
//					log.info(message);
//				}
                break;

            case WARN:
//				if (log.isWarnEnabled()) {
//					log.warn(message);
//				}
                break;

            case ERROR:
                if (STD.out.isErrorEnabled()) {
                    STD.out.error(message);
                }
                break;

            case FATAL:
                if (STD.out.isFatalEnabled()) {
                    STD.out.fatal(message);
                }
                break;

            default:
                throw new UnsupportedOperationException(String.valueOf(level));
        }
    }
}
