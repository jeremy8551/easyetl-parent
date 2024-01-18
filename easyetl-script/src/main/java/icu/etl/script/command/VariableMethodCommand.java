package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariableMethod;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.method.VariableMethodRepository;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 变量方法命令
 */
public class VariableMethodCommand extends AbstractTraceCommand implements NohupCommandSupported {

    /** 变量名 */
    private String variableName;

    /** 变量方法名 */
    private String methodName;

    /** 变量方法执行结果 */
    private Object value;

    /** true表示布尔值取反 */
    private boolean reverse;

    /** 变量方法集合 */
    private VariableMethodRepository repository;

    /**
     * 初始化
     *
     * @param command      脚本命令
     * @param repository   变量方法仓库
     * @param variableName 变量名
     * @param methodName   变量方法
     * @param reverse      true表示取反
     */
    public VariableMethodCommand(UniversalCommandCompiler compiler, String command, VariableMethodRepository repository, String variableName, String methodName, boolean reverse) {
        super(compiler, command);
        this.repository = repository;
        this.variableName = variableName;
        this.methodName = methodName;
        this.reverse = reverse;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        this.value = null;

        UniversalScriptAnalysis analysis = session.getAnalysis();
        String variableName = analysis.replaceShellVariable(session, context, this.variableName, true, true, true, false);
        String methodStr = analysis.trim(analysis.replaceShellVariable(session, context, this.methodName, false, true, true, false), 2, 3, '.');

        try {
            Object value = context.getAttribute(variableName);
            session.addMethodVariable(variableName, value);

            // 提取变量方法名
            String name = VariableMethodCommand.parseName(methodStr); // 方法名的前缀: ls, [
            if (name == null) {
                stderr.println(ResourcesUtils.getMessage("script.message.stderr125", name));
                return UniversalScriptCommand.COMMAND_ERROR;
            }

            // 根据方法名查找对应的变量方法
            UniversalScriptVariableMethod repository = this.repository.get(name);
            if (repository == null) {
                stderr.println(ResourcesUtils.getMessage("script.message.stderr125", methodStr));
                return UniversalScriptCommand.COMMAND_ERROR;
            } else {
                int exitcode = repository.execute(session, context, stdout, stderr, analysis, variableName, methodStr);
                this.value = repository.value();

                if (this.reverse) {
                    if (this.value instanceof Boolean) {
                        boolean b = ((Boolean) this.value).booleanValue();
                        this.value = !b;
                        return b ? UniversalScriptCommand.VARIABLE_METHOD_ERROR : 0;
                    } else {
                        stderr.println(ResourcesUtils.getMessage("script.message.stderr068", this.getScript()));
                        return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
                    }
                } else {
                    return exitcode;
                }
            }
        } finally {
            session.removeMethodVariable(variableName);
        }
    }

    public void terminate() throws Exception {
    }

    public boolean enableNohup() {
        return true;
    }

    /**
     * 变量方法的返回值
     *
     * @return
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * 返回变量方法名
     *
     * @param method method, e.g: <br>
     *               [index] <br>
     *               .trim() <Br>
     *               substr()
     * @return
     */
    public static String parseName(String method) {
        if (method.startsWith("[")) {
            return "[";
        } else {
            int index = method.indexOf('(');
            if (index == -1) {
                return null;
            } else {
                String methodName = method.substring(0, index);
                if (methodName.length() > 0 && methodName.charAt(0) == '.') {
                    return StringUtils.ltrimBlank(methodName, '.');
                } else {
                    return methodName;
                }
            }
        }
    }

}
