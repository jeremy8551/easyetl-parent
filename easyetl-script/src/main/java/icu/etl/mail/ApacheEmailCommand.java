package icu.etl.mail;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import icu.apache.mail.Address;
import icu.apache.mail.BodyPart;
import icu.apache.mail.Flags;
import icu.apache.mail.Folder;
import icu.apache.mail.Message;
import icu.apache.mail.MessagingException;
import icu.apache.mail.Multipart;
import icu.apache.mail.Part;
import icu.apache.mail.Session;
import icu.apache.mail.Store;
import icu.apache.mail.activation.DataHandler;
import icu.apache.mail.activation.FileDataSource;
import icu.apache.mail.activation.registries.MailcapFile;
import icu.apache.mail.activation.registries.MimeTypeFile;
import icu.apache.mail.activation.viewers.ImageViewer;
import icu.apache.mail.activation.viewers.TextEditor;
import icu.apache.mail.activation.viewers.TextViewer;
import icu.apache.mail.common.EmailAttachment;
import icu.apache.mail.common.MultiPartEmail;
import icu.apache.mail.common.SimpleEmail;
import icu.apache.mail.handlers.message_rfc822;
import icu.apache.mail.handlers.multipart_mixed;
import icu.apache.mail.handlers.text_html;
import icu.apache.mail.handlers.text_plain;
import icu.apache.mail.handlers.text_xml;
import icu.apache.mail.internet.InternetAddress;
import icu.apache.mail.internet.MimeBodyPart;
import icu.apache.mail.internet.MimeMessage;
import icu.apache.mail.internet.MimeMultipart;
import icu.apache.mail.internet.MimeUtility;
import icu.apache.mail.search.SearchTerm;
import icu.etl.collection.ByteBuffer;
import icu.etl.expression.GPatternExpression;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ArrayUtils;
import icu.etl.util.CharsetName;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 邮件功能的接口实现类 <br>
 * <p>
 * SMTP:邮件发送协议 ssl对应端口465 非ssl对应端口25
 * <p>
 * IMAP:收邮件协议 ssl对应端口993 非ssl对应端口143
 *
 * @author jeremy8551@qq.com
 */
public class ApacheEmailCommand implements MailCommand {
    private final static Log log = LogFactory.getLog(ApacheEmailCommand.class);

    /**
     * Load mailcap.default
     */
    public static MailcapFile loadMailcapDefault() {
        try {
            ByteBuffer bytes = new ByteBuffer();
            bytes.setCharsetName(CharsetName.ISO_8859_1);
            bytes.append("image/gif;;		x-java-view=").append(ImageViewer.class.getName()).append(FileUtils.lineSeparatorUnix);
            bytes.append("image/jpeg;;		x-java-view=").append(ImageViewer.class.getName()).append(FileUtils.lineSeparatorUnix);
            bytes.append("text/*;;		x-java-view=").append(TextViewer.class.getName()).append(FileUtils.lineSeparatorUnix);
            bytes.append("text/*;;		x-java-edit=").append(TextEditor.class.getName()).append(FileUtils.lineSeparatorUnix);
            return new MailcapFile(bytes.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("set mailcap.default fail!", e);
        }
    }

    /**
     * mimetypes.default
     */
    public static MimeTypeFile loadMimetypesDefault() {
        try {
            ByteBuffer bytes = new ByteBuffer();
            bytes.setCharsetName(CharsetName.ISO_8859_1);
            bytes.append("text/html		html htm HTML HTM").append(FileUtils.lineSeparatorUnix);
            bytes.append("text/plain		txt text TXT TEXT").append(FileUtils.lineSeparatorUnix);
            bytes.append("image/gif		gif GIF").append(FileUtils.lineSeparatorUnix);
            bytes.append("image/ief		ief").append(FileUtils.lineSeparatorUnix);
            bytes.append("image/jpeg		jpeg jpg jpe JPG").append(FileUtils.lineSeparatorUnix);
            bytes.append("image/tiff		tiff tif").append(FileUtils.lineSeparatorUnix);
            bytes.append("image/png		png PNG").append(FileUtils.lineSeparatorUnix);
            bytes.append("image/x-xwindowdump	xwd").append(FileUtils.lineSeparatorUnix);
            bytes.append("application/postscript	ai eps ps").append(FileUtils.lineSeparatorUnix);
            bytes.append("application/rtf		rtf").append(FileUtils.lineSeparatorUnix);
            bytes.append("application/x-tex	tex").append(FileUtils.lineSeparatorUnix);
            bytes.append("application/x-texinfo	texinfo texi").append(FileUtils.lineSeparatorUnix);
            bytes.append("application/x-troff	t tr roff").append(FileUtils.lineSeparatorUnix);
            bytes.append("audio/basic		au").append(FileUtils.lineSeparatorUnix);
            bytes.append("audio/midi		midi mid").append(FileUtils.lineSeparatorUnix);
            bytes.append("audio/x-aifc		aifc").append(FileUtils.lineSeparatorUnix);
            bytes.append("audio/x-aiff            aif aiff").append(FileUtils.lineSeparatorUnix);
            bytes.append("audio/x-mpeg		mpeg mpg").append(FileUtils.lineSeparatorUnix);
            bytes.append("audio/x-wav             wav").append(FileUtils.lineSeparatorUnix);
            bytes.append("video/mpeg		mpeg mpg mpe").append(FileUtils.lineSeparatorUnix);
            bytes.append("video/quicktime		qt mov").append(FileUtils.lineSeparatorUnix);
            bytes.append("video/x-msvideo		avi").append(FileUtils.lineSeparatorUnix);
            return new MimeTypeFile(bytes.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("mimetypes.default", e);
        }
    }

    /**
     * Load mailcap
     */
    public static MailcapFile loadMailcap() {
        try {
            ByteBuffer bytes = new ByteBuffer();
            bytes.setCharsetName(CharsetName.ISO_8859_1);
            bytes.append("text/plain;;		x-java-content-handler=").append(text_plain.class.getName()).append(FileUtils.lineSeparatorUnix);
            bytes.append("text/html;;		x-java-content-handler=").append(text_html.class.getName()).append(FileUtils.lineSeparatorUnix);
            bytes.append("text/xml;;		x-java-content-handler=").append(text_xml.class.getName()).append(FileUtils.lineSeparatorUnix);
            bytes.append("multipart/*;;	x-java-content-handler=").append(multipart_mixed.class.getName()).append("; x-java-fallback-entry=true").append(FileUtils.lineSeparatorUnix);
            bytes.append("message/rfc822;;	x-java-content-handler=").append(message_rfc822.class.getName()).append(FileUtils.lineSeparatorUnix);
            bytes.append("").append(TextEditor.class.getName()).append(FileUtils.lineSeparatorUnix);
            return new MailcapFile(bytes.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("set mailcap fail!", e);
        }
    }

    public static String[] toString(Address[] from) throws UnsupportedEncodingException {
        String[] array = new String[from == null ? 0 : from.length];
        if (array.length > 0) {
            for (int j = 0; j < from.length; j++) {
                Address add = from[j];
                array[j] = MimeUtility.decodeText(add.toString());
            }
        }
        return array;
    }

    /**
     * &lt;name&gt; test@company.com <br>
     * <br>
     * The first place in the array is the name, the second place is the email address
     *
     * @param str
     * @return
     */
    public static String[] toAddress(String str) {
        str = StringUtils.trimBlank(str);
        int index = StringUtils.lastIndexOfBlank(str, -1);
        if (index == -1) {
            return new String[]{str, str};
        } else {
            String[] array = new String[2];
            array[0] = StringUtils.trimBlank(str.substring(0, index));
            array[1] = StringUtils.trimBlank(str.substring(index));

            array[0] = StringUtils.trim(array[0], '\'');
            array[0] = StringUtils.trim(array[0], '"');
            array[0] = StringUtils.ltrim(array[0], '<');
            array[0] = StringUtils.rtrim(array[0], '>');
            array[0] = StringUtils.trim(array[0], '"');
            array[0] = StringUtils.trim(array[0], '\'');

            array[1] = StringUtils.ltrim(array[1], '<');
            array[1] = StringUtils.rtrim(array[1], '>');
            return array;
        }
    }

    private String host;
    private String username;
    private String password;
    private String charsetName;

    public String getCharsetName() {
        return this.charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @param sender  收件人
     * @param title   主题
     * @param content 内容
     */
    public void drafts(String protocol, int port, boolean ssl, String sender, List<String> receivers, String title, CharSequence content, MailFile... attachments) {
        if (StringUtils.isBlank(protocol)) {
        }
        protocol = "imap";

        Store store = null;
        try {
            Properties config = new Properties();
            config.setProperty("mail.debug", String.valueOf(log.isDebugEnabled()));
            config.setProperty("mail.store.protocol", protocol);
            config.setProperty("mail." + protocol + ".host", host);
            config.setProperty("mail." + protocol + ".auth", "true");

            if (port > 0) {
                config.setProperty("mail." + protocol + ".port", String.valueOf(port));
            }
            config.setProperty("mail." + protocol + ".connectiontimeout", "100000");
            config.setProperty("mail." + protocol + ".timeout", "100000");

            Session session = Session.getInstance(config);
            store = session.getStore();
            store.connect(this.host, this.username, this.password);

            Folder folder = store.getFolder("Drafts");// 打开草稿箱
            folder.open(Folder.READ_WRITE);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(receivers.get(0)));
            message.setSubject(title);

            if (attachments.length == 0) {
                message.setText(content.toString());
            } else {
                MimeMultipart mm = new MimeMultipart("related");

                MimeBodyPart body = new MimeBodyPart();
                body.setContent(content.toString(), "text/plain;charset=" + StringUtils.CHARSET);
                mm.addBodyPart(body);

                for (MailFile attchment : attachments) {
                    if (attchment != null) {
                        System.out.println(attchment.getFile().getAbsolutePath());
                        System.out.println(attchment.getName());
                        System.out.println(attchment.getDescription());
                        System.out.println(attchment.getDisposition());

                        MimeBodyPart part = new MimeBodyPart();
                        FileDataSource fds = new FileDataSource(attchment.getFile());
                        DataHandler dh = new DataHandler(fds);
//						part.setDisposition(attchment.getDisposition());
                        part.setDataHandler(dh);
                        part.setFileName(fds.getName());
//						part.setFileName(MimeUtility.encodeText(fds.getName())); // 得到文件名同样至入BodyPart
//						part.setDescription(attchment.getDescription());

                        mm.addBodyPart(part);
                    }
                }

                mm.setSubType("mixed");
                message.setContent(mm);
            }

            message.setSentDate(new Date());
            message.setFlag(Flags.Flag.DRAFT, true);
            message.saveChanges();

            MimeMessage draftMessages[] = {message};
            folder.appendMessages(draftMessages);
            folder.close(false);
        } catch (Exception e) {
            throw new RuntimeException(ResourcesUtils.getMailMessage(6), e);
        } finally {
            IO.close(store);
        }
    }

    public String send(String protocol, int port, boolean ssl, String sender, List<String> receivers, String title, CharSequence content, MailFile... attachments) {
        if (StringUtils.isNotBlank(protocol)) {
            Ensure.isTrue("smtp".equalsIgnoreCase(StringUtils.trimBlank(protocol)), protocol, port, ssl, sender, receivers, title, content, attachments);
        }

        try {
            if (attachments.length == 0) {
                SimpleEmail mail = new SimpleEmail();
                mail.setDebug(log.isDebugEnabled());
                mail.setHostName(this.host);
                mail.setAuthentication(this.username, this.password);// 邮件服务器验证：用户名/密码
                mail.setSSL(ssl);
                mail.setCharset(StringUtils.defaultString(this.charsetName, CharsetName.UTF_8));// 必须放在前面，否则乱码
                if (ssl) {
                    mail.setSslSmtpPort(String.valueOf(port));
                } else {
                    mail.setSmtpPort(port);
                }

                for (String str : receivers) {
                    String[] array = toAddress(str);
                    mail.addTo(array[1], array[0]);
                }

                String[] array = toAddress(sender);
                mail.setFrom(array[1], array[0]);
                mail.setSubject(title);
                mail.setMsg(content.toString());

                String messageId = mail.send();
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMailMessage(1));
                }
                return messageId;
            } else {
                // Create the email message
                MultiPartEmail mail = new MultiPartEmail();
                mail.setDebug(log.isDebugEnabled());
                mail.setHostName(this.host);
                mail.setAuthentication(this.username, this.password);// 邮件服务器验证：用户名/密码
                mail.setSSL(ssl);
                mail.setCharset(StringUtils.defaultString(this.charsetName, CharsetName.UTF_8));// 必须放在前面，否则乱码

                for (String str : receivers) {
                    String[] array = toAddress(str);
                    mail.addTo(array[1], array[0]);
                }

                String[] array = toAddress(sender);
                mail.setFrom(array[1], array[0]);
                mail.setSubject(title);
                mail.setMsg(content.toString());

                // add the attachment
                for (MailFile file : attachments) {
                    if (file != null) {
                        EmailAttachment attachment = new EmailAttachment();
                        attachment.setPath(file.getPath());
                        attachment.setDisposition(file.getDisposition());
                        attachment.setDescription(file.getDescription());
                        attachment.setName(MimeUtility.encodeText(file.getName()));

                        if (log.isDebugEnabled()) {
                            log.debug(ResourcesUtils.getMailMessage(10, file.getPath(), file.getName()));
                        }

                        mail.attach(attachment);
                    }
                }

                // send the email
                String messageId = mail.send();
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMailMessage(1));
                }
                return messageId;
            }
        } catch (Exception e) {
            throw new RuntimeException(ResourcesUtils.getMailMessage(2), e);
        }
    }

    public List<Mail> search(String protocol, int port, boolean ssl, String name, SearchTerm term) {
        if (StringUtils.isBlank(protocol)) {
            protocol = "pop3";
        }

        Store store = null;
        try {
            Properties config = new Properties();
            config.setProperty("mail.debug", String.valueOf(log.isDebugEnabled()));
            config.setProperty("mail.store.protocol", protocol);
            if (port > 0) {
                config.setProperty("mail." + protocol + ".port", String.valueOf(port));
            }

            Session session = Session.getInstance(config);
            store = session.getStore();
            store.connect(this.host, this.username, this.password);

            MailFolderImpl folder = this.getFolder(store, name, term);
            folder.setProtocol(protocol);
            folder.setProtocolPort(port);
            folder.setSSL(true);
            return folder.getMails();
        } catch (Exception e) {
            throw new RuntimeException(ResourcesUtils.getMailMessage(6), e);
        } finally {
            IO.close(store);
        }
    }

    private MailFolderImpl getFolder(Store store, String name, SearchTerm term) throws MessagingException, UnsupportedEncodingException, IOException {
        Folder folder = null;
        if (name == null) {
            folder = this.getDefaultFolder(store.getDefaultFolder());
            name = folder.getFullName();
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMailMessage(9, name));
            }
        } else {
            folder = store.getFolder(name);
        }

        if (folder == null) {
            return null;
        }
        if (term == null) {
            term = new DefaultSearchTerm();
        }

        try {
            folder.open(Folder.READ_ONLY);

            MailFolderImpl mailFolder = new MailFolderImpl();
            mailFolder.setName(name);
            mailFolder.setNewMailCount(folder.getNewMessageCount());
            mailFolder.setUnreadMailCount(folder.getUnreadMessageCount());

            List<Mail> mails = new ArrayList<Mail>();
            Message[] messages = folder.search(term);
            for (int i = 0; i < messages.length; i++) {
                Mail mail = this.toMail(messages[i], mailFolder);
                mails.add(mail);
            }
            mailFolder.setList(mails);
            return mailFolder;
        } finally {
            if (folder.isOpen()) {
                folder.close(false);
            }
        }
    }

    public Folder getDefaultFolder(Folder folder) throws MessagingException {
        if (folder.exists()) {
            try {
                Folder[] list = folder.list();
                for (Folder file : list) {
                    if (file != null) {
                        return file;
                    }
                }
            } catch (Exception e) {
                String msg = StringUtils.toString(e);
                if (msg.indexOf("not a directory") == -1) {
                    throw new MessagingException(folder.getFullName(), e);
                }
            }
        }
        return folder;
    }

    private Mail toMail(Message msg, MailFolder mailFolder) throws MessagingException, UnsupportedEncodingException, IOException {
        MailImpl mail = new MailImpl();
        mail.setFolder(mailFolder);
        mail.setFolderIndex(msg.getMessageNumber());
        mail.setId(StringUtils.toString(msg.getMessageNumber()));
        mail.setTitle(msg.getSubject());

        String[] from = toString(msg.getFrom());
        if (!ArrayUtils.isEmpty(from)) {
            String[] address = toAddress(from[0].toString());
            mail.setSenderName(address[0]);
            mail.setSenderAddress(address[1]);
        }

        List<String> receiverNames = new ArrayList<String>();
        List<String> receiverAddress = new ArrayList<String>();
        String[] allRecipients = toString(msg.getAllRecipients());
        for (String add : allRecipients) {
            String[] address = toAddress(add.toString());
            receiverNames.add(address[0]);
            receiverAddress.add(address[1]);
        }

        mail.setReceiverNames(receiverNames);
        mail.setReceiverAddress(receiverAddress);
        mail.setSendTime(msg.getSentDate());
        mail.setReceivedTime(msg.getReceivedDate());
        mail.setAttachments(this.toMailAttachments(msg, mail));
        this.setMailContent(msg, mail);
        mail.setNew(this.isNew(msg));
        mail.setHasRead(this.hasRead(msg));
        return mail;
    }

    private List<MailAttachment> toMailAttachments(Message message, Mail mail) throws IOException, MessagingException, UnsupportedEncodingException {
        List<MailAttachment> attachements = new ArrayList<MailAttachment>();
        if (message.getContent() instanceof Multipart) {
            Multipart content = (Multipart) message.getContent();
            for (int j = 0; j < content.getCount(); j++) {
                BodyPart part = content.getBodyPart(j);

                if (part.getDisposition() != null) {
                    String filename = part.getFileName();
                    if (StringUtils.isBlank(filename)) {
                        continue;
                    }

                    if (filename.startsWith("=?")) {
                        filename = MimeUtility.decodeText(filename);
                    }

                    MailAttachmentImpl obj = new MailAttachmentImpl();
                    obj.setMail(mail);
                    obj.setName(filename);
                    obj.setDescription(part.getDescription());
                    attachements.add(obj);
                }
            }
        }
        return attachements;
    }

    private boolean hasRead(Message msg) throws MessagingException {
        try {
            return msg.getFlags().contains(Flags.Flag.SEEN);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isNew(Message msg) throws MessagingException {
        boolean isnew = false;
        Flags flags = msg.getFlags();
        Flags.Flag[] flag = flags.getSystemFlags();
        for (int i = 0; i < flag.length; i++) {
            if (flag[i] == Flags.Flag.SEEN) {
                isnew = true;
                break;
            }
        }
        return isnew;
    }

    private void setMailContent(Part part, MailImpl mail) throws MessagingException, IOException {
        try {
            String type = part.getContentType();
            int index = type.indexOf("name");
            boolean name = false;
            if (index != -1) {
                name = true;
            }

            if (part.isMimeType("text/plain") && !name) {
                mail.setText((String) part.getContent());
            } else if (part.isMimeType("text/html") && !name) {
                mail.setHtml((String) part.getContent());
            } else if (part.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) part.getContent();
                int counts = multipart.getCount();
                for (int i = 0; i < counts; i++) {
                    this.setMailContent(multipart.getBodyPart(i), mail);
                }
            } else if (part.isMimeType("message/rfc822")) {
                this.setMailContent((Part) part.getContent(), mail);
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File download(MailAttachment attachment, File parent) {
        if (attachment == null) {
            return null;
        }

        Mail mail = attachment.getMail();
        int messageId = mail.getFolderIndex();
        MailFolder mailFolder = mail.getFolder();
        String folderName = mailFolder.getName();

        Store store = null;
        try {
            Properties config = new Properties();
            config.setProperty("mail.debug", String.valueOf(log.isDebugEnabled()));
            config.setProperty("mail.store.protocol", mailFolder.getProtocol());
            // mail.transport.protocol

            Session session = Session.getInstance(config);
            store = session.getStore();
            store.connect(this.host, this.username, this.password);

            Folder folder = store.getFolder(folderName);
            if (folder == null) {
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMailMessage(8, folderName));
                }
                return null;
            }

            try {
                folder.open(Folder.READ_ONLY);
                Message message = folder.getMessage(messageId);
                if (message == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getMailMessage(7, messageId));
                    }
                    return null;
                } else {
                    return this.saveAttachment(message, parent, attachment.getName());
                }
            } finally {
                if (folder.isOpen()) {
                    folder.close(false);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(ResourcesUtils.getMailMessage(6), e);
        } finally {
            IO.close(store);
        }
    }

    private File saveAttachment(Part message, File parent, String downfilename) throws UnsupportedEncodingException, MessagingException, IOException {
        if (message.getContent() instanceof Multipart) {
            Multipart content = (Multipart) message.getContent();
            for (int j = 0; j < content.getCount(); j++) {
                BodyPart part = content.getBodyPart(j);

                if (part.getDisposition() != null) {
                    String filename = part.getFileName();
                    if (filename.startsWith("=?")) {
                        filename = MimeUtility.decodeText(filename);
                    }

                    if (filename != null && (filename.equalsIgnoreCase(downfilename) || GPatternExpression.match(filename, downfilename))) {
                        return this.saveFile(parent, filename, part.getInputStream());
                    }
                }
            }
        }
        return null;
    }

    private File saveFile(File parent, String filename, InputStream in) throws IOException {
        if (parent == null) {
            parent = FileUtils.getTempDir("mail", "attach");
        } else {
            Ensure.isTrue(parent.exists() && parent.isDirectory(), parent, filename, in);
        }

        File file = new File(parent, filename);
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMailMessage(4, file));
        }

        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buf = new byte[1024];
            for (int length = in.read(buf, 0, buf.length); length != -1; length = in.read(buf, 0, buf.length)) {
                out.write(buf, 0, length);
                out.flush();
            }
            return file;
        } catch (Exception e) {
            throw new RuntimeException(ResourcesUtils.getMailMessage(5, filename));
        } finally {
            out.close();
        }
    }

}

class DefaultSearchTerm extends SearchTerm {
    private final static long serialVersionUID = 1L;

    public boolean match(Message msg) {
        return msg.getMessageNumber() <= 10;
    }
}
