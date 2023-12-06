package icu.etl.ioc.scan;

import java.util.List;

import icu.etl.ioc.EasyBeanInfo;
import icu.etl.ioc.EasyContext;
import icu.etl.ioc.impl.EasyBeanInfoImpl;
import icu.etl.util.SPI;

/**
 * 使用 SPI 机制扫描组件
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/29
 */
public class BeanSpiScanner {

    public int load(EasyContext context) {
        ClassLoader classLoader = context.getClassLoader();
        List<EasyBeanInfo> list = SPI.load(classLoader, EasyBeanInfo.class);
        for (EasyBeanInfo bean : list) {
            context.addBean(new EasyBeanInfoImpl(bean));
        }
        return list.size();
    }
}
