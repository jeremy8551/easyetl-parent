package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import icu.etl.mail.MailCommand;
import icu.etl.mail.MailFile;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;

/**
 * 发送邮件
 *
 * @author jeremy8551@qq.com
 * @createtime 2022-01-12
 */
public class EmailSendCommand extends AbstractTraceCommand {

    private String host; // 邮件服务器地址
    private String username; // 邮件服务器登陆用户
    private String password; // 密码
    private String charset; // 邮件服务器字符集编码
    private int port; // 邮件服务器端口

    private String protocal; // 发送邮件协议名
    private boolean ssl; // true表示使用ssl协议发送
    private String sender; // 发送地址
    private List<String> receivers; // 接收地址
    private String title; // 标题
    private String content; // 正文
    private MailFile[] attchments; // 附件

    public EmailSendCommand(UniversalCommandCompiler compiler, String script, String host, String username, String password, String charset, int port, String protocal, boolean ssl, String sender, List<String> receivers, String title, String content, MailFile[] attchments) {
        super(compiler, script);
        this.host = host;
        this.username = username;
        this.password = password;
        this.charset = charset;
        this.port = port;
        this.protocal = protocal;
        this.ssl = ssl;
        this.sender = sender;
        this.receivers = receivers;
        this.title = title;
        this.content = content;
        this.attchments = attchments;
    }

    public int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, boolean forceStdout, File outfile, File errfile) throws Exception {
        MailCommand mail = context.getContainer().getBean(MailCommand.class);
        mail.setHost(this.host);
        mail.setUser(this.username, this.password);
        mail.setCharsetName(this.charset);
        String number = mail.send(this.protocal, this.port, this.ssl, this.sender, this.receivers, this.title, this.content, this.attchments);
        if (session.isEchoEnable() || forceStdout) {
            stdout.println(ResourcesUtils.getScriptStdoutMessage(46) + FileUtils.lineSeparator + number);
        }
        return 0;
    }

    public void terminate() throws Exception {
    }

}
