package icu.etl.log.cxt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;

import icu.etl.log.Log;

/**
 * 日志池
 * 用于保存所有已注册还存活的日志接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/21
 */
public class LogPool {

    /** 还存活的日志接口 */
    private WeakHashMap<String, Log> map;

    /**
     * 日志池
     */
    public LogPool() {
        this.map = new WeakHashMap<String, Log>(40);
    }

    /**
     * 添加日志
     *
     * @param log 日志接口
     */
    public void add(Log log) {
        this.map.put(log.getName(), log);
    }

    /**
     * 查询指定包下的日志接口
     *
     * @param name 包名或类名
     * @return 日志接口集合
     */
    public List<Log> get(String name) {
        Collection<Log> values = this.map.values();
        ArrayList<Log> list = new ArrayList<Log>(values.size());
        if (LogLevelManager.isRoot(name)) {
            list.addAll(values);
        } else {
            for (Log log : values) {
                if (log.getName() != null && log.getName().startsWith(name)) {
                    list.add(log);
                }
            }
        }
        return list;
    }

}
