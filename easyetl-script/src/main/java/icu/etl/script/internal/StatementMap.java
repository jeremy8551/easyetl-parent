package icu.etl.script.internal;

import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptProgram;

/**
 * 数据库批量处理
 *
 * @author jeremy8551@qq.com
 */
public class StatementMap extends MapTemplate<ScriptStatement> implements UniversalScriptProgram {

    public final static String key = "StatementMap";

    public static StatementMap get(UniversalScriptContext context, boolean... array) {
        boolean global = array.length != 0 && array[0];
        StatementMap obj = context.getProgram(key, global);
        if (obj == null) {
            obj = new StatementMap();
            context.addProgram(key, obj, global);
        }
        return obj;
    }

    public ScriptProgramClone deepClone() {
        StatementMap obj = new StatementMap();
        obj.map.putAll(this.map);
        return new ScriptProgramClone(key, this.map);
    }

    public void close() {
        super.close();
    }

}
