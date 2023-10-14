package icu.etl.mail;

/**
 * 用于描述邮件附件接口功能
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-12-16
 */
public interface MailAttachment {

    /**
     * 返回附件归属的邮件对象
     *
     * @return
     */
    Mail getMail();

    /**
     * 返回附件说明信息
     *
     * @return A String.
     * @since 1.0
     */
    String getDescription();

    /**
     * 返回附件名
     *
     * @return A String.
     * @since 1.0
     */
    String getName();

}
