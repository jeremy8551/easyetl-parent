package icu.etl.test.loader;

import icu.etl.annotation.EasyBean;
import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.EasyContext;
import icu.etl.test.bean.TestLoader;

@EasyBean
public class TestLoaderBuilder implements BeanBuilder<TestLoader> {
    @Override
    public TestLoader getBean(EasyContext context, Object... args) throws Exception {
        return null;
    }
}
