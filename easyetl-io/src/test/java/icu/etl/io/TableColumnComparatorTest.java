package icu.etl.io;

import org.junit.Assert;
import org.junit.Test;

public class TableColumnComparatorTest {

    @Test
    public void test() {
        TableColumnComparator c = new TableColumnComparator();
        Assert.assertEquals(0, c.compare("1", "1"));
        Assert.assertNotEquals(0, c.compare("1", "2"));
        Assert.assertTrue(c.compare("1", "2") < 0);
        Assert.assertTrue(c.compare("2", "1") > 0);
    }
}
