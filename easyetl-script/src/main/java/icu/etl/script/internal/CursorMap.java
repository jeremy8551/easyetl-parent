package icu.etl.script.internal;

import icu.etl.database.JdbcQueryStatement;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptProgram;

/**
 * 数据库游标
 *
 * @author jeremy8551@qq.com
 */
public class CursorMap extends MapTemplate<JdbcQueryStatement> implements UniversalScriptProgram {

    public final static String key = "CursorMap";

    public static CursorMap get(UniversalScriptContext context, boolean... array) {
        boolean global = array.length != 0 && array[0];
        CursorMap obj = context.getProgram(key, global);
        if (obj == null) {
            obj = new CursorMap();
            context.addProgram(key, obj, global);
        }
        return obj;
    }

    public ScriptProgramClone deepClone() {
        CursorMap obj = new CursorMap();
        obj.map.putAll(this.map);
        return new ScriptProgramClone(key, this.map);
    }

    public void close() {
        super.close();
    }

}
