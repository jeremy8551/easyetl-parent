package icu.etl.mail;

import java.util.Date;
import java.util.List;

public interface Mail {

    /**
     * 返回邮件编号
     *
     * @return 邮件编号
     */
    String getId();

    /**
     * 返回邮件所在文件夹
     *
     * @return 邮件所在文件夹
     */
    MailFolder getFolder();

    /**
     * 返回邮件所在文件夹的编号
     *
     * @return 邮件所在文件夹的编号
     */
    int getFolderIndex();

    /**
     * 邮件标题
     *
     * @return 邮件标题
     */
    String getTitle();

    /**
     * 邮件发送地址
     *
     * @return 邮件发送地址
     */
    String getSenderAddress();

    /**
     * 邮件发送人
     *
     * @return 邮件发送人
     */
    String getSenderName();

    /**
     * 邮件发送时间
     *
     * @return 邮件发送时间
     */
    Date getSendTime();

    /**
     * 接受邮件的时间
     *
     * @return 接受邮件的时间
     */
    Date getReceivedTime();

    /**
     * 邮件的接收地址
     *
     * @return 邮件的接收地址
     */
    List<String> getReceiverAddress();

    /**
     * 邮件的接收人
     *
     * @return 邮件的接收人
     */
    List<String> getReceiverNames();

    /**
     * 收件人阅读电子邮件的时间，null表示尚未阅读电子邮件内容
     *
     * @return 收件人阅读电子邮件的时间
     */
    List<Date> getReceiverReadTime();

    /**
     * 邮件正文（文本）
     *
     * @return 邮件正文
     */
    String getText();

    /**
     * 邮件正文（html）
     *
     * @return 邮件正文
     */
    String getHtml();

    /**
     * 判断邮件是否已被阅读
     *
     * @return 返回true表示邮件已被阅读
     */
    boolean hasRead();

    /**
     * 判断邮件是否是未读邮件
     *
     * @return 返回true表示邮件还未被阅读
     */
    boolean isNew();

    /**
     * 返回邮件的附件
     *
     * @return 附件
     */
    List<MailAttachment> getAttachments();

}
