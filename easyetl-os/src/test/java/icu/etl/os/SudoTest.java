package icu.etl.os;

import java.io.InputStream;
import java.io.OutputStream;
import javax.script.SimpleBindings;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore
public class SudoTest {

    @Rule
    public WithSSHRule rule = new WithSSHRule();

    @Test
    public void test() {
        SimpleBindings env = rule.getEnvironment();
        String host = (String) env.get("ssh.host");
        int port = Integer.parseInt((String) env.get("ssh.port"));
        String username = (String) env.get("ssh.username");
        String password = (String) env.get("ssh.password");

        String suname = "";
        String supass = "";

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setServerAliveInterval(60);
            session.setTimeout(0);
            session.connect(0);

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("whoami && su - " + suname);
            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            ((ChannelExec) channel).setErrStream(System.err);
            channel.connect();

            out.write((supass + "\n").getBytes());
            out.flush();

            out.write(("pwd && whoami && exit 0" + "\n").getBytes());
            out.flush();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }

                if (channel.isClosed()) {
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
