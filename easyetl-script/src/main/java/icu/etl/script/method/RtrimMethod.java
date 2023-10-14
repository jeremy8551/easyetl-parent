package icu.etl.script.method;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.annotation.ScriptVariableFunction;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptVariableFunction(name = "rtrim")
public class RtrimMethod extends AbstractMethod {

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptAnalysis analysis, String variableName, String methodHandle) throws IOException, SQLException {
        if (methodHandle.charAt("rtrim".length()) != '(') {
            stderr.println(ResourcesUtils.getScriptStderrMessage(111, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        int funcStart = "rtrim(".length();
        int funcEnd = analysis.indexOf(methodHandle, ")", funcStart, 2, 2);
        if (funcEnd == -1) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(112, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        String parameters = methodHandle.substring(funcStart, funcEnd);
        if (StringUtils.isNotBlank(parameters)) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(113, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        Object value = session.getMethodVariable(variableName);
        if (value == null) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(127, variableName));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        } else if (value instanceof String) {
            String str = (String) value;
            this.value = StringUtils.rtrimBlank(str);
            int next = funcEnd + 1;
            return this.executeNextMethod(session, context, stdout, stderr, analysis, variableName, methodHandle, this.value, next);
        } else if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            Object[] newarray = new Object[array.length];
            for (int i = 0; i < newarray.length; i++) {
                Object element = newarray[i];
                if (element instanceof String) {
                    newarray[i] = StringUtils.rtrimBlank(element);
                } else {
                    newarray[i] = element;
                }
            }

            this.value = newarray;
            int next = funcEnd + 1;
            return this.executeNextMethod(session, context, stdout, stderr, analysis, variableName, methodHandle, newarray, next);
        } else {
            stderr.println(ResourcesUtils.getScriptStderrMessage(124, value.getClass().getName(), methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }
    }

}
