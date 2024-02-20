package icu.etl.script.method;

import icu.etl.annotation.ScriptFunction;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptFunction(name = "print")
public class PrintMethod extends AbstractMethod {

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptAnalysis analysis, String variableName, String methodHandle) throws Exception {
        if (methodHandle.charAt("print".length()) != '(') {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr111", methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        int begin = "print(".length();
        int end = analysis.indexOf(methodHandle, ")", begin, 2, 2);
        if (end == -1) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr112", methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        String parameters = methodHandle.substring(begin, end);
        if (StringUtils.isNotBlank(parameters)) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr113", methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        Object value = session.getMethodVariable(variableName);
        if (value == null) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr127", variableName));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        } else if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            stdout.println(StringUtils.join(array, " "));
            int next = end + 1;
            return this.executeNextMethod(session, context, stdout, stderr, analysis, variableName, methodHandle, array, next);
        } else {
            stdout.println(value);
            int next = end + 1;
            return this.executeNextMethod(session, context, stdout, stderr, analysis, variableName, methodHandle, value, next);
        }
    }

}
