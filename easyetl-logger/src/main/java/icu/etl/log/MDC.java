package icu.etl.log;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志MDC类
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class MDC {

    /** 本地变量 */
    private static ThreadLocal<Map<String, String>> local = new ThreadLocal<Map<String, String>>();

    /**
     * 返回属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    public static String get(String key) {
        Map<String, String> map = get();
        return map.get(key);
    }

    /**
     * 设置属性
     *
     * @param key   属性名
     * @param value 属性值
     * @return 原来保存的属性值
     */
    public static String put(String key, String value) {
        Map<String, String> map = get();
        return map.put(key, value);
    }

    /**
     * 返回属性集合
     *
     * @return 属性集合
     */
    public static Map<String, String> get() {
        Map<String, String> map = local.get();
        if (map == null) {
            map = new HashMap<String, String>();
            local.set(map);
        }
        return map;
    }

    /**
     * 清空所有属性
     */
    public static void clear() {
        local.set(null);
    }
}
