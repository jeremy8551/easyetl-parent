package icu.etl.script.internal;

import java.util.Map;
import javax.script.Bindings;
import javax.script.SimpleBindings;

import icu.etl.script.UniversalScriptVariable;

/**
 * 脚本引擎变量集合
 */
public class ScriptVariable extends SimpleBindings implements UniversalScriptVariable {

    /**
     * 初始化
     */
    public ScriptVariable() {
        super();
    }

    /**
     * 初始化
     *
     * @param map 参数集合
     */
    public ScriptVariable(Map<String, Object> map) {
        super(map);
    }

    public boolean containsKey(Object key) {
        return !"".equals(key) && super.containsKey(key);
    }

    /**
     * 返回变量值
     *
     * @param name 变量名
     * @return 变量值
     */
    public Object get(Object name) {
        return "".equals(name) ? null : super.get(name);
    }

    public void addAll(Bindings bindings) {
        this.putAll(bindings);
    }

}
