package icu.etl.script.internal;

import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptProgram;
import icu.etl.util.Ensure;

/**
 * 用户自定义方法集合
 *
 * @author jeremy8551@qq.com
 */
public class FunctionSet implements UniversalScriptProgram {

    public final static String key = "FunctionSet";

    public static FunctionSet get(UniversalScriptContext context, boolean... array) {
        boolean global = array.length != 0 && array[0];
        FunctionSet obj = context.getProgram(key, global);
        if (obj == null) {
            obj = new FunctionSet();
            context.addProgram(key, obj, global);
        }
        return obj;
    }

    /** 自定义方法名与自定义方法的映射关系 */
    private Hashtable<String, CommandList> map;

    /**
     * 初始化
     */
    public FunctionSet() {
        this.map = new Hashtable<String, CommandList>();
    }

    /**
     * 判断用户自定义方法是否存在
     *
     * @param name 自定义方法名
     * @return 返回true表示用户自定义方法存在
     */
    public boolean contains(String name) {
        return this.map.containsKey(name.toUpperCase());
    }

    /**
     * 添加自定义方法
     *
     * @param cmdlist 自定义方法
     * @return 原来的自定义方法体
     */
    public CommandList add(CommandList cmdlist) {
        Ensure.notNull(cmdlist);
        return this.map.put(cmdlist.getName().toUpperCase(), cmdlist);
    }

    /**
     * 返回自定义方法
     *
     * @param name 自定义方法名
     * @return 自定义方法体
     */
    public CommandList get(String name) {
        return this.map.get(name.toUpperCase());
    }

    /**
     * 删除自定义方法
     *
     * @param name 自定义方法名
     * @return 自定义方法体
     */
    public CommandList remove(String name) {
        return this.map.remove(name.toUpperCase());
    }

    public void close() {
        this.map.clear();
    }

    public ScriptProgramClone deepClone() {
        FunctionSet obj = new FunctionSet();
        Set<Entry<String, CommandList>> set = this.map.entrySet();
        for (Entry<String, CommandList> e : set) {
            String key = e.getKey();
            CommandList body = e.getValue();
            obj.map.put(key, body.clone());
        }
        return new ScriptProgramClone(key, obj);
    }

}
