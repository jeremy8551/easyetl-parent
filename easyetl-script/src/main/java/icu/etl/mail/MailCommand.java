package icu.etl.mail;

import java.io.File;
import java.util.List;

import icu.etl.apache.mail.search.SearchTerm;
import icu.etl.util.CharsetName;

/**
 * 用于描述邮件发送功能和查询邮件功能
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-10-01
 */
public interface MailCommand extends CharsetName {

    /**
     * 设置邮件服务器地址
     *
     * @param host 地址
     */
    void setHost(String host);

    /**
     * 设置登录邮件服务器认证用户和密码
     *
     * @param username 用户名
     * @param password 密码
     */
    void setUser(String username, String password);

    /**
     * 使用指定协议发送邮件
     *
     * @param protocol    邮件协议名
     * @param port        邮件协议的端口号
     * @param ssl         true表示使用SSL加密方式发送邮件
     * @param sender      设置邮件发送地址: “name address@company.com”
     * @param receivers   设置邮件的接收地址: “name address@company.com”
     * @param title       邮件的主题
     * @param content     邮件正文
     * @param attachments 邮件的附件
     * @return 发送邮件的唯一编号
     */
    String send(String protocol, int port, boolean ssl, String sender, List<String> receivers, String title, CharSequence content, MailFile... attachments);

    /**
     * 使用指定搜索条件在邮件服务器上搜索邮件
     *
     * @param protocol   邮件协议
     * @param port       邮件协议的端口号
     * @param ssl        true表示使用SSL加密方式发送邮件
     * @param folderName 搜索的文件夹
     * @param condition  搜索条件 <br>
     *                   如果为null时，默认返回前10个邮件
     * @return 邮件集合
     */
    List<Mail> search(String protocol, int port, boolean ssl, String folderName, SearchTerm condition);

    /**
     * 下载指定邮件的附件
     *
     * @param attachment 邮件附件
     * @param parent     附件存储目录 <br>
     *                   如果为null时默认使用操作系统的临时目录存放附件
     * @return 附件文件
     */
    File download(MailAttachment attachment, File parent);

}
