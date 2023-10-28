package icu.etl.os;

import icu.etl.ioc.EasyBeanContext;

/**
 * TELNET 协议操作接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2022-01-13
 */
public class TelnetCommandTest {

    public static void main(String[] args) throws Exception {
        EasyBeanContext context = new EasyBeanContext();
        OSShellCommand telnet = context.getBean(OSShellCommand.class, "telnet");
        telnet.connect("130.1.10.10", 23, "user", "xxx");
        telnet.execute("pwd", 2000);
        System.out.println(telnet.getStdout());
        telnet.execute("ls -la", 2000);
        System.out.println(telnet.getStdout());
        telnet.close();
    }

}
