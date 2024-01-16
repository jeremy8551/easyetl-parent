package icu.etl.ioc.impl;

import icu.etl.ioc.EasyBeanContext;
import org.junit.Assert;
import org.junit.Test;

public class EasyBeanFactoryImplTest {

    @Test
    public void create() {
        EasyBeanContext context = new EasyBeanContext();
        CeshiBean bean = context.createBean(CeshiBean.class);
        Assert.assertNotNull(bean.getContext());
        Assert.assertEquals(context, bean.getContext());
        Assert.assertEquals("test2", bean.getCeshi().getMessage());
    }

}