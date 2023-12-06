package icu.etl.test.loader;

import icu.etl.annotation.EasyBean;
import icu.etl.ioc.EasyBeanBuilder;
import icu.etl.ioc.EasyContext;
import icu.etl.test.bean.TestLoader;

@EasyBean
public class TestLoaderBuilder implements EasyBeanBuilder<TestLoader> {
    @Override
    public TestLoader getBean(EasyContext context, Object... args) throws Exception {
        return null;
    }
}
