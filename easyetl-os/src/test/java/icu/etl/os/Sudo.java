package icu.etl.os;

import java.io.InputStream;
import java.io.OutputStream;

import icu.jsch.Channel;
import icu.jsch.ChannelExec;
import icu.jsch.JSch;
import icu.jsch.Session;

public class Sudo {

    public static void main(String[] arg) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession("root", "192.168.33.218", 22);
            session.setPassword("root");
            session.setConfig("StrictHostKeyChecking", "no");
            session.setServerAliveInterval(60);
            session.setTimeout(0);
            session.connect(0);

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("whoami && su - db2inst1");
            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            ((ChannelExec) channel).setErrStream(System.err);
            channel.connect();

            out.write(("pwd && whoami && exit 0" + "\n").getBytes());
            out.flush();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
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
