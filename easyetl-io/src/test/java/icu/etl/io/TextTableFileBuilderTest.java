package icu.etl.io;

import icu.etl.ioc.AnnotationEasyetlContext;
import org.junit.Assert;
import org.junit.Test;

public class TextTableFileBuilderTest {

    @Test
    public void test() throws Exception {
        AnnotationEasyetlContext cxt = new AnnotationEasyetlContext("sout:debug");
        TextTableFileBuilder builder = new TextTableFileBuilder();
        Assert.assertNotNull(builder.getBean(cxt, "csv"));
    }

}
