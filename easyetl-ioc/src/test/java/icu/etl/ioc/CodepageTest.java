package icu.etl.ioc;

import org.junit.Assert;
import org.junit.Test;

public class CodepageTest {

    @Test
    public void test() {
        EasyBeanContext context = new EasyBeanContext();
        Codepage bean = context.getBean(Codepage.class);
        Assert.assertEquals("UTF-8", bean.get("1208"));
        Assert.assertEquals("UTF-8", bean.get(1208));
        Assert.assertEquals("GBK", bean.get(1386));
        Assert.assertEquals("1208", bean.get("UTF-8"));
        Assert.assertEquals("1386", bean.get("GBK"));
    }

}
