package icu.etl.apache;

import java.io.ByteArrayInputStream;

import icu.etl.apache.mail.activation.registries.MailcapFile;
import icu.etl.apache.mail.activation.registries.MimeTypeFile;
import icu.etl.apache.mail.activation.viewers.ImageViewer;
import icu.etl.apache.mail.activation.viewers.TextEditor;
import icu.etl.apache.mail.activation.viewers.TextViewer;
import icu.etl.apache.mail.handlers.message_rfc822;
import icu.etl.apache.mail.handlers.multipart_mixed;
import icu.etl.apache.mail.handlers.text_html;
import icu.etl.apache.mail.handlers.text_plain;
import icu.etl.apache.mail.handlers.text_xml;

/**
 * 邮件功能的接口实现类 <br>
 * <p>
 * SMTP:邮件发送协议 ssl对应端口465 非ssl对应端口25
 * <p>
 * IMAP:收邮件协议 ssl对应端口993 非ssl对应端口143
 *
 * @author jeremy8551@qq.com
 */
public class ApacheEmailCommand {

    /**
     * Load mailcap.default
     */
    public static MailcapFile loadMailcapDefault() {
        try {
            StringBuilder buf = new StringBuilder();
            buf.append("image/gif;;		x-java-view=").append(ImageViewer.class.getName()).append('\n');
            buf.append("image/jpeg;;		x-java-view=").append(ImageViewer.class.getName()).append('\n');
            buf.append("text/*;;		x-java-view=").append(TextViewer.class.getName()).append('\n');
            buf.append("text/*;;		x-java-edit=").append(TextEditor.class.getName()).append('\n');
            return new MailcapFile(new ByteArrayInputStream(buf.toString().getBytes("ISO-8859-1")));
        } catch (Exception e) {
            throw new RuntimeException("set mailcap.default fail!", e);
        }
    }

    /**
     * mimetypes.default
     */
    public static MimeTypeFile loadMimetypesDefault() {
        try {
            StringBuilder buf = new StringBuilder();
            buf.append("text/html		html htm HTML HTM").append('\n');
            buf.append("text/plain		txt text TXT TEXT").append('\n');
            buf.append("image/gif		gif GIF").append('\n');
            buf.append("image/ief		ief").append('\n');
            buf.append("image/jpeg		jpeg jpg jpe JPG").append('\n');
            buf.append("image/tiff		tiff tif").append('\n');
            buf.append("image/png		png PNG").append('\n');
            buf.append("image/x-xwindowdump	xwd").append('\n');
            buf.append("application/postscript	ai eps ps").append('\n');
            buf.append("application/rtf		rtf").append('\n');
            buf.append("application/x-tex	tex").append('\n');
            buf.append("application/x-texinfo	texinfo texi").append('\n');
            buf.append("application/x-troff	t tr roff").append('\n');
            buf.append("audio/basic		au").append('\n');
            buf.append("audio/midi		midi mid").append('\n');
            buf.append("audio/x-aifc		aifc").append('\n');
            buf.append("audio/x-aiff            aif aiff").append('\n');
            buf.append("audio/x-mpeg		mpeg mpg").append('\n');
            buf.append("audio/x-wav             wav").append('\n');
            buf.append("video/mpeg		mpeg mpg mpe").append('\n');
            buf.append("video/quicktime		qt mov").append('\n');
            buf.append("video/x-msvideo		avi").append('\n');
            return new MimeTypeFile(new ByteArrayInputStream(buf.toString().getBytes("ISO-8859-1")));
        } catch (Exception e) {
            throw new RuntimeException("mimetypes.default", e);
        }
    }

    /**
     * Load mailcap
     */
    public static MailcapFile loadMailcap() {
        try {
            StringBuilder buf = new StringBuilder();
            buf.append("text/plain;;		x-java-content-handler=").append(text_plain.class.getName()).append('\n');
            buf.append("text/html;;		x-java-content-handler=").append(text_html.class.getName()).append('\n');
            buf.append("text/xml;;		x-java-content-handler=").append(text_xml.class.getName()).append('\n');
            buf.append("multipart/*;;	x-java-content-handler=").append(multipart_mixed.class.getName()).append("; x-java-fallback-entry=true").append('\n');
            buf.append("message/rfc822;;	x-java-content-handler=").append(message_rfc822.class.getName()).append('\n');
            buf.append("").append(TextEditor.class.getName()).append('\n');
            return new MailcapFile(new ByteArrayInputStream(buf.toString().getBytes("ISO-8859-1")));
        } catch (Exception e) {
            throw new RuntimeException("set mailcap fail!", e);
        }
    }

}


