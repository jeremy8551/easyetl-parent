package icu.etl.os;

import icu.etl.os.linux.Linuxs;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/2
 */
public class LinuxsTest {

    @Test
    public void testremoveLinuxAnnotation() {
        assertTrue("".equals(Linuxs.removeLinuxAnnotation("", null)));
        assertTrue("1".equals(Linuxs.removeLinuxAnnotation("1", null)));
        assertTrue("12".equals(Linuxs.removeLinuxAnnotation("12", null)));
        assertTrue("1".equals(Linuxs.removeLinuxAnnotation("1#2", null)));
        assertTrue("1".equals(Linuxs.removeLinuxAnnotation("1#234", null)));
        assertTrue("1".equals(Linuxs.removeLinuxAnnotation("1#", null)));
    }
    
}
