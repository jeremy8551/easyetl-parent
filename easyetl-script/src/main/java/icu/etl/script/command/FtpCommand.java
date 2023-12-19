package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import icu.etl.expression.LoginExpression;
import icu.etl.os.OSFtpCommand;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.UniversalScriptInputStream;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.FtpList;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 建立 ftp 客户端 <br>
 * <br>
 * ftp username@serverHost:port?password=password; <br>
 * supported ftp command: <br>
 * exists filepath <br>
 * isfile filepath <br>
 * isDir filepath <br>
 * mkdir filepath <br>
 * cd filepath <br>
 * rm filepath <br>
 * pwd <br>
 * ls <br>
 * PUT file remotedir <br>
 * get remotefile localdir <br>
 */
public class FtpCommand extends AbstractFileCommand implements UniversalScriptInputStream {

    protected String host;
    protected String port;
    protected String username;
    protected String password;

    public FtpCommand(UniversalCommandCompiler compiler, String command, String host, String port, String username, String password) {
        super(compiler, command);
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void read(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, Reader in) throws IOException {
        if (!analysis.isBlankline(this.host) || !analysis.isBlankline(this.port) || !analysis.isBlankline(this.username) || !analysis.isBlankline(this.password)) {
            throw new UniversalScriptException(ResourcesUtils.getScriptStderrMessage(14, this.command, "ftp", "ftp " + username + "@" + host + ":" + port + "?password=" + password));
        }

        String expression = StringUtils.trimBlank(IO.read(in, new StringBuilder("ftp ")));
        LoginExpression expr = new LoginExpression(analysis, expression);
        this.host = expr.getLoginHost();
        this.port = expr.getLoginPort();
        this.username = expr.getLoginUsername();
        this.password = expr.getLoginPassword();
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        if (session.isEchoEnable() || forceStdout) {
            stdout.println("ftp " + this.username + "@" + this.host + ":" + this.port + "?password=" + this.password);
        }

        OSFtpCommand ftp = context.getContainer().getBean(OSFtpCommand.class, "ftp");
        if (!ftp.connect(this.host, Integer.parseInt(this.port), this.username, this.password)) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(38, "ftp"));
            return UniversalScriptCommand.COMMAND_ERROR;
        } else {
            FtpList.get(context).add(ftp);
            return 0;
        }
    }

    public void terminate() throws Exception {
    }

}
