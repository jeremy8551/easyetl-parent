package icu.etl.concurrent;

import java.util.ArrayList;
import java.util.List;

import icu.etl.collection.Throwables;

/**
 * 可终止任务集合
 *
 * @author jeremy8551@qq.com
 * @createtime 2023-09-29
 */
public class Terminates implements Terminate {

    /** 观察者 */
    private final List<Terminate> list;

    /** 任务的终止状态 */
    private volatile boolean terminate;

    /**
     * 初始化
     */
    public Terminates() {
        this.terminate = false;
        this.list = new ArrayList<Terminate>();
    }

    /**
     * 添加任务
     *
     * @param terminate 并发任务
     */
    public synchronized void add(Terminate terminate) {
        this.list.add(terminate);
    }

    /**
     * 删除任务
     *
     * @param terminate 任务
     */
    public synchronized void remove(Terminate terminate) {
        this.list.remove(terminate);
    }

    public boolean isTerminate() {
        return this.terminate;
    }

    /**
     * 终止任务
     */
    public synchronized void terminate() {
        this.terminate = true;
        Throwables throwables = new Throwables();
        for (Terminate obj : this.list) {
            try {
                if (obj != null && !obj.isTerminate()) {
                    obj.terminate();
                }
            } catch (Throwable e) {
                throwables.add(e);
            }
        }

        if (throwables.notEmpty()) {
            throw throwables;
        }
    }

}
