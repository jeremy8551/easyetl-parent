package icu.etl.ioc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import icu.etl.util.ClassUtils;

/**
 * 组件容器
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/25
 */
class Ioc {

    /** 容器 */
    private Map<String, Object> ioc;

    public Ioc() {
        this.ioc = Collections.synchronizedMap(new LinkedHashMap<String, Object>(50));
    }

    public <E> List<E> getBean(Class<E> cls) {
        Collection<Object> values = this.ioc.values();
        List<E> list = new ArrayList<E>(values.size());
        for (Iterator<Object> it = values.iterator(); it.hasNext(); ) {
            Object obj = it.next();
            Class<?> objClass = obj.getClass();

            // 判断接口
            if (cls.isInterface() && ClassUtils.isInterfacePresent(objClass, cls)) {
                list.add((E) obj);
                continue;
            }

            // 判断类名
            if (ClassUtils.isExtendClass(objClass, cls)) {
                list.add((E) obj);
                continue;
            }
        }
        return list;
    }

}
