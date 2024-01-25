package icu.etl.mail;

import icu.apache.mail.common.SimpleEmail;
import icu.etl.util.Dates;

public class SendEmailExample {

    public void test() {
        try {
            SimpleEmail mail = new SimpleEmail();
            mail.setHostName("mail.foxmail.com");
            mail.setAuthentication("etl", "xxx");// 邮件服务器验证：用户名/密码
            mail.setCharset("UTF-8");// 必须放在前面，否则乱码
            mail.addTo("410336929@qq.com");
            mail.setSSL(true);
            mail.setFrom("etl@foxmail.com", "测试 邮件");
            mail.setSubject("测试单统计信息-" + Dates.currentTimeStamp());
            mail.setDebug(false);

            StringBuilder msg = new StringBuilder();
            msg.append("测试单统计信息如下：").append("\r\n\t");
            msg.append("1、测试单数量: 4").append("\r\n\t");
            msg.append("2、测试单数量：5").append("\r\n\t");
            msg.append("3、测试单成功数量：6").append("\r\n\t");
            msg.append("统计时间：1 ").append(Dates.currentTimeStamp());

            mail.setMsg(msg.toString());
            mail.send();
            System.out.println("邮件发送完毕！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
