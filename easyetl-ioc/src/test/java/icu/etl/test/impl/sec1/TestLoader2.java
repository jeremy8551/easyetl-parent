package icu.etl.test.impl.sec1;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.test.bean.TestLoader;

@EasyBeanClass(type = TestLoader.class, kind = "2", mode = "", major = "", minor = "", description = "")
public class TestLoader2 implements TestLoader {
    @Override
    public void print() {
        System.out.println(TestLoader2.class.getName());
    }
}
