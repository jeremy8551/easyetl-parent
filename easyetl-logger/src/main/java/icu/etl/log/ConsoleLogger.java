package icu.etl.log;

/**
 * 使用 System.out 与 System.err 输出日志信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-10-09
 */
public class ConsoleLogger extends DefaultLogger implements Log {

    public ConsoleLogger(LogFactory factory, String level) {
        super(factory, ConsoleLogger.class, level);
    }

    public void print(CharSequence msg) {
        if (this.factory.isDisable()) {
            return;
        }

        System.out.print(msg);
    }

    public void println(Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        e.printStackTrace(System.err);
    }

    public void write(String level, CharSequence msg) {
        if (this.factory.isDisable()) {
            return;
        }

        System.out.println(msg);
    }

    public void write(String level, CharSequence msg, Throwable e) {
        if (this.factory.isDisable()) {
            return;
        }

        System.err.println(msg);
        e.printStackTrace(System.err);
    }

    public Log writeline(CharSequence msg) {
        if (this.factory.isDisable()) {
            return this;
        }

        System.out.println(msg);
        return this;
    }

}