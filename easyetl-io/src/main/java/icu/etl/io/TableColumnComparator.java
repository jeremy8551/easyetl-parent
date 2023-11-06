package icu.etl.io;

import icu.etl.util.StringComparator;
import icu.etl.util.StringUtils;

public class TableColumnComparator extends StringComparator {

    public int compare(String str1, String str2) {
        String value1 = StringUtils.trimBlank(str1);
        String value2 = StringUtils.trimBlank(str2);
        return value1.compareTo(value2);
    }

}
