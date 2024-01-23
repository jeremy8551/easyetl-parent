package icu.etl.time;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ResourcesUtils;

/**
 * 定时任务集合
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-05
 */
public class TaskList {
    private final static Log log = LogFactory.getLog(TaskList.class);

    private Vector<String> list;
    private Hashtable<String, TimerTask> map;

    /**
     * 初始化集合
     *
     * @param initialCapacity 集合初始容量
     */
    public TaskList(int initialCapacity) {
        super();
        list = new Vector<String>(initialCapacity);
        map = new Hashtable<String, TimerTask>(initialCapacity);
    }

    /**
     * 判断定时任务是否存在
     *
     * @param taskId 定时任务id
     * @return true表示任务已经存在
     */
    public synchronized boolean existsTask(String taskId) {
        if (taskId == null) {
            return false;
        }
        return map.containsKey(taskId);
    }

    /**
     * 追加新任务到集合<br>
     * 如果集合中已经存在任务则删除旧任务，并追加新任务到集合<br>
     *
     * @param task 定时任务
     * @return true表示任务添加成功
     */
    public synchronized boolean add(TimerTask task) {
        if (task == null) {
            return false;
        }

        String taskId = task.getTaskId();
        if (taskId == null) {
            return false;
        }

        TimerTask old = this.map.put(taskId, task);
        if (old != null) {
            list.remove(taskId);
        }
        list.add(taskId);
        return true;
    }

    /**
     * 搜索定时任务
     *
     * @param index 位置, 从0开始
     * @return 定时任务
     */
    public synchronized TimerTask get(int index) {
        String taskId = list.get(index);
        return map.get(taskId);
    }

    /**
     * 搜索定时任务
     *
     * @param taskId 定时任务id
     * @return null表示未搜索到任务
     */
    public synchronized TimerTask get(String taskId) {
        if (taskId == null) {
            return null;
        }
        return map.get(taskId);
    }

    /**
     * 定时任务数
     *
     * @return 定时任务数
     */
    public synchronized int size() {
        return list.size();
    }

    /**
     * 从集合中删除定时任务
     *
     * @param task 定时任务
     * @return true表示成功删除任务 false表示集合中不存在此任务
     */
    public synchronized boolean remove(TimerTask task) {
        if (task == null) {
            return false;
        }
        return remove(task.getTaskId());
    }

    /**
     * 从集合中删除定时任务
     *
     * @param taskId 定时任务id
     * @return true表示成功删除任务 false表示集合中不存在此任务
     */
    public synchronized boolean remove(String taskId) {
        if (taskId == null) {
            return false;
        }

        TimerTask obj = map.remove(taskId);
        if (obj == null) {
            return false;
        } else {
            list.remove(taskId);
            return true;
        }
    }

    /**
     * 清空集合中的所有数据
     */
    public synchronized void clear() {
        this.map.clear();
        this.list.clear();
    }

    /**
     * 复制任务到数组
     */
    public synchronized List<TimerTask> toList() {
        ArrayList<TimerTask> obj = new ArrayList<TimerTask>(map.size());
        obj.addAll(this.map.values());
        return obj;
    }

    /**
     * 终止任务 <br>
     * 本函数不能保证任务真正立刻终止,只负责调用 TimerTask.terminate() 函数. <br>
     * 如果任务的 TimerTask.terminate() 函数没有实现终止操作任务还会继续运行知道运行完毕
     *
     * @param task 定时任务
     */
    public synchronized void kill(TimerTask task) {
        if (task == null) {
            return;
        }

        try {
            if (task.isRunning()) {
                task.terminate();
            }
        } catch (TimerException e) {
            if (log.isErrorEnabled()) {
                log.error(ResourcesUtils.getMessage("timer.standard.output.msg035", task.getTaskId()), e);
            }
        }
    }

    /**
     * 终止所有任务 <br>
     * 本函数不能保证任务真正立刻终止,只负责调用 TimerTask.terminate() 函数. <br>
     * 如果任务的 TimerTask.terminate() 函数没有实现终止操作任务还会继续运行知道运行完毕
     */
    public synchronized void kill() {
        Collection<TimerTask> values = this.map.values();
        Iterator<TimerTask> it = values.iterator();
        while (it.hasNext()) {
            TimerTask task = it.next();
            kill(task);
        }
    }
}
