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

@ScriptFunction(name = "format")
public class FormatMethod extends AbstractMethod {

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptAnalysis analysis, String variableName, String methodHandle) throws Exception {
        if (methodHandle.charAt("format".length()) != '(') {
            stderr.println(ResourcesUtils.getScriptStderrMessage(111, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        int begin = "format(".length();
        int end = analysis.indexOf(methodHandle, ")", begin, 2, 2);
        if (end == -1) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(112, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        String parameters = methodHandle.substring(begin, end);
        if (StringUtils.isBlank(parameters)) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(97, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        String pattern = analysis.unQuotation(analysis.trim(parameters, 0, 0));
        if (StringUtils.isBlank(pattern)) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(97, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        Object value = session.getMethodVariable(variableName);
        if (value == null) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(127, variableName));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        Date date = Dates.testParse(value);
        if (date == null) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(124, value.getClass().getName(), methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        } else {
            this.value = Dates.format(date, pattern);
            int next = end + 1;
            return this.executeNextMethod(session, context, stdout, stderr, analysis, variableName, methodHandle, this.value, next);
        }
    }

}
