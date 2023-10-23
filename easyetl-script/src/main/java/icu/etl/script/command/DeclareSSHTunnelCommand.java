package icu.etl.script.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import icu.etl.expression.LoginExpression;
import icu.etl.os.OSSecureShellCommand;
import icu.etl.printer.OutputStreamPrinter;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptChecker;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.internal.SSHTunnelMap;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 建立SSH端口转发协议 <br>
 * <br>
 * declare {name} SSH tunnel use proxy {proxyUsername}@{proxyHost}:{proxySSHPort}?password={proxyPassword} connect to {remoteHost}:{remoteSSHPort}; <br>
 */
public class DeclareSSHTunnelCommand extends AbstractCommand {

    /** 端口转发协议名 */
    private String name;

    /** 代理服务器配置: username@host:port?password=password */
    private String proxy;

    /** 目标服务器配置 remote:port */
    private String remote;

    public DeclareSSHTunnelCommand(UniversalCommandCompiler compiler, String command, String name, String proxy, String remote) {
        super(compiler, command);
        this.name = name;
        this.proxy = proxy;
        this.remote = remote;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout) throws IOException, SQLException {
        UniversalScriptAnalysis analysis = session.getAnalysis();

        // 解析代理服务器配置
        LoginExpression expr = new LoginExpression(analysis, "ssh " + this.proxy);
        String host = expr.getLoginHost();
        int port = Integer.parseInt(expr.getLoginPort());
        String username = expr.getLoginUsername();
        String password = expr.getLoginPassword();
        List<String> portforward = analysis.split(analysis.replaceVariable(session, context, analysis.replaceVariable(session, context, this.remote, false), false), ':');

        // 连接代理服务器
        OSSecureShellCommand client = context.getFactory().getContext().get(OSSecureShellCommand.class);
        if (!client.connect(host, port, username, password)) {
            stderr.println(ResourcesUtils.getScriptStderrMessage(38, "ssh2"));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        client.setStdout(new OutputStreamPrinter(stdout, client.getCharsetName()));
        client.setStderr(new OutputStreamPrinter(stderr, client.getCharsetName()));

        // 解析本地端口号
        String localStr = portforward.get(0);
        boolean variableName = false;
        int inputPort = 0;
        UniversalScriptChecker checker = context.getChecker();
        if (checker.isVariableName(localStr)) { // 本地端口位置是变量名
            inputPort = 0; // 随机分配
            variableName = true;
        } else if (StringUtils.isNumber(localStr) && //
                ((inputPort = Integer.parseInt(localStr)) == 0 || (inputPort >= 1024 && inputPort <= 65535)) //
        ) { // 端口为0 或 1024-65535
            inputPort = Integer.parseInt(localStr);
        } else {
            stderr.println(ResourcesUtils.getScriptStderrMessage(70, this.command, localStr));
            return UniversalScriptCommand.COMMAND_ERROR;
        }

        // 建立本地端口转发隧道
        int localPort = client.localPortForward(inputPort, portforward.get(1), Integer.parseInt(portforward.get(2)));
        if (variableName) {
            context.addLocalVariable(localStr, localPort);
        }

        if (session.isEchoEnable() || forceStdout) {
            stdout.println(localPort);
        }

        SSHTunnelMap.get(context).add(this.name, client);
        return 0;
    }

    public void terminate() throws IOException, SQLException {
    }

}
