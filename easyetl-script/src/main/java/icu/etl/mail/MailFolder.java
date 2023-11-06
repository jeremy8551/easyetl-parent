package icu.etl.mail;

import java.util.List;

/**
 * 用于描述邮件服务器上文件夹信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-12-06
 */
public interface MailFolder {

    /**
     * 返回邮件文件夹名
     *
     * @return
     */
    String getName();

    /**
     * 返回邮件协议名
     *
     * @return
     */
    String getProtocol();

    /**
     * 返回邮件协议端口号
     *
     * @return
     */
    int getProtocolPort();

    /**
     * 返回 true 表示使用SSL协议收发邮件
     *
     * @return
     */
    boolean isSSL();

    /**
     * 返回未读邮件个数
     *
     * @return
     */
    int getUnreadMailCount();

    /**
     * 返回文件夹中已读邮件个数
     *
     * @return
     */
    int getNewMailCount();

    /**
     * 返回文件夹所有邮件的个数
     *
     * @return
     */
    List<Mail> getMails();

}
