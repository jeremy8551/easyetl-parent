package icu.etl.concurrent;

import java.util.List;
import java.util.Vector;

/**
 * 批量执行多个线程<br>
 * <p>
 * ExecuteThread et = new ExecuteThread(同时执行线程的个数); <br>
 * et.addTask(线程对象); <br>
 * et.addTask(线程对象); <br>
 * et.execute(); // 开始执行所有线程 <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2010-01-18 01:03:07
 */
public class ExecuteThread {

    /** 同时运行线程的个数 */
    protected int sameRunNum;

    /** 正在执行的线程 */
    protected List<Executor> tasks;

    /** 执行完毕的线程 */
    protected List<Executor> overTasks;

    /** 保存错误信息, 一个线程对应List中的一条信息 */
    protected List<String> errmsg;

    /**
     * 初始化
     *
     * @param num 同时执行线程的个数
     */
    public ExecuteThread(int num) {
        if (num <= 0) {
            throw new IllegalArgumentException(String.valueOf(num));
        }

        this.sameRunNum = num;
        this.errmsg = new Vector<String>();
        this.tasks = new Vector<Executor>();
        this.overTasks = new Vector<Executor>();
    }

    /**
     * 添加线程任务
     */
    public void addTask(Executor tks) {
        this.tasks.add(tks);
    }

    /**
     * 检查是否有 Null 线程
     */
    private void checkNullValue() {
        for (int i = 0; i < this.tasks.size(); i++) {
            Executor et = (Executor) this.tasks.get(i);
            if (et == null) {
                throw new NullPointerException("thread [" + i + "] is null !");
            }
        }
    }

    /**
     * 去掉执行完毕的线程
     */
    private void removeOverTask() {
        for (int i = 0; i < this.tasks.size(); i++) {
            Executor et = (Executor) this.tasks.get(i);
            if (et.alreadyStop()) {
                // 如果发生错误，则保留错误信息
                if (et.alreadyError()) {
                    this.errmsg.add(et.getErrorMessage());
                }
                // 从可执行线程序列中移除已执行完毕的线程
                if (this.tasks.remove(et)) {
                    this.overTasks.add(et);
                }
            }
        }
    }

    /**
     * 执行所有线程
     */
    public void execute() {
        this.checkNullValue();
        while (!tasks.isEmpty()) {
            for (int i = 0; i < this.tasks.size() && i < this.sameRunNum; i++) {
                Executor et = (Executor) this.tasks.get(i);
                et.start();
            }
            this.removeOverTask();
        }
    }

    /**
     * 判断是否发生错误 <br>
     * true 表示卸数发生错误 <br>
     * false 没有错误
     *
     * @return
     */
    public boolean isError() {
        int len = this.overTasks.size();// 遍历已完成的任务
        for (int i = 0; i < len; i++) {
            Executor et = (Executor) this.overTasks.get(i);
            if (et != null && et.alreadyError()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回所有错误信息
     *
     * @return
     */
    public List<String> getErrmsg() {
        return errmsg;
    }

    /**
     * 返回已执行完毕任务数（包含报错的任务）
     *
     * @return
     */
    public int getOverTaskSize() {
        return this.overTasks.size();
    }

}