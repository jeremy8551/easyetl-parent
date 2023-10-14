package icu.etl.mail;

import java.util.List;

/**
 * 邮箱服务器上文件夹的接口实现类
 */
public class MailFolderImpl implements MailFolder {

    private String name;
    private int unreadMailCount;
    private int newMailCount;
    private List<Mail> list;
    private String protocol;
    private int protocolPort;
    private boolean ssl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnreadMailCount(int unreadMailCount) {
        this.unreadMailCount = unreadMailCount;
    }

    public void setNewMailCount(int newMailCount) {
        this.newMailCount = newMailCount;
    }

    public void setList(List<Mail> list) {
        this.list = list;
    }

    public int getUnreadMailCount() {
        return this.unreadMailCount;
    }

    public int getNewMailCount() {
        return this.newMailCount;
    }

    public List<Mail> getMails() {
        return list;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getProtocolPort() {
        return protocolPort;
    }

    public void setProtocolPort(int protocolPort) {
        this.protocolPort = protocolPort;
    }

    public void setSSL(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isSSL() {
        return this.ssl;
    }

    public String toString() {
        return "MailFolderImpl [unreadMailCount=" + unreadMailCount + ", newMailCount=" + newMailCount + "]";
    }

}
