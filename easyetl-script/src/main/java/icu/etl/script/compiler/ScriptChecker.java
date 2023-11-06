package icu.etl.script.compiler;

import java.util.Set;

import icu.etl.annotation.EasyBean;
import icu.etl.script.UniversalScriptChecker;
import icu.etl.util.StringUtils;

/**
 * 校验规则
 *
 * @author jeremy8551@qq.com
 */
@EasyBean(name = "default", description = "")
public class ScriptChecker implements UniversalScriptChecker {

    /** 数据库的关键字 */
    private Set<String> databaseKeyword;

    /** 脚本引擎的关键字 */
    private Set<String> scriptKeyword;

    /**
     * 初始化
     */
    public ScriptChecker() {
    }

    /**
     * 设置数据库关键字
     *
     * @param databaseKeyword
     */
    public void setDatabaseKeywords(Set<String> databaseKeyword) {
        this.databaseKeyword = databaseKeyword;
    }

    /**
     * 设置脚本引擎关键字
     *
     * @param scriptKeyword
     */
    public void setScriptEngineKeywords(Set<String> scriptKeyword) {
        this.scriptKeyword = scriptKeyword;
    }

    /**
     * 判断变量名是否合法
     *
     * @param name 变量名
     * @return
     */
    public boolean isVariableName(String name) {
        if (name.length() == 0) { // 变量名不能为空
            return false;
        }

        if (StringUtils.isBlank(StringUtils.replaceAll(name, "_", "")) || name.equals("$")) {
            return false;
        }

        if (name.length() > 0 && !StringUtils.isLetter(name.charAt(0)) && !StringUtils.inArray(name.charAt(0), '_', '$')) {
            return false;
        }

        // 变量名只能包含英文字母，数字，下划线
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (StringUtils.isNumber(c) || StringUtils.isLetter(c) || c == '_') {
                continue;
            } else {
                return false;
            }
        }

        return this.scriptKeyword != null && !this.scriptKeyword.contains(name);
    }

    /**
     * 判断字符串参数 name 是否是数据库关键字
     *
     * @param name 字符串
     * @return
     */
    public boolean isDatabaseKeyword(String name) {
        return this.databaseKeyword != null && this.databaseKeyword.contains(name);
    }

}
