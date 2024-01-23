package icu.etl.script.internal;

import java.util.Hashtable;

import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptProgram;
import icu.etl.util.StringUtils;

/**
 * 进度输出组件集合
 *
 * @author jeremy8551@qq.com
 */
public class ProgressMap implements UniversalScriptProgram {

    public final static String key = "ProgressMap";

    public static ProgressMap get(UniversalScriptContext context, boolean... array) {
        boolean global = array.length != 0 && array[0];
        ProgressMap obj = context.getProgram(key, global);
        if (obj == null) {
            obj = new ProgressMap();
            context.addProgram(key, obj, global);
        }
        return obj;
    }

    public static ScriptProgress getProgress(UniversalScriptContext context, String name) {
        ScriptProgress obj = ProgressMap.get(context, false).get(name);
        if (obj == null) {
            obj = ProgressMap.get(context, true).get(name);
        }
        return obj;
    }

    public static ScriptProgress getProgress(UniversalScriptContext context) {
        ScriptProgress obj = ProgressMap.get(context, false).get();
        if (obj == null) {
            obj = ProgressMap.get(context, true).get();
        }
        return obj;
    }

    /** 脚本命令编号与脚本的映射关系 */
    private Hashtable<String, ScriptProgress> map;

    /** 单例进度输出组件 */
    private ScriptProgress progress;

    /**
     * 初始化
     */
    public ProgressMap() {
        super();
        this.map = new Hashtable<String, ScriptProgress>();
    }

    /**
     * 返回 true 表示存在进度输出组件
     *
     * @param taskId 任务ID
     * @return 返回true表示存在进度输出组件
     */
    public boolean contains(String taskId) {
        return taskId != null && this.map.containsKey(taskId.toUpperCase());
    }

    /**
     * 添加一个进度输出组件
     *
     * @param progress 进度输出组件
     */
    public void add(ScriptProgress progress) {
        if (StringUtils.isBlank(progress.getTaskId())) {
            this.progress = progress;
        } else {
            this.map.put(progress.getTaskId().toUpperCase(), progress);
        }
    }

    /**
     * 返回默认的进度输出组件
     *
     * @return 进度输出组件
     */
    public ScriptProgress get() {
        return this.progress;
    }

    /**
     * 返回进度输出组件
     *
     * @param taskId 任务ID
     * @return 进度输出组件
     */
    public ScriptProgress get(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            return this.progress;
        } else {
            return this.map.get(taskId.toUpperCase());
        }
    }

    /**
     * 删除进度输出组件
     *
     * @param taskId 任务ID
     * @return 进度输出组件
     */
    public ScriptProgress remove(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            ScriptProgress obj = this.progress;
            this.progress = null;
            return obj;
        } else {
            return this.map.remove(taskId.toUpperCase());
        }
    }

    public void close() {
        this.map.clear();
        this.progress = null;
    }

    public ScriptProgramClone deepClone() {
        ProgressMap obj = new ProgressMap();
        obj.progress = this.progress;
        obj.map.putAll(this.map);
        return new ScriptProgramClone(key, obj);
    }

}
