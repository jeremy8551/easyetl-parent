package icu.etl.os;

import javax.script.SimpleBindings;

import icu.etl.os.ftp.FtpCommand;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore
public class FtpCommandTest {

    @Rule
    public WithFtpRule rule = new WithFtpRule();

    @Test
    public void test() {
        SimpleBindings env = rule.getEnvironment();
        String ftphost = (String) env.get("ftp.host");
        int ftpport = Integer.parseInt((String) env.get("ftp.port"));
        String ftpusername = (String) env.get("ftp.username");
        String ftppassword = (String) env.get("ftp.password");
        String ftphomedir = (String) env.get("ftp.homedir");

        FtpCommand ftp = new FtpCommand();
        try {
            ftp.setContext(rule.getContext());
            Assert.assertTrue(ftp.connect(ftphost, ftpport, ftpusername, ftppassword));
            if (StringUtils.isNotBlank(ftphomedir)) {
                Assert.assertTrue(ftp.cd(ftphomedir));
            }
            FtpClientCase.run(ftp);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            ftp.close();
        }
    }
}
