package icu.etl.script.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptProgram;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.io.ScriptStderr;
import icu.etl.util.Ensure;

/**
 * JAVA 错误处理逻辑
 */
public class ErrorHandlerMap implements UniversalScriptProgram {

    public final static String key = "ErrorHandlerMap";

    public static ErrorHandlerMap get(UniversalScriptContext context, boolean... array) {
        boolean global = array.length == 0 ? false : array[0];
        ErrorHandlerMap obj = context.getProgram(key, global);
        if (obj == null) {
            obj = new ErrorHandlerMap();
            context.addProgram(key, obj, global);
        }
        return obj;
    }

    /** 无异常处理类 */
    public final static int EMPTY_HANDLER = -1;

    /** 退出 */
    public final static int EXIT_HANDLER = 0;

    /** 继续向下执行 */
    public final static int CONTINUE_HANDLER = 1;

    /** 执行条件与异常处理逻辑的映射关系 */
    private LinkedHashMap<String, ScriptHandler> map;

    /** true 表示 {@link ErrorHandlerMap#catchCommandError(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, boolean, String, Throwable)}} 已经被执行了 */
    private boolean hasHandle;

    /**
     * 初始化
     */
    public ErrorHandlerMap() {
        this.map = new LinkedHashMap<String, ScriptHandler>();
    }

    /**
     * 添加异常错误处理逻辑
     *
     * @param handler 异常错误处理逻辑
     * @return 处理逻辑
     */
    public ScriptHandler add(ScriptHandler handler) {
        Ensure.notNull(handler);
        return this.map.put(handler.getKey(), handler);
    }

    /**
     * 删除异常错误处理逻辑
     *
     * @param condition 异常错误处理逻辑的执行条件, 如: exception
     * @return 处理逻辑
     */
    public ScriptHandler remove(String condition) {
        return this.map.remove(ScriptHandler.toKey(Ensure.notBlank(condition)));
    }

    /**
     * 返回异常处理逻辑个数
     *
     * @return 处理逻辑个数
     */
    public int size() {
        return this.map.size();
    }

    /**
     * 返回所有异常处理逻辑
     *
     * @return 处理逻辑集合
     */
    public Collection<ScriptHandler> values() {
        return Collections.unmodifiableCollection(this.map.values());
    }

    /**
     * 判断是否已执行过 {@linkplain #catchEvalError(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, boolean, String, Throwable)} 方法
     *
     * @return 返回 true 表示 {@linkplain #catchEvalError(UniversalScriptSession, UniversalScriptContext, UniversalScriptStdout, UniversalScriptStderr, boolean, String, Throwable)} 方法已被执行过
     */
    public boolean alreadyCatchEvalError() {
        return this.hasHandle;
    }

    /**
     * 执行java异常错误处理逻辑
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param script      命令语句
     * @param exception   java异常错误信息
     * @return -1表示没有异常处理逻辑，0表示退出脚本，1表示继续向下执行
     */
    public synchronized int catchCommandError(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, String script, Throwable exception) {
        if (stderr == null) {
            stderr = new ScriptStderr();
        }

        if (this.map.isEmpty()) {
            return ErrorHandlerMap.EMPTY_HANDLER;
        }

        int exit = ErrorHandlerMap.EXIT_HANDLER;
        Set<String> names = this.map.keySet();
        for (String name : names) {
            ScriptHandler handler = this.map.get(name);
            if (handler != null && handler.executeException(session, context, stdout, stderr, forceStdout, script, exception)) {
                if (!handler.isReturnExit()) {
                    exit = ErrorHandlerMap.CONTINUE_HANDLER;
                }
            }
        }
        this.hasHandle = true;
        return exit;
    }

    /**
     * 执行脚本引擎运行错误处理逻辑
     *
     * @param session     用户会话信息
     * @param context     脚本引擎上下文信息
     * @param stdout      标准信息输出接口
     * @param stderr      错误信息输出接口
     * @param forceStdout true 表示使用标准信息输出接口输出标准信息（忽略 {@linkplain UniversalScriptSession#isEchoEnable()} 返回值）
     * @param message     输出信息
     * @param exception   脚本引擎运行错误
     * @return -1表示没有异常处理逻辑；0表示退出脚本；1表示继续向下执行
     */
    public synchronized int catchEvalError(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, String message, Throwable exception) {
        if (stderr == null) {
            stderr = new ScriptStderr();
        }

        if (this.map.isEmpty()) {
            return ErrorHandlerMap.EMPTY_HANDLER;
        }

        int exit = ErrorHandlerMap.EXIT_HANDLER;
        Set<String> names = this.map.keySet();
        for (String name : names) {
            ScriptHandler handler = this.map.get(name);
            if (handler != null && handler.executeException(session, context, stdout, stderr, forceStdout, message, exception)) {
                if (!handler.isReturnExit()) {
                    exit = ErrorHandlerMap.CONTINUE_HANDLER;
                }
            }
        }
        this.hasHandle = true;
        return exit;
    }

    public ScriptProgramClone deepClone() {
        ErrorHandlerMap obj = new ErrorHandlerMap();
        obj.hasHandle = this.hasHandle;
        Set<Entry<String, ScriptHandler>> set = this.map.entrySet();
        for (Entry<String, ScriptHandler> e : set) {
            String key = e.getKey();
            ScriptHandler val = e.getValue().clone();
            obj.map.put(key, val);
        }
        return new ScriptProgramClone(key, obj);
    }

    public void close() throws IOException {
        Collection<ScriptHandler> values = this.map.values();
        for (ScriptHandler handler : values) {
            if (handler != null) {
                handler.clear();
            }
        }

        this.map.clear();
        this.hasHandle = false;
    }

}
