package icu.etl.mail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import icu.etl.TestEnv;
import icu.etl.ioc.BeanContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Test;

public class ApacheEmailCommandTest {

    @Test
    public void test1() throws IOException {
        BeanContext context = new BeanContext();
//		LogFactory.setLoggerLevel("info");
        ApacheEmailCommand cmd = new ApacheEmailCommand();
        cmd.setHost(TestEnv.getMailHost());
        cmd.setUser(TestEnv.getMailUsername(), TestEnv.getMailPassword());
        cmd.setCharsetName(TestEnv.getMailCharset());

        StringBuilder msg = new StringBuilder();
        msg.append("测试单统计信息如下：").append("\r\n\t");
        msg.append("1、测试单数量: 4").append("\r\n\t");
        msg.append("2、测试单数量：5").append("\r\n\t");
        msg.append("3、测试单成功数量：6").append("\r\n\t");
        msg.append("统计时间：1 ").append(Dates.currentTimeStamp());

        File txt = new File(FileUtils.getTempDir(ApacheEmailCommandTest.class), "test测试.txt");
        FileUtils.write(txt, StringUtils.CHARSET, false, msg);

        File dir = new File(FileUtils.getTempDir(ApacheEmailCommandTest.class), "目录12test");
        Ensure.isTrue(FileUtils.createDirectory(dir), dir);
        File f1 = new File(dir, "test测试.txt");
        FileUtils.write(f1, StringUtils.CHARSET, false, msg);

        File txt1 = new File(FileUtils.getTempDir(ApacheEmailCommandTest.class), "testsetest.txt");
        FileUtils.write(txt1, StringUtils.CHARSET, false, msg);

        cmd.drafts("imap", 0, true, "etl@foxmail.com", ArrayUtils.asList("410336929@qq.com"), "测试单统计信息-" + Dates.currentTimeStamp(), msg, new MailFile(context, txt1, "tt1.txt", "file"));
    }

    //	@Test
    public void test3() throws IOException {
        BeanContext context = new BeanContext();
//		LogFactory.setLoggerLevel("info");
        ApacheEmailCommand cmd = new ApacheEmailCommand();
        cmd.setHost(TestEnv.getMailHost());
        cmd.setUser(TestEnv.getMailUsername(), TestEnv.getMailPassword());
        cmd.setCharsetName(TestEnv.getMailCharset());

        StringBuilder msg = new StringBuilder();
        msg.append("测试单统计信息如下：").append("\r\n\t");
        msg.append("1、测试单数量: 4").append("\r\n\t");
        msg.append("2、测试单数量：5").append("\r\n\t");
        msg.append("3、测试单成功数量：6").append("\r\n\t");
        msg.append("统计时间：1 ").append(Dates.currentTimeStamp());

        File txt = new File(FileUtils.getTempDir(ApacheEmailCommandTest.class), "test测试.txt");
        FileUtils.write(txt, StringUtils.CHARSET, false, msg);

        File dir = new File(FileUtils.getTempDir(ApacheEmailCommandTest.class), "目录12test");
        Ensure.isTrue(FileUtils.createDirectory(dir), dir);
        File f1 = new File(dir, "test测试.txt");
        FileUtils.write(f1, StringUtils.CHARSET, false, msg);

        cmd.send(null, 0, true, "测试 etl@foxmail.com", ArrayUtils.asList("410336929@qq.com"), "测试单统计信息-" + Dates.currentTimeStamp(), msg, new MailFile(context, txt, "tt1.txt", "file"), new MailFile(context, txt, "tttt.txt", "file"), new MailFile(context, txt), new MailFile(context, dir));
    }

    //	@Test
    public void test2() {
        ApacheEmailCommand cmd = new ApacheEmailCommand();
        cmd.setHost(TestEnv.getMailHost());
        cmd.setUser(TestEnv.getMailUsername(), TestEnv.getMailPassword());
        cmd.setCharsetName(TestEnv.getMailCharset());

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
