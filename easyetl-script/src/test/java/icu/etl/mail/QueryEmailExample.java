package icu.etl.mail;

import java.util.Properties;

import icu.apache.mail.BodyPart;
import icu.apache.mail.Folder;
import icu.apache.mail.Message;
import icu.apache.mail.MessagingException;
import icu.apache.mail.Multipart;
import icu.apache.mail.Part;
import icu.apache.mail.Session;
import icu.apache.mail.Store;
import icu.apache.mail.internet.MimeMultipart;
import icu.apache.mail.internet.MimeUtility;
import icu.etl.mail.ApacheEmailCommand;
import icu.etl.util.Dates;
import icu.etl.util.StringUtils;

public class QueryEmailExample {
    final static String USER = "etl"; // 用户名
    final static String PASSWORD = "xxx"; // 密码
    public final static String MAIL_SERVER_HOST = "mail.foxmail.com"; // 邮箱服务器
    public final static String TYPE_HTML = "text/html;charset=UTF-8"; // 文本内容类型
    public final static String MAIL_FROM = "[email protected]"; // 发件人
    public final static String MAIL_TO = "[email protected]"; // 收件人
    public final static String MAIL_CC = "[email protected]"; // 抄送人
    public final static String MAIL_BCC = "[email protected]"; // 密送人

    public static void main(String[] args) throws Exception {
        // 创建一个有具体连接信息的Properties对象
        Properties prop = new Properties();
        prop.setProperty("mail.debug", "false");
        prop.setProperty("mail.store.protocol", "pop3");
//		prop.setProperty("mail.pop3.host", MAIL_SERVER_HOST);
        // 1、创建session
        Session session = Session.getInstance(prop);
        // 2、通过session得到Store对象
        Store store = session.getStore();
        // 3、连上邮件服务器
        store.connect(MAIL_SERVER_HOST, USER, PASSWORD);

        System.out.println("fullname: " + store.getDefaultFolder().getName());

        Folder fr = store.getDefaultFolder();
        System.out.println("DefaultFolder: " + fr.getName() + " " + fr.getFullName());
        listFolder(fr);

        Folder[] fs = store.getPersonalNamespaces();
        for (Folder f : fs) {
            System.out.println("PersonalNamespaces: " + f.getName() + " " + f.getFullName());
            listFolder(f);
        }

        Folder[] ss = store.getSharedNamespaces();
        for (Folder f : ss) {
            System.out.println("SharedNamespaces: " + f.getName());
            Folder[] files = f.list();
            for (Folder cf : files) {
                System.out.println("SharedNamespaces files: " + cf.getName());
            }
        }

        // 4、获得邮箱内的邮件夹
        Folder folder = store.getFolder("inbox");
        folder.open(Folder.READ_ONLY);

        Message[] messages = folder.getMessages();
        for (int i = 0; i < 1; i++) {
            Message msg = messages[i];

            System.out.println("第 " + (i + 1) + " 封邮件的主题：" + msg.getSubject());
            System.out.println("第 " + (i + 1) + " 封邮件的发件人地址：" + StringUtils.toString(ApacheEmailCommand.toString(msg.getFrom())));
            System.out.println("第 " + (i + 1) + " 封邮件的收件人地址：" + StringUtils.toString(ApacheEmailCommand.toString(msg.getAllRecipients())));
            System.out.println("第 " + (i + 1) + " 封邮件的发送时间：" + Dates.format19(msg.getSentDate()));
            System.out.println("第 " + (i + 1) + " 封邮件的接收时间：" + Dates.format19(msg.getReceivedDate()));

            MimeMultipart content = (MimeMultipart) msg.getContent();

            for (int j = 0; j < content.getCount(); j++) {
                BodyPart bp = content.getBodyPart(j);

                // 如果该BodyPart对象包含附件，则应该解析出来
                if (bp.getDisposition() != null) {
                    String filename = bp.getFileName();
                    if (filename.startsWith("=?")) {
                        filename = MimeUtility.decodeText(filename);
                    }

                    // 生成打开附件的超链接
                    System.out.println("附件：" + filename);
                    continue;
                }

//				System.out.println("[" + bp.getContentType() + "]");
                StringBuilder ct = getMailContent(bp);
                if (ct.length() > 0) {
                    System.out.println("第 " + (i + 1) + " 封邮件内容2：" + ct);
                }
            }
        }
        folder.close(false);
        store.close();
    }

    public static void listFolder(Folder folder) throws MessagingException {
        if (folder.exists()) {
            try {
                Folder[] files = folder.list();
                for (Folder cf : files) {
                    System.out.println("PersonalNamespaces files: " + cf.getName());
                    listFolder(cf);
                }
            } catch (Exception e) {
                String msg = StringUtils.toString(e);
                if (msg.indexOf("not a directory") == -1) {
                    throw new MessagingException(msg, e);
                }
            }
        }
    }

    /**
     * 解析邮件，把得到的邮件内容保存到一个StringBuffer对象中，解析邮件 主要是根据MimeType类型的不同执行不同的操作，一步一步的解析
     */
    public static StringBuilder getMailContent(Part part) throws Exception {
        StringBuilder bodytext = new StringBuilder();
        String contenttype = part.getContentType();
        int nameindex = contenttype.indexOf("name");
        boolean conname = false;
        if (nameindex != -1)
            conname = true;
//		System.out.println("CONTENTTYPE: " + contenttype);
        if (part.isMimeType("text/plain") && !conname) {
//			System.out.println("text " + part.getContent() + "]");
            bodytext.append((String) part.getContent());
        } else if (part.isMimeType("text/html") && !conname) {
//			System.out.println("html " + part.getContent() + "]");
            bodytext.append((String) part.getContent());
        } else if (part.isMimeType("multipart/*")) {
//			System.out.println("multipart");
            Multipart multipart = (Multipart) part.getContent();
            int counts = multipart.getCount();
            for (int i = 0; i < counts; i++) {
                bodytext.append(getMailContent(multipart.getBodyPart(i)));
            }
        } else if (part.isMimeType("message/rfc822")) {
            bodytext.append(getMailContent((Part) part.getContent()));
        } else {
        }
        return bodytext;
    }

}
