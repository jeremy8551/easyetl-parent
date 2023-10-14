package icu.etl.database.load;

import icu.etl.util.ResourcesUtils;

public enum LoadMode {

    REPLACE("replace"), INSERT("insert"), MERGE("merge");

    /** 模式名 */
    private String name;

    /**
     * 初始化
     *
     * @param str
     */
    private LoadMode(String str) {
        if (str == null) {
            throw new NullPointerException();
        } else {
            this.name = str.toUpperCase();
        }
    }

    /**
     * 返回数据装载模式名
     *
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * 将字符串转为数据装载模式
     *
     * @param str
     * @return
     */
    public final static LoadMode valueof(String str) {
        if ("insert".equalsIgnoreCase(str)) {
            return INSERT;
        } else if ("replace".equalsIgnoreCase(str)) {
            return REPLACE;
        } else if ("merge".equalsIgnoreCase(str)) {
            return MERGE;
        } else {
            throw new UnsupportedOperationException(ResourcesUtils.getLoadMessage(15));
        }
    }

    /**
     * 返回 true 表示字符串参数 str 是数据装载模式
     *
     * @param str
     * @return
     */
    public final static boolean isMode(String str) {
        if ("insert".equalsIgnoreCase(str)) {
            return true;
        } else if ("replace".equalsIgnoreCase(str)) {
            return true;
        } else if ("merge".equalsIgnoreCase(str)) {
            return true;
        } else {
            return false;
        }
    }
}
