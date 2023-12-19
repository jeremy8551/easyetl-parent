package icu.etl.script.command;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import icu.etl.annotation.ScriptCommand;
import icu.etl.expression.LoginExpression;
import icu.etl.expression.WordIterator;
import icu.etl.ioc.EasyContext;
import icu.etl.mail.MailFile;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptParser;
import icu.etl.script.UniversalScriptReader;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

/**
 * email send filepath of protocal modified by ssl title= attachments= sender= charset=UTF-8 to receivers by user@ip:port?password=
 *
 * @author jeremy8551@qq.com
 * @createtime 2022-01-12
 */
@ScriptCommand(name = "email", keywords = {"email"})
public class EmailSendCommandCompiler extends AbstractTraceCommandCompiler {

    public String read(UniversalScriptReader in, UniversalScriptAnalysis analysis) throws IOException {
        return in.readSinglelineScript();
    }

    public AbstractTraceCommand compile(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptParser parser, UniversalScriptAnalysis analysis, String orginalScript, String command) throws Exception {
        WordIterator it = analysis.parse(analysis.replaceShellVariable(session, context, command, false, true, true, false));
        it.assertNext("email");
        it.assertNext("send");
        String filepath = it.readUntil("of");
        String protocal = it.next();
        it.assertNext("modified");
        it.assertNext("by");

        CommandAttribute attrs = new CommandAttribute("ssl", "title:", "attach:", "sender:", "charset:");
        while (!it.isNext("to")) {
            String str = it.next();
            String[] array = StringUtils.splitProperty(str);
            if (array == null) {
                attrs.setAttribute(str, "");
            } else {
                attrs.setAttribute(array[0], array[1]);
            }
        }

        it.assertNext("to");
        String receivers = it.next();
        it.assertNext("by");
        String expr = it.next();
        it.assertOver();

        boolean ssl = attrs.contains("ssl"); // 是否使用安全连接
        String title = attrs.getAttribute("title"); // 标题
        String sender = attrs.getAttribute("sender"); // 发送地址
        String charsetName = attrs.getAttribute("charset"); // 邮件服务器字符集
        String content = FileUtils.readline(new File(filepath), charsetName, 0); // 正文
        EasyContext ioccxt = context.getFactory().getContext();

        // 附件
        String[] attaches = StringUtils.split(attrs.getAttribute("attach"), ',');
        MailFile[] mfs = new MailFile[attaches.length];
        for (int i = 0; i < attaches.length; i++) {
            mfs[i] = new MailFile(ioccxt, new File(attaches[i]));
        }

        // 接收地址
        List<String> reces = new ArrayList<String>();
        StringUtils.split(receivers, ',', reces);

        // 登陆表达式
        LoginExpression login = new LoginExpression(analysis, "email " + expr);
        String username = login.getLoginUsername();
        String password = login.getLoginPassword();
        int port = StringUtils.parseInt(login.getLoginPort(), -1); // 默认值 -1
        String host = login.getLoginHost();

        return new EmailSendCommand(this, orginalScript, host, username, password, charsetName, port, protocal, ssl, sender, reces, title, content, mfs);
    }

}
