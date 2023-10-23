package icu.etl.ioc;

import icu.etl.util.Ensure;
import org.junit.Test;

public class CodepageTest {

    @Test
    public void test() {
        BeanContext context = new BeanContext();
        Codepage bean = context.get(Codepage.class);
        Ensure.isTrue("utf-8".equalsIgnoreCase(bean.get("1208")));
        Ensure.isTrue("utf-8".equalsIgnoreCase(bean.get(1208)));
        Ensure.isTrue("GBK".equalsIgnoreCase(bean.get(1386)));
        Ensure.isTrue("1208".equalsIgnoreCase(bean.get("utf-8")));
        Ensure.isTrue("1386".equalsIgnoreCase(bean.get("GBK")));
    }

}
