package icu.etl.script;

import java.text.Format;

/**
 * 脚本引擎中使用的类型转换器
 *
 * @author jeremy8551@qq.com
 */
public abstract class UniversalScriptFormatter extends Format {
    private final static long serialVersionUID = 1L;

    /**
     * 将 JDBC 参数 object 转为脚本引擎内部类型
     *
     * @param session 会话信息
     * @param context 脚本引擎上下文信息
     * @param object  Jdbc参数对象
     * @return 对象
     * @throws Exception 转换错误
     */
    public abstract Object formatJdbcParameter(UniversalScriptSession session, UniversalScriptContext context, Object object) throws Exception;

}
