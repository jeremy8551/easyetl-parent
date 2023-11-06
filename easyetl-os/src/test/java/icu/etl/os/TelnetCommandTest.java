package icu.etl.os;

import javax.script.SimpleBindings;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * TELNET 协议操作接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2022-01-13
 */
@Ignore
public class TelnetCommandTest {

    @Rule
    public WithSSHRule rule = new WithSSHRule();

    @Test
    public void test() throws Exception {
        SimpleBindings env = rule.getEnvironment();
        String host = (String) env.get("ssh.host");
        int port = Integer.parseInt((String) env.get("ssh.port"));
        String username = (String) env.get("ssh.username");
        String password = (String) env.get("ssh.password");

        OSShellCommand cmd = rule.getContext().getBean(OSShellCommand.class, "telnet");
        try {
            Assert.assertTrue(cmd.connect(host, 23, username, password));

            cmd.execute("pwd", 2000);
            System.out.println(cmd.getStdout());

            cmd.execute("ls -la", 2000);
            System.out.println(cmd.getStdout());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            cmd.close();
        }
    }

}
