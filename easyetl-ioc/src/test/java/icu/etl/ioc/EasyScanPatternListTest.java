package icu.etl.ioc;

import java.util.Arrays;

import icu.etl.ProjectPom;
import icu.etl.ioc.scan.ClassScanner;
import icu.etl.ioc.scan.EasyScanPatternList;
import org.junit.Assert;
import org.junit.Test;

public class EasyScanPatternListTest {

    @Test
    public void test1() {
        EasyScanPatternList list = new EasyScanPatternList();
        list.add("org.apache.test");
        list.add("org.apache.*");
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("org.apache", list.get(0).getRule());

        list.add("!com.spring.**");
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("!com.spring", list.get(1).getRule());

        list.addArgument("a.b.c,a.b,", "!", "^^");
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("a.b", list.get(2).getRule());
        Assert.assertEquals(2, list.getScanPattern().size());

        list.exclude(Arrays.asList("d.c"));
        Assert.assertEquals(2, list.getScanPattern().size());
        Assert.assertEquals("!d.c", list.get(list.size() - 1).getRule());
        Assert.assertTrue(list.get(list.size() - 1).isExclude());

        System.setProperty(ClassScanner.PROPERTY_SCANNPKG, "g.h.c, !a.c.d");
        list.addProperty();
        Assert.assertEquals(3, list.getScanPattern().size());
        Assert.assertEquals("!a.c.d", list.get(list.size() - 1).getRule());

        int old = list.size();
        list.addFirst("org.apache.test");
        Assert.assertEquals(old, list.size());
        Assert.assertEquals("org.apache", list.get(0).getPrefix());

        list.addGroupID();
        Assert.assertEquals(old + 1, list.size());
        Assert.assertEquals(ProjectPom.getGroupID(), list.get(0).getPrefix());

        Assert.assertEquals("icu.etl,org.apache,!com.spring,a.b,!d.c,g.h.c,!a.c.d", list.toArgumentString());
        Assert.assertEquals("[icu.etl, org.apache, !com.spring, a.b, !d.c, g.h.c, !a.c.d]", Arrays.toString(list.toArray()));
        System.out.println(list.toArgumentString());
        System.out.println(Arrays.toString(list.toArray()));
    }
}
