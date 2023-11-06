package icu.etl.os;

import java.util.Iterator;
import javax.script.SimpleBindings;

import icu.etl.ioc.EasyBeanContext;
import icu.etl.util.Settings;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class OSTest {

    @Rule
    public WithSSHRule rule = new WithSSHRule();

    @Test
    public void test() {
        EasyBeanContext context = rule.getContext();
        SimpleBindings env = rule.getEnvironment();
        String host = (String) env.get("ssh.host");
        int port = Integer.parseInt((String) env.get("ssh.port"));
        String username = (String) env.get("ssh.username");
        String password = (String) env.get("ssh.password");
        System.out.println(username + "@" + host + ":" + port + "?password=" + password);

        OS os = context.getBean(OS.class, host, port, username, password);
        this.testoscommand(os);

        boolean exists = false;
        for (Iterator<OSNetworkCard> it = os.getOSNetwork().getOSNetworkCards().iterator(); it.hasNext(); ) {
            OSNetworkCard card = it.next();
            System.out.println(card.getIPAddress());
            if (host.equals(card.getIPAddress())) {
                exists = true;
                break;
            }
        }
        Assert.assertTrue(exists);

        this.testoscommand(context.getBean(OS.class, host, username, password));
        this.testoscommand(context.getBean(OS.class, Settings.getUserName()));
        this.testoscommand(context.getBean(OS.class));
    }

    private void testoscommand(OS os) {
        try {
            Assert.assertTrue(os.supportOSCommand());
            Assert.assertTrue(os.supportOSFileCommand());
            Assert.assertTrue(os.enableOSCommand());
            Assert.assertTrue(os.enableOSFileCommand());
        } finally {
            os.close();
        }
    }

}
