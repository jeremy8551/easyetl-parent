package icu.etl.test.impl;

import icu.etl.annotation.EasyBean;
import icu.etl.test.bean.TestLoader;

@EasyBean(name = "1", description = "")
public class TestLoader1 implements TestLoader {
    @Override
    public void print() {
        System.out.println(TestLoader1.class.getName());
    }
}
