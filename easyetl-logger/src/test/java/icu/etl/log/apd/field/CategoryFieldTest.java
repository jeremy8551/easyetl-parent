package icu.etl.log.apd.field;

import icu.etl.log.LogFactory;
import icu.etl.log.apd.DefaultLogBuilder;
import icu.etl.log.apd.LogAppender;
import icu.etl.log.cxt.LogContextImpl;
import org.junit.Assert;
import org.junit.Test;

public class CategoryFieldTest {

    @Test
    public void test1() {
        LogContextImpl context = new LogContextImpl();
        context.setBuilder(new DefaultLogBuilder());

        LogAppender a = new LogAppender("%c{10}");
        a.setup(context);

        LogFactory.getLog(context, String.class, null, false).info("test");
        Assert.assertEquals(String.class.getName(), a.getValue());

        a.close();
        Assert.assertNotNull(a.getName());
    }

}
