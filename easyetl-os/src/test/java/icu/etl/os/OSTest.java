package icu.etl.os;

import java.util.Iterator;

import icu.etl.ioc.AnnotationEasyetlContext;
import icu.etl.util.Settings;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/2
 */
public class OSTest {

    @Test
    public void testos() {
        String host = TestEnv.getSSHHost();
        int port = TestEnv.getSSHPort();
        String username = TestEnv.getSSHUsername();
        String password = TestEnv.getSSHPassword();
        System.out.println(username + "@" + host + ":" + port + "?password=" + password);

        AnnotationEasyetlContext context = new AnnotationEasyetlContext();
        OS os = context.get(OS.class, host, port, username, password);
        testoscommand(os);

        boolean existsIp = false;
        for (Iterator<OSNetworkCard> it = os.getOSNetwork().getOSNetworkCards().iterator(); it.hasNext(); ) {
            OSNetworkCard obj = it.next();
            System.out.println(obj.getIPAddress());
            if (host.equals(obj.getIPAddress())) {
                existsIp = true;
                break;
            }
        }
        assertTrue(existsIp);

        testoscommand(context.get(OS.class, host, username, password));
        testoscommand(context.get(OS.class, Settings.getUserName()));
        testoscommand(context.get(OS.class));
    }

    private void testoscommand(OS os) {
        try {
            assertTrue(os.supportOSCommand());
            assertTrue(os.supportOSFileCommand());
            assertTrue(os.enableOSCommand());
            assertTrue(os.enableOSFileCommand());
        } finally {
            os.close();
        }
    }

}
