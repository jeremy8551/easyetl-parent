package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import icu.etl.expression.DataUnitExpression;
import icu.etl.ioc.BeanFactory;
import icu.etl.os.OS;
import icu.etl.os.OSDisk;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.util.CharTable;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public class DfCommand extends AbstractTraceCommand implements NohupCommandSupported {

    public DfCommand(UniversalCommandCompiler compiler, String command) {
        super(compiler, command);
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        OS os = BeanFactory.get(OS.class);
        try {
            CharTable table = new CharTable();
            String[] titles = StringUtils.split(ResourcesUtils.getScriptStdoutMessage(3), ',');
            table.addTitle(titles[0]);
            table.addTitle(titles[1]);
            table.addTitle(titles[2]);
            table.addTitle(titles[3]);
            table.addTitle(titles[4]);
            table.addTitle(titles[5]);

            List<OSDisk> list = os.getOSDisk();
            for (OSDisk disk : list) {
                table.addValue(disk.getId());
                table.addValue(DataUnitExpression.toString(disk.total()));
                table.addValue(DataUnitExpression.toString(disk.free()));
                table.addValue(DataUnitExpression.toString(disk.used()));
                table.addValue(disk.getType());
                table.addValue(disk.getAmount());
            }

            if (session.isEchoEnable() || forceStdout) {
                stdout.println(table.removeLeftBlank().toShellShape());
            }
            return 0;
        } finally {
            os.close();
        }
    }

    public void terminate() throws IOException, SQLException {
    }

    public boolean enableNohup() {
        return true;
    }

}
