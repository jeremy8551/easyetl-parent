package icu.etl.ioc;

import java.util.List;

import icu.etl.test.bean.TestLoader;
import icu.etl.test.impl.TestLoader1;
import icu.etl.test.impl.sec1.TestLoader2;
import org.junit.Assert;
import org.junit.Test;

public class ClassScannerTest {

    @Test
    public void test() {
        AnnotationEasyetlContext context = new AnnotationEasyetlContext("sout:debug,!org.apache,!icu.etl.test.impl.sec1,");
        List<BeanInfo> list = context.getBeanInfoList(TestLoader.class);
        boolean exists = false;
        boolean exists1 = false;
        for (BeanInfo anno : list) {
            if (anno.getType().equals(TestLoader2.class)) {
                exists = true;
            }
            if (anno.getType().equals(TestLoader1.class)) {
                exists1 = true;
            }
        }
        Assert.assertFalse(exists);
        Assert.assertTrue(exists1);
    }

}
