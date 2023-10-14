package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptSessionFactory;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.command.feature.NohupCommandSupported;
import icu.etl.script.session.ScriptMainProcess;
import icu.etl.script.session.ScriptProcess;
import icu.etl.script.session.ScriptSubProcess;
import icu.etl.util.CharTable;
import icu.etl.util.Dates;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 打印后台命令
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-06-05
 */
public class PSCommand extends AbstractTraceCommand implements NohupCommandSupported {

    /** 0-表示显示后台进程 1-表示显示用户会话 */
    private int type;

    public PSCommand(UniversalCommandCompiler compiler, String command, int type) {
        super(compiler, command);
        this.type = type;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws IOException, SQLException {
        StringBuilder buf = new StringBuilder();
        if (this.type == 1) {
            buf.append(this.printAllSession(session).toShellShape());
        } else {
            buf.append(this.printAllProcess(session).toShellShape());
        }

        if (session.isEchoEnable() || forceStdout) {
            stdout.println(buf.toString());
        }
        return 0;
    }

    public CharTable printAllProcess(UniversalScriptSession session) {
        String[] titles = StringUtils.split(ResourcesUtils.getScriptStdoutMessage(48), ',');
        CharTable table = new CharTable();
        table.addTitle(titles[0]);
        table.addTitle(CharTable.ALIGN_RIGHT, titles[1]);
        table.addTitle(titles[2]);
        table.addTitle(titles[3]);
        table.addTitle(titles[4]);
        table.addTitle(titles[5]);
        table.addTitle(CharTable.ALIGN_RIGHT, titles[6]);
        table.addTitle(titles[7]);

//		table.addTitle("pid");
//		table.addTitle(CharTable.ALIGN_RIGHT, "row");
//		table.addTitle("alive");
//		table.addTitle("terminate");
//		table.addTitle("start");
//		table.addTitle("end");
//		table.addTitle(CharTable.ALIGN_RIGHT, "exitcode");
//		table.addTitle("command");

        // 打印主进程
        ScriptMainProcess mainProcess = session.getMainProcess();
        for (Iterator<UniversalScriptCommand> it = mainProcess.iterator(); it.hasNext(); ) {
            UniversalScriptCommand obj = it.next();
            table.addValue("0");
            table.addValue(session.getCompiler().getLineNumber());
            table.addValue(session.isAlive());
            table.addValue(session.isTerminate());
            table.addValue(Dates.format19(session.getCreateTime()));
            table.addValue("");
            table.addValue(mainProcess.getExitcode());
            table.addValue(obj.getScript());
        }

        // 打印子进程
        ScriptSubProcess subProcess = session.getSubProcess();
        for (Iterator<ScriptProcess> it = subProcess.iterator(); it.hasNext(); ) {
            ScriptProcess obj = it.next();
            table.addValue(obj.getPid());
            table.addValue(obj.getLineNumber());
            table.addValue(obj.isAlive());
            table.addValue(obj.isTerminate());
            table.addValue(Dates.format19(obj.getStartTime()));
            table.addValue(Dates.format19(obj.getEndTime()));
            table.addValue(obj.getExitcode());
            table.addValue(obj.getCommand().getScript());
        }

        return table.removeLeftBlank();
    }

    public CharTable printAllSession(UniversalScriptSession session) {
        String[] titles = StringUtils.split(ResourcesUtils.getScriptStdoutMessage(49), ',');
        CharTable table = new CharTable();
        table.addTitle(titles[0]);
        table.addTitle(titles[1]);
        table.addTitle(titles[2]);
        table.addTitle(titles[3]);
        table.addTitle(titles[4]);
        table.addTitle(titles[5]);
        table.addTitle("");

//		table.addTitle("id");
//		table.addTitle("parent");
//		table.addTitle("alive");
//		table.addTitle("terminate");
//		table.addTitle("start");
//		table.addTitle("end");
//		table.addTitle("");

        UniversalScriptSessionFactory sessionFactory = session.getSessionFactory();
        for (Iterator<String> it = new ArrayList(sessionFactory.getSessionIDs()).iterator(); it.hasNext(); ) {
            String id = it.next();
            UniversalScriptSession obj = sessionFactory.get(id);
            boolean self = id.equals(session.getId());
            table.addValue(obj.getId());
            table.addValue(obj.getParentID());
            table.addValue(obj.isAlive());
            table.addValue(obj.isTerminate());
            table.addValue(Dates.format19(obj.getCreateTime()));
            table.addValue(Dates.format19(obj.getEndTime()));
            table.addValue(self ? "*" : "");
        }

        return table.removeLeftBlank();
    }

    public void terminate() throws IOException, SQLException {
    }

    public boolean enableNohup() {
        return true;
    }

}
