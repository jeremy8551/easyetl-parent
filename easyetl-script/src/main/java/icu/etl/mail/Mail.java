package icu.etl.mail;

import java.util.Date;
import java.util.List;

public interface Mail {

    /**
     * 返回邮件编号
     *
     * @return
     */
    String getId();

    /**
     * 返回邮件所在文件夹
     *
     * @return
     */
    MailFolder getFolder();

    /**
     * 返回邮件所在文件夹的编号
     *
     * @return
     */
    int getFolderIndex();

    /**
     * 邮件标题
     *
     * @return
     */
    String getTitle();

    /**
     * 邮件发送地址
     *
     * @return
     */
    String getSenderAddress();

    /**
     * 邮件发送人
     *
     * @return
     */
    String getSenderName();

    /**
     * 邮件发送时间
     *
     * @return
     */
    Date getSendTime();

    /**
     * 接受邮件的时间
     *
     * @return
     */
    Date getReceivedTime();

    /**
     * 邮件的接收地址
     *
     * @return
     */
    List<String> getReceiverAddress();

    /**
     * 邮件的接收人
     *
     * @return
     */
    List<String> getReceiverNames();

    /**
     * 收件人阅读电子邮件的时间，null表示尚未阅读电子邮件内容
     *
     * @return
     */
    List<Date> getReceiverReadTime();

    /**
     * 邮件正文（文本）
     *
     * @return
     */
    String getText();

    /**
     * 邮件正文（html）
     *
     * @return
     */
    String getHtml();

    /**
     * 判断邮件是否已被阅读
     *
     * @return
     */
    boolean hasRead();

    /**
     * 判断邮件是否是未读邮件
     *
     * @return
     */
    boolean isNew();

    /**
     * 返回邮件的附件
     *
     * @return
     */
    List<MailAttachment> getAttachments();

}
