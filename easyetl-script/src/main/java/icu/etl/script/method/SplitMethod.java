package icu.etl.script.method;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import icu.etl.annotation.ScriptFunction;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.ArrayUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptFunction(name = "split")
public class SplitMethod extends AbstractMethod {

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptAnalysis analysis, String variableName, String methodHandle) throws Exception {
        if (methodHandle.charAt("split".length()) != '(') {
            stderr.println(ResourcesUtils.getScriptStderrMessage(111, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        int funcStart = "split(".length();
        int funcEnd = analysis.indexOf(methodHandle, ")", funcStart, 2, 2);
        if (funcEnd == -1) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(112, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        String params = methodHandle.substring(funcStart, funcEnd); // 'string', 'delimiter', 'escape'
        List<String> parameters = analysis.split(params, analysis.getSegment());

        if (parameters.size() != 0 && parameters.size() != 1 && parameters.size() != 2) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(119, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        String delimiter = null, escape = null;

        if (StringUtils.isBlank(params)) {
            parameters.clear();
        } else if (parameters.size() == 1) {
            delimiter = analysis.replaceShellVariable(session, context, parameters.get(0), true, true, true, false);
            if (StringUtils.isBlank(delimiter)) {
                stderr.println(ResourcesUtils.getScriptStderrMessage(120, methodHandle));
                return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
            }
        } else if (parameters.size() == 2) {
            delimiter = analysis.replaceShellVariable(session, context, parameters.get(0), true, true, true, false);
            escape = analysis.unQuotation(analysis.replaceShellVariable(session, context, parameters.get(1), false, true, true, false));
            escape = analysis.unescapeString(escape); // 需要对字符串进行反向转义

            if (StringUtils.isBlank(escape) || escape.length() != 1) {
                stderr.println(ResourcesUtils.getScriptStderrMessage(121, methodHandle));
                return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
            }
        }

        Object object = session.getMethodVariable(variableName);
        if (object == null) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(127, variableName));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        } else if (object instanceof String) {
            String value = (String) object;
            String[] array = null; // string array
            if (parameters.size() == 0) {
                array = StringUtils.splitByBlank(value);
                if (value.length() > 0 //
                        && Character.isWhitespace(value.charAt(value.length() - 1)) //
                        && !"".equals(ArrayUtils.lastElement(array)) //
                ) {
                    String[] newarray = new String[array.length + 1];
                    Arrays.fill(newarray, "");
                    System.arraycopy(array, 0, newarray, 0, array.length);
                    array = newarray;
                }
            } else if (escape == null) {
                array = StringUtils.split(value, delimiter);
            } else {
                array = StringUtils.split(value, delimiter, escape.charAt(0));
            }

            this.value = array;
            int next = funcEnd + 1;
            return this.executeNextMethod(session, context, stdout, stderr, analysis, variableName, methodHandle, array, next);
        } else {
            stderr.println(ResourcesUtils.getScriptStderrMessage(124, object.getClass().getName(), methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }
    }

}
