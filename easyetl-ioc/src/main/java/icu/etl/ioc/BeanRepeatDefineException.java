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
public class BeanRepeatDefineException extends RuntimeException {

    /**
     * 重复定义组件错误
     *
     * @param type 组件类
     * @param name 组件名
     * @param list 重复的组件
     */
    public BeanRepeatDefineException(Class<?> type, String name, List<?> list) {
        super(ResourcesUtils.getClassMessage(13, name, type == null ? "" : " " + type.getName() + " ", toBeanInfoList(list)));
    }

    /**
     * 将组件信息转为字符串
     *
     * @param list 组件信息集合
     * @return 字符串
     */
    protected static String toBeanInfoList(List<?> list) {
        StringBuilder buf = new StringBuilder(FileUtils.lineSeparator);
        for (Object obj : list) {
            buf.append(toString(obj)).append(FileUtils.lineSeparator);
        }
        return buf.toString();
    }

    /**
     * 将实例对象转为字符串
     *
     * @param obj 实例对象
     * @return 字符串
     */
    protected static String toString(Object obj) {
        if (obj instanceof Class) {
            return ((Class<?>) obj).getName();
        } else if (obj instanceof EasyBeanInfoValue) {
            return ((EasyBeanInfoValue) obj).getType().getName();
        } else {
            return obj.getClass().getName();
        }
    }
}
