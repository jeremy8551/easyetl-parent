package icu.etl.os;

import icu.etl.os.linux.Linuxs;
import org.junit.Assert;
import org.junit.Test;

public class LinuxsTest {

    @Test
    public void test() {
        Assert.assertEquals("", Linuxs.removeShellNote("", null));
        Assert.assertEquals("1", Linuxs.removeShellNote("1", null));
        Assert.assertEquals("12", Linuxs.removeShellNote("12", null));
        Assert.assertEquals("1", Linuxs.removeShellNote("1#2", null));
        Assert.assertEquals("1", Linuxs.removeShellNote("1#234", null));
        Assert.assertEquals("1", Linuxs.removeShellNote("1#", null));
    }

}
