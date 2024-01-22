package icu.etl.database.load;

import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;

public enum LoadMode {

    REPLACE("replace"), INSERT("insert"), MERGE("merge");

    /** 模式名 */
    private String name;

    /**
     * 初始化
     *
     * @param str 字符串
     */
    LoadMode(String str) {
        this.name = Ensure.notNull(str).toUpperCase();
    }

    /**
     * 返回数据装载模式名
     *
     * @return 模式名
     */
    public String getName() {
        return this.name;
    }

    /**
     * 将字符串转为数据装载模式
     *
     * @param str 字符串
     * @return 数据装载模式
     */
    public static LoadMode valueof(String str) {
        if ("insert".equalsIgnoreCase(str)) {
            return INSERT;
        } else if ("replace".equalsIgnoreCase(str)) {
            return REPLACE;
        } else if ("merge".equalsIgnoreCase(str)) {
            return MERGE;
        } else {
            throw new UnsupportedOperationException(ResourcesUtils.getMessage("load.standard.output.msg015"));
        }
    }

    /**
     * 判断字符串参数 str 是否是数据装载模式
     *
     * @param str 字符串
     * @return 返回 true 表示字符串参数 str 是数据装载模式
     */
    public static boolean isMode(String str) {
        if ("insert".equalsIgnoreCase(str)) {
            return true;
        } else if ("replace".equalsIgnoreCase(str)) {
            return true;
        } else {
            return "merge".equalsIgnoreCase(str);
        }
    }
}
