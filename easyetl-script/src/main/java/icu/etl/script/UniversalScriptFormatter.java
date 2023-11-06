package icu.etl.script;

import java.io.IOException;
import java.sql.SQLException;
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
     * @param context 脚本引擎上下文信息
     * @param object  Jdbc参数对象
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public abstract Object formatJdbcParameter(UniversalScriptContext context, Object object) throws IOException, SQLException;

}
