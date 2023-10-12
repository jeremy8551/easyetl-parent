package icu.etl.log;

import icu.etl.util.FileUtils;
import org.junit.Test;

public class DefaultLoggerTest {

    @Test
    public void test2() {
        String s = "" + FileUtils.lineSeparatorMacOS;
        s += "1" + FileUtils.lineSeparator;
        s += "12" + FileUtils.lineSeparatorWindows;
        s += "" + FileUtils.lineSeparator;
        s += "1345" + FileUtils.lineSeparatorWindows;
        s += "" + FileUtils.lineSeparator;

        DefaultLogger log = new DefaultLogger(LogFactory.INSTANCE, DefaultLoggerTest.class, "info");
        log.write("test1");
        log.writeline("1234");
        log.info("5");
    }

    @Test
    public void test3() {
    }
}
