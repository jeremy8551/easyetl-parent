package icu.etl.mail;

import java.util.Date;
import java.util.List;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.util.Dates;

/**
 * 邮件的接口实现类
 */
@EasyBeanClass(type = Mail.class)
public class MailImpl implements Mail {
    private MailFolder folder;
    private String id;
    private int folderIndex;
    private String title;
    private String senderAddress;
    private String senderName;
    private Date sendTime;
    private Date receivedTime;
    private List<String> receiverAddress;
    private List<String> receiverNames;
    private List<Date> receiverReadTime;
    private String text;
    private String html;
    private boolean isNew;
    private boolean hasRead;
    private List<MailAttachment> attachments;

    public String getId() {
        return this.id;
    }

    public MailFolder getFolder() {
        return folder;
    }

    public int getFolderIndex() {
        return this.folderIndex;
    }

    public String getTitle() {
        return this.title;
    }

    public String getSenderAddress() {
        return this.senderAddress;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public Date getSendTime() {
        return this.sendTime;
    }

    public Date getReceivedTime() {
        return this.receivedTime;
    }

    public List<String> getReceiverAddress() {
        return this.receiverAddress;
    }

    public List<String> getReceiverNames() {
        return this.receiverNames;
    }

    public List<Date> getReceiverReadTime() {
        return this.receiverReadTime;
    }

    public String getText() {
        return this.text;
    }

    public String getHtml() {
        return this.html;
    }

    public boolean hasRead() {
        return this.hasRead;
    }

    public boolean isNew() {
        return this.isNew;
    }

    public List<MailAttachment> getAttachments() {
        return this.attachments;
    }

    public void setFolder(MailFolder folder) {
        this.folder = folder;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFolderIndex(int folderIndex) {
        this.folderIndex = folderIndex;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    public void setReceiverAddress(List<String> receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public void setReceiverNames(List<String> receiverNames) {
        this.receiverNames = receiverNames;
    }

    public void setReceiverReadTime(List<Date> receiverReadTime) {
        this.receiverReadTime = receiverReadTime;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public void setHasRead(boolean hasRead) {
        this.hasRead = hasRead;
    }

    public void setAttachments(List<MailAttachment> attachments) {
        this.attachments = attachments;
    }

    public String toString() {
        return "MailImpl [folder=" + folder + ", id=" + id + ", folderIndex=" + folderIndex + ", title=" + title + ", senderAddress=" + senderAddress + ", senderName=" + senderName + ", sendTime=" + Dates.format19(sendTime) + ", receivedTime=" + Dates.format19(receivedTime) + ", receiverAddress=" + receiverAddress + ", receiverNames=" + receiverNames + ", receiverReadTime=" + receiverReadTime + ", isNew=" + isNew + ", hasRead=" + hasRead + ", attachments=" + attachments + "]";
    }

}
