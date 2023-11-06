package icu.etl.database.db2;

import org.junit.Assert;
import org.junit.Test;

public class DB2DialectTest {

    @Test
    public void test() {
        Assert.assertEquals(null, DB2Dialect.to(null));
        Assert.assertEquals("", DB2Dialect.to(""));
        Assert.assertEquals("A", DB2Dialect.to("a"));
        Assert.assertEquals("AB", DB2Dialect.to("ab"));
        Assert.assertEquals("aBc", DB2Dialect.to("\"aBc\""));
        Assert.assertEquals("aBc", DB2Dialect.to("'aBc'"));
        Assert.assertEquals("", DB2Dialect.to(""));
    }
}
