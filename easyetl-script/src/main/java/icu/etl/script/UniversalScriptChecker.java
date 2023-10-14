package icu.etl.script;

import java.util.Set;

/**
 * 脚本引擎内部使用的校验逻辑类
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptChecker {

    /**
     * 设置数据库关键字
     *
     * @param databaseKeyword
     */
    void setDatabaseKeywords(Set<String> databaseKeyword);

    /**
     * 设置脚本引擎关键字
     *
     * @param scriptKeyword
     */
    void setScriptEngineKeywords(Set<String> scriptKeyword);

    /**
     * 判断变量名是否合法
     *
     * @param name 变量名
     * @return
     */
    boolean isVariableName(String name);

    /**
     * 判断字符串参数 name 是否是数据库关键字
     *
     * @param name 字符串
     * @return
     */
    boolean isDatabaseKeyword(String name);

}
