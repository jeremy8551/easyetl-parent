package icu.etl.mail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.script.SimpleBindings;

import icu.etl.ioc.EasyBeanContext;
import icu.etl.script.WithDBRule;
import icu.etl.util.ArrayUtils;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore
public class ApacheEmailCommandTest {

    @Rule
    public WithDBRule rule = new WithDBRule();

    @Test
    public void test1() throws IOException {
        EasyBeanContext context = rule.getContext();
        SimpleBindings environment = rule.getEnvironment();

        ApacheEmailCommand cmd = new ApacheEmailCommand();
        cmd.setHost((String) environment.get("mail.host"));
        cmd.setUser((String) environment.get("mail.username"), (String) environment.get("mail.password"));
        cmd.setCharsetName((String) environment.get("mail.charset"));

        StringBuilder msg = new StringBuilder();
        msg.append("测试单统计信息如下：").append("\r\n\t");
        msg.append("1、测试单数量: 4").append("\r\n\t");
        msg.append("2、测试单数量：5").append("\r\n\t");
        msg.append("3、测试单成功数量：6").append("\r\n\t");
        msg.append("统计时间：1 ").append(Dates.currentTimeStamp());

        File txt = FileUtils.getTempDir("test", ApacheEmailCommandTest.class.getSimpleName(), "test测试.txt");
        FileUtils.write(txt, StringUtils.CHARSET, false, msg);

        File dir = FileUtils.getTempDir("test", ApacheEmailCommandTest.class.getSimpleName(), "目录12test");
        Assert.assertTrue(FileUtils.createDirectory(dir));
        File f1 = new File(dir, "test测试.txt");
        FileUtils.write(f1, StringUtils.CHARSET, false, msg);

        File txt1 = FileUtils.getTempDir("test", ApacheEmailCommandTest.class.getSimpleName(), "testsetest.txt");
        FileUtils.write(txt1, StringUtils.CHARSET, false, msg);

        cmd.drafts("imap", 0, true, "etl@foxmail.com", ArrayUtils.asList("410336929@qq.com"), "测试单统计信息-" + Dates.currentTimeStamp(), msg, new MailFile(context, txt1, "tt1.txt", "file"));
    }

    @Test
    public void test3() throws IOException {
        EasyBeanContext context = rule.getContext();
        SimpleBindings environment = rule.getEnvironment();

        ApacheEmailCommand cmd = new ApacheEmailCommand();
        cmd.setHost((String) environment.get("mail.host"));
        cmd.setUser((String) environment.get("mail.username"), (String) environment.get("mail.password"));
        cmd.setCharsetName((String) environment.get("mail.charset"));

        StringBuilder msg = new StringBuilder();
        msg.append("测试单统计信息如下：").append("\r\n\t");
        msg.append("1、测试单数量: 4").append("\r\n\t");
        msg.append("2、测试单数量：5").append("\r\n\t");
        msg.append("3、测试单成功数量：6").append("\r\n\t");
        msg.append("统计时间：1 ").append(Dates.currentTimeStamp());

        File txt = FileUtils.getTempDir("test", ApacheEmailCommandTest.class.getSimpleName(), "test测试.txt");
        FileUtils.write(txt, StringUtils.CHARSET, false, msg);

        File dir = FileUtils.getTempDir("test", ApacheEmailCommandTest.class.getSimpleName(), "目录12test");
        Assert.assertTrue(FileUtils.createDirectory(dir));
        File f1 = new File(dir, "test测试.txt");
        FileUtils.write(f1, StringUtils.CHARSET, false, msg);

        cmd.send(null, 0, true, "测试 etl@foxmail.com", ArrayUtils.asList("410336929@qq.com"), "测试单统计信息-" + Dates.currentTimeStamp(), msg, new MailFile(context, txt, "tt1.txt", "file"), new MailFile(context, txt, "tttt.txt", "file"), new MailFile(context, txt), new MailFile(context, dir));
    }

    @Test
    public void test2() {
        SimpleBindings environment = rule.getEnvironment();

        ApacheEmailCommand cmd = new ApacheEmailCommand();
        cmd.setHost((String) environment.get("mail.host"));
        cmd.setUser((String) environment.get("mail.username"), (String) environment.get("mail.password"));
        cmd.setCharsetName((String) environment.get("mail.charset"));

        List<Mail> it = cmd.search(null, 0, true, null, null);
        for (Mail mail : it) {
            System.out.println(mail);
            List<MailAttachment> attachments = mail.getAttachments();
            for (MailAttachment ma : attachments) {
                System.out.print("下载附件 " + ma.getName());
                File file = cmd.download(ma, null);
                if (file == null) {
                    System.out.println(" 失败！");
                } else {
                    System.out.println(" " + file.getAbsolutePath());
                }
            }
            System.out.println();
            System.out.println();
        }
    }
}
