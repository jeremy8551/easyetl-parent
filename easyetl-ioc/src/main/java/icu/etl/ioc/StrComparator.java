package icu.etl.ioc;

import java.util.Comparator;

import icu.etl.annotation.EasyBeanClass;

/**
 * 字符串比较类
 *
 * @author jeremy8551@qq.com
 * @createtime 2017-03-27
 */
@EasyBeanClass(type = Comparator.class, kind = "default", mode = "", major = "", minor = "", description = "常规的字符串比较规则")
public class StrComparator extends icu.etl.util.StringComparator {

}
