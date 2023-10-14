package icu.etl.script.session;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Set;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptSessionFactory;
import icu.etl.util.IO;

/**
 * 脚本引擎用户会话信息集合
 */
@EasyBeanClass(type = UniversalScriptSessionFactory.class, kind = "default", mode = "", major = "", minor = "", description = "")
public class ScriptSessionFactory implements UniversalScriptSessionFactory {

    /** 用户会话编号与用户会话信息的映射关系 */
    private LinkedHashMap<String, UniversalScriptSession> map;

    /**
     * 初始化
     */
    public ScriptSessionFactory() {
        this.map = new LinkedHashMap<String, UniversalScriptSession>();
    }

    public UniversalScriptSession build() {
        ScriptSession session = new ScriptSession(this);
        this.map.put(session.getId(), session);
        return session;
    }

    public UniversalScriptSession remove(String sessionid) {
        return this.map.remove(sessionid);
    }

    public boolean isAlive() {
        Set<String> set = this.map.keySet();
        for (String id : set) {
            UniversalScriptSession session = this.map.get(id);
            if (session != null && session.isAlive()) {
                return true;
            }
        }
        return false;
    }

    public void terminate() throws IOException, SQLException {
        Set<String> set = this.map.keySet();
        for (String id : set) { // 遍历脚本引擎中所有用户会话信息
            UniversalScriptSession session = this.map.get(id);
            if (session != null) {
                session.terminate();
            }
        }
    }

    public void terminate(String id) throws IOException, SQLException {
        UniversalScriptSession session = this.map.get(id);
        if (session != null) {
            session.terminate();
        }
    }

    public Set<String> getSessionIDs() {
        return this.map.keySet();
    }

    /**
     * 将用户会话信息注册到会话池
     *
     * @param session 用户会话
     * @return
     */
    public UniversalScriptSession add(UniversalScriptSession session) {
        if (session == null) {
            throw new NullPointerException();
        } else {
            return this.map.put(session.getId(), session);
        }
    }

    public UniversalScriptSession get(String sessionid) {
        return this.map.get(sessionid);
    }

    public void clear() {
        IO.close(this.map);
        this.map.clear();
    }

}
