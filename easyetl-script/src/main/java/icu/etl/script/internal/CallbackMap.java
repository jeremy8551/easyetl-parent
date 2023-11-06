package icu.etl.script.internal;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptProgram;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;

/**
 * 回调函数管理集合类
 *
 * @author jeremy8551@qq.com
 */
public class CallbackMap implements UniversalScriptProgram {

    public final static String key = "CallbackMap";

    public static CallbackMap get(UniversalScriptContext context, boolean... array) {
        boolean global = array.length == 0 ? false : array[0];
        CallbackMap obj = context.getProgram(key, global);
        if (obj == null) {
            obj = new CallbackMap();
            context.addProgram(key, obj, global);
        }
        return obj;
    }

    /** 脚本命令类信息与钩子函数集合 */
    private Map<Class<?>, Callback> map;

    /**
     * 初始化
     */
    public CallbackMap() {
        super();
        this.map = new HashMap<Class<?>, Callback>();
    }

    /**
     * 执行脚本命令的回调函数
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true表示可以输出信息
     * @param cls         脚本命令
     * @param args        脚本命令对应的参数
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public int executeCallback(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, Class<?> cls, String[] args) throws IOException, SQLException {
        Callback obj = this.get(cls);
        return obj.executeCallback(session, context, stdout, stderr, forceStdout, args);
    }

    /**
     * 返回脚本命令对应的所有回调函数（按行数定义的先后顺序）
     *
     * @param key 脚本命令类信息
     * @return 脚本目录对应的回调函数
     */
    private Callback get(Object key) {
        Class<?> cls = (Class<?>) ((key instanceof Class) ? key : key.getClass());
        Callback obj = this.map.get(cls);
        if (obj == null) {
            obj = new Callback();
            this.map.put(cls, obj);
        }
        return obj;
    }

    /**
     * 添加一个脚本命令的回调函数
     *
     * @param key  脚本目录类信息
     * @param list 钩子函数
     */
    public void add(Object key, CommandList list) {
        this.get(key).add(list);
    }

    /**
     * 删除一个脚本命令的回调函数
     *
     * @param cls
     */
    public void remove(Class<? extends UniversalCommandCompiler> cls) {
        this.map.remove(cls);
    }

    public ScriptProgramClone deepClone() {
        CallbackMap obj = new CallbackMap();
        Set<Entry<Class<?>, Callback>> set = this.map.entrySet();
        for (Entry<Class<?>, Callback> entry : set) {
            Class<?> key = entry.getKey();
            Callback val = entry.getValue();
            obj.map.put(key, val.clone());
        }
        return new ScriptProgramClone(key, obj);
    }

    public void close() throws IOException {
        this.map.clear();
    }

}
