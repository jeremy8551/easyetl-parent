package icu.etl.ioc;

import java.util.Comparator;

import icu.etl.annotation.EasyBean;
import icu.etl.util.StringUtils;

@EasyBean(name = "int", description = "将字符串转为整数后比较")
public class StrAsIntComparator implements Comparator<String> {

    public int compare(String o1, String o2) {
        return Integer.parseInt(StringUtils.trimBlank(o1)) - Integer.parseInt(StringUtils.trimBlank(o2));
    }

}
