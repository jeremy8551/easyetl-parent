package icu.etl.script;

public interface UniversalScriptContextAware {

    /**
     * 注入脚本引擎上下文
     *
     * @param context 脚本引擎上下文
     */
    void setContext(UniversalScriptContext context);
}
