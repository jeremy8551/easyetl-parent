package icu.etl.ioc;

import java.math.BigDecimal;
import java.util.Comparator;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.util.StringUtils;

@EasyBeanClass(type = Comparator.class, kind = "number", mode = "", major = "", minor = "", description = "将字符串转为浮点数后比较")
public class StrAsNumberComparator implements Comparator<String> {

    public int compare(String o1, String o2) {
        return new BigDecimal(StringUtils.trimBlank(o1)).compareTo(new BigDecimal(StringUtils.trimBlank(o2)));
    }

}
