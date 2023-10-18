package icu.etl.io;

import icu.etl.ioc.BeanContext;
import org.junit.Assert;
import org.junit.Test;

public class TextTableFileBuilderTest {

    @Test
    public void test() throws Exception {
        BeanContext cxt = new BeanContext("sout:debug");
        TextTableFileBuilder builder = new TextTableFileBuilder();
        Assert.assertNotNull(builder.build(cxt, "csv"));
    }

}
