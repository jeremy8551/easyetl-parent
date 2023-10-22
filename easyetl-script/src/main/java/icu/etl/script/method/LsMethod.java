package icu.etl.script.method;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import icu.etl.annotation.ScriptVariableFunction;
import icu.etl.os.linux.LinuxLocalOS;
import icu.etl.os.linux.Linuxs;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.CharTable;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@ScriptVariableFunction(name = "ls")
public class LsMethod extends AbstractMethod {

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptAnalysis analysis, String variableName, String methodHandle) throws IOException, SQLException {
        if (methodHandle.charAt("ls".length()) != '(') {
            stderr.println(ResourcesUtils.getScriptStderrMessage(111, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        int begin = "ls(".length();
        int end = analysis.indexOf(methodHandle, ")", begin, 2, 2);
        if (end == -1) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(112, methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }

        String parameters = methodHandle.substring(begin, end);
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
            File file = new File(FileUtils.replaceFolderSeparator(str));

            CharTable table = new CharTable();
            table.addTitle("filename");

            if (file.isDirectory()) {
                File[] files = FileUtils.array(file.listFiles());
                for (File f : files) {
                    if (LinuxLocalOS.KEY_FILENAMES.contains(file.getName())) {
                        continue;
                    } else {
                        table.addCell(Linuxs.toLongname(f));
                    }
                }
            } else {
                table.addCell(Linuxs.toLongname(file));
            }

            this.value = table.toSimpleShape().ltrim().toString();
            int next = end + 1;
            return this.executeNextMethod(session, context, stdout, stderr, analysis, variableName, methodHandle, this.value, next);
        } else {
            stderr.println(ResourcesUtils.getScriptStderrMessage(124, value.getClass().getName(), methodHandle));
            return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
        }
    }

}
