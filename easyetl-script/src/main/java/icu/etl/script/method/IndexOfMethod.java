package icu.etl.script.method;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import icu.etl.annotation.ScriptFunction;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * indexOf('str', from) <br>
 * indexOf('str')
 *
 * @author jeremy8551@qq.com
 */
@ScriptFunction(name = "indexOf")
public class IndexOfMethod extends AbstractMethod {

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptAnalysis analysis, String variableName, String methodHandle) throws Exception {
        if (methodHandle.charAt("indexOf".length()) != '(') {
            stderr.println(ResourcesUtils.getScriptStderrMessage(111, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        int funcStart = "indexOf(".length();
        int funcEnd = analysis.indexOf(methodHandle, ")", funcStart, 2, 2);
        if (funcEnd == -1) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(112, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        String params = methodHandle.substring(funcStart, funcEnd); // 'string', 'delimiter', 'escape'
        List<String> parameters = analysis.split(params, analysis.getSegment());

        if (parameters.size() != 1 && parameters.size() != 2) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(110, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        String str = null;
        int from = -1;

        if (parameters.size() == 1) {
            str = analysis.replaceShellVariable(session, context, parameters.get(0), true, true, true, false);
            if (str.length() == 0) {
                stderr.println(ResourcesUtils.getScriptStderrMessage(129, methodHandle));
                return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
            }
        } else if (parameters.size() == 2) {
            str = analysis.replaceShellVariable(session, context, parameters.get(0), true, true, true, false);
            from = StringUtils.parseInt(analysis.replaceShellVariable(session, context, parameters.get(1), true, true, true, false), -1);
            if (from < 0) {
                stderr.println(ResourcesUtils.getScriptStderrMessage(123, methodHandle));
                return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
            }
        }

        Object object = session.getMethodVariable(variableName);
        if (object == null) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(127, variableName));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        } else if (object instanceof String) {
            String value = (String) object;

            int index = -1;
            if (from >= value.length()) {
                index = -1;
            } else if (from == -1) {
                index = value.indexOf(str);
            } else {
                index = value.indexOf(str, from);
            }

            this.value = index;
            int next = funcEnd + 1;
            return this.executeNextMethod(session, context, stdout, stderr, analysis, variableName, methodHandle, index, next);
        } else if (object.getClass().isArray()) {
            Object[] array = (Object[]) object;

            int index = -1;
            if (from >= array.length) {
                index = -1;
            } else {
                for (int i = (from == -1 ? 0 : from); i < array.length; i++) {
                    Object obj = array[i];
                    if (str.equals(context.getFormatter().format(obj))) {
                        index = i;
                        break;
                    }
                }
            }

            this.value = index;
            int next = funcEnd + 1;
            return this.executeNextMethod(session, context, stdout, stderr, analysis, variableName, methodHandle, index, next);
        } else {
            stderr.println(ResourcesUtils.getScriptStderrMessage(124, object.getClass().getName(), methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }
    }

}
