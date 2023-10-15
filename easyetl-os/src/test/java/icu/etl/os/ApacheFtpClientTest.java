package icu.etl.os;

import icu.etl.os.ftp.FtpCommand;
import icu.etl.util.Ensure;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ApacheFtpClientTest {

    @Test
    public void test1() {
        FtpCommand ftp = new FtpCommand();
        try {
            Ensure.isTrue(ftp.connect(TestEnv.getFtpHost(), TestEnv.getFtpPort(), TestEnv.getFtpUsername(), TestEnv.getFtpPassword()));
            SftpClientTest.test(ftp);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            ftp.close();
        }
    }
}
