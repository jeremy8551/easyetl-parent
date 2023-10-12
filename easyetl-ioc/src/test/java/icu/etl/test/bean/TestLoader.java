package icu.etl.test.bean;

import icu.etl.annotation.EasyBean;
import icu.etl.test.loader.TestLoaderBuilder;

@EasyBean(builder = TestLoaderBuilder.class)
public interface TestLoader {

    void print();
}
