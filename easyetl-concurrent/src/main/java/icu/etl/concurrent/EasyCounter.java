package icu.etl.concurrent;

/**
 * 线程安全的整数计数器
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/12/6
 */
public class EasyCounter {

    /** 数值 */
    private volatile int n;

    /**
     * 计算器
     *
     * @param n 初始值
     */
    public EasyCounter(int n) {
        this.n = n;
    }

    /**
     * 先自增一，再返回值
     *
     * @return 整数
     */
    public synchronized int incrementAndGet() {
        ++n;
        return n;
    }

    /**
     * 先自减一，再返回值
     *
     * @return 整数
     */
    public synchronized int decrementAndGet() {
        --n;
        return n;
    }

    /**
     * 先加参数，再返回值
     *
     * @param value 整数
     * @return 整数
     */
    public synchronized int addAndGet(int value) {
        n += value;
        return n;
    }

    /**
     * 返回值
     *
     * @return 整数
     */
    public int get() {
        return n;
    }

}
