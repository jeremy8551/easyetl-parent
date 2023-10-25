package icu.etl.test.loader;

import icu.etl.annotation.EasyBean;
import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.EasyetlContext;
import icu.etl.test.bean.TestLoader;

@EasyBean
public class TestLoaderBuilder implements BeanBuilder<TestLoader> {
    @Override
    public TestLoader build(EasyetlContext context, Object... array) throws Exception {
        return null;
    }
}
