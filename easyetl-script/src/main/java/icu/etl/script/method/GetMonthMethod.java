package icu.etl.script.method;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import icu.etl.annotation.ScriptFunction;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.Dates;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptFunction(name = "getMonth")
public class GetMonthMethod extends AbstractMethod {

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptAnalysis analysis, String variableName, String methodHandle) throws Exception {
        if (methodHandle.charAt("getMonth".length()) != '(') {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr111", methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        int funcStart = "getMonth(".length();
        int funcEnd = analysis.indexOf(methodHandle, ")", funcStart, 2, 2);
        if (funcEnd == -1) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr112", methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        String parameters = methodHandle.substring(funcStart, funcEnd);
        if (StringUtils.isNotBlank(parameters)) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr113", methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        Object value = session.getMethodVariable(variableName);
        if (value == null) {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr127", variableName));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        } else if (value instanceof String) {
            String str = (String) value;
            Date date = Dates.parse(str);
            int month = Dates.getMonth(date);
            this.value = month;
            int next = funcEnd + 1;
            return this.executeNextMethod(session, context, stdout, stderr, analysis, variableName, methodHandle, this.value, next);
        } else if (value instanceof Date) {
            Date date = (Date) value;
            int month = Dates.getMonth(date);
            this.value = month;
            int next = funcEnd + 1;
            return this.executeNextMethod(session, context, stdout, stderr, analysis, variableName, methodHandle, this.value, next);
        } else {
            stderr.println(ResourcesUtils.getMessage("script.message.stderr124", value.getClass().getName(), methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }
    }

}
