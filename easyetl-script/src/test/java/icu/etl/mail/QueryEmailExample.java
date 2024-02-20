package icu.etl.mail;

import icu.etl.apache.mail.Folder;
import icu.etl.apache.mail.MessagingException;
import icu.etl.apache.mail.Multipart;
import icu.etl.apache.mail.Part;
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
