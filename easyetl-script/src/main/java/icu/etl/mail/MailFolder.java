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
     * @return 文件夹名
     */
    String getName();

    /**
     * 返回邮件协议名
     *
     * @return 邮件协议名
     */
    String getProtocol();

    /**
     * 返回邮件协议端口号
     *
     * @return 邮件协议端口号
     */
    int getProtocolPort();

    /**
     * 判断是否使用SSL协议收发邮件
     *
     * @return 返回 true 表示使用SSL协议收发邮件
     */
    boolean isSSL();

    /**
     * 返回未读邮件个数
     *
     * @return 未读邮件个数
     */
    int getUnreadMailCount();

    /**
     * 返回文件夹中已读邮件个数
     *
     * @return 已读邮件个数
     */
    int getNewMailCount();

    /**
     * 返回文件夹所有邮件的个数
     *
     * @return 所有邮件个数
     */
    List<Mail> getMails();

}
