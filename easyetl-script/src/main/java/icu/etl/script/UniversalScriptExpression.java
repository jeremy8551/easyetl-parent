package icu.etl.script;

import icu.etl.expression.Expression;
import icu.etl.script.internal.ScriptExpressionParser;

/**
 * 脚本引擎表达式 <br>
 * <br>
 * 在表达式引擎的基础上支持：命令替换，变量替换，变量方法的执行，布尔表达式取反操作 <br>
 * <br>
 * name.length() + 1 <br>
 * name.substr(1, 3) + "_" + name.substr(2, 4) +" is " + name[2] <br>
 * <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-12-08
 */
public class UniversalScriptExpression extends Expression {

    /**
     * 初始化
     *
     * @param session 用户会话信息
     * @param context 脚本引擎上下文信息
     * @param stdout  标准信息输出接口
     * @param stderr  错误信息输出接口
     * @param str     表达式字符串
     */
    public UniversalScriptExpression(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, String str) {
        super(new ScriptExpressionParser(session, context, stdout, stderr), str);
    }
}

