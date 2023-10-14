package icu.etl.script;

public interface UniversalScriptContextAware {

    /**
     * 注入脚本引擎上下文信息
     *
     * @param context
     */
    void setContext(UniversalScriptContext context);
}
