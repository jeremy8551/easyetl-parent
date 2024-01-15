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
     * @param set 数据库关键字集合
     */
    void setDatabaseKeywords(Set<String> set);

    /**
     * 设置脚本引擎关键字
     *
     * @param set 关键字集合
     */
    void setScriptEngineKeywords(Set<String> set);

    /**
     * 判断变量名是否合法
     *
     * @param name 变量名
     * @return 返回true表示变量名合法 false表示变量名非法
     */
    boolean isVariableName(String name);

    /**
     * 判断字符串参数 name 是否是数据库关键字
     *
     * @param name 字符串
     * @return 返回true表示关键字合法 false表示关键字非法
     */
    boolean isDatabaseKeyword(String name);

}
