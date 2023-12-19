package icu.etl.script;

import java.util.Set;

public interface UniversalScriptSessionFactory {

    /**
     * 创建一个用户会话信息实例
     *
     * @param engine 脚本引擎
     * @return 用户会话信息
     */
    UniversalScriptSession build(UniversalScriptEngine engine);

    /**
     * 返回所有用户会话编号
     *
     * @return
     */
    Set<String> getSessionIDs();

    /**
     * 返回用户会话信息
     *
     * @param sessionid 用户会话编号
     * @return
     */
    UniversalScriptSession get(String sessionid);

    /**
     * 删除用户会话信息
     *
     * @param sessionid 用户会话编号
     * @return
     */
    UniversalScriptSession remove(String sessionid);

    /**
     * 判断是否还有活动的用户会话信息
     *
     * @return
     */
    boolean isAlive();

    /**
     * 终止所有会话信息
     *
     * @throws Exception
     */
    void terminate() throws Exception;

    /**
     * 终止会话信息
     *
     * @param id 会话编号
     * @throws Exception
     */
    void terminate(String id) throws Exception;

    /**
     * 清空所有用户会话信息
     */
    void clear();

}