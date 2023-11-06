package icu.etl.ioc;

import java.util.List;

import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;

/**
 * 重复定义组件错误
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/25
 */
public class RepeatDefineBeanException extends RuntimeException {

    /**
     * 重复定义组件错误
     *
     * @param type 组件类
     * @param name 组件名
     * @param list 重复的组件
     */
    public RepeatDefineBeanException(Class<?> type, String name, List<?> list) {
        super(ResourcesUtils.getClassMessage(13, name, type == null ? "" : " " + type.getName() + " ", toBeanInfoList(list)));
    }

    protected static String toBeanInfoList(List<?> list) {
        StringBuilder buf = new StringBuilder(FileUtils.lineSeparator);
        for (Object obj : list) {
            buf.append(toString(obj)).append(FileUtils.lineSeparator);
        }
        return buf.toString();
    }

    protected static String toString(Object obj) {
        if (obj instanceof Class) {
            return ((Class) obj).getName();
        } else if (obj instanceof BeanInfoRegister) {
            return ((BeanInfoRegister) obj).getType().getName();
        } else {
            return obj.getClass().getName();
        }
    }
}
