package icu.etl.io;

import icu.etl.ioc.EasyBeanContext;
import org.junit.Assert;
import org.junit.Test;

public class TextTableFileBuilderTest {

    @Test
    public void test() throws Exception {
        EasyBeanContext cxt = new EasyBeanContext("sout:debug");
        TextTableFileBuilder builder = new TextTableFileBuilder();
        Assert.assertNotNull(builder.getBean(cxt, "csv"));
    }

}
