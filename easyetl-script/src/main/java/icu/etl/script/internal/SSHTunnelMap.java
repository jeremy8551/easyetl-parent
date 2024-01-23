package icu.etl.script.internal;

import java.util.Hashtable;
import java.util.Iterator;

import icu.etl.os.OSSecureShellCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptProgram;
import icu.etl.util.Ensure;
import icu.etl.util.IO;

/**
 * SSH 协议端口转发客户端集合
 *
 * @author jeremy8551@qq.com
 */
public class SSHTunnelMap implements UniversalScriptProgram {

    public final static String key = "SSHTunnelMap";

    public static SSHTunnelMap get(UniversalScriptContext context, boolean... array) {
        boolean global = array.length != 0 && array[0];
        SSHTunnelMap obj = context.getProgram(key, global);
        if (obj == null) {
            obj = new SSHTunnelMap();
            context.addProgram(key, obj, global);
        }
        return obj;
    }

    private Hashtable<String, OSSecureShellCommand> map;

    /**
     * 初始化
     */
    public SSHTunnelMap() {
        this.map = new Hashtable<String, OSSecureShellCommand>();
    }

    /**
     * 添加 SSH 端口转发协议
     *
     * @param name   客户端名
     * @param client SSH 端口转发协议
     */
    public void add(String name, OSSecureShellCommand client) {
        Ensure.notBlank(name);
        Ensure.notNull(client);
        String key = name.toUpperCase();
        this.close(key);
        this.map.put(key, client);
    }

    /**
     * 关闭客户端并从集合中删除
     *
     * @param name 名字
     */
    public void close(String name) {
        String key = Ensure.notNull(name).toUpperCase();
        OSSecureShellCommand ssh = this.map.get(key);
        if (ssh != null) {
            IO.close(ssh);
            this.map.remove(key);
        }
    }

    public void close() {
        for (Iterator<OSSecureShellCommand> it = this.map.values().iterator(); it.hasNext(); ) {
            OSSecureShellCommand tunnel = it.next();
            IO.closeQuiet(tunnel);
            IO.closeQuietly(tunnel);
        }
        this.map.clear();
    }

    public ScriptProgramClone deepClone() {
        SSHTunnelMap obj = new SSHTunnelMap();
        obj.map.putAll(this.map);
        return new ScriptProgramClone(key, obj);
    }

}
