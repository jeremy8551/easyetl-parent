package icu.etl.database.load;

import icu.etl.util.ResourcesUtils;

public enum IndexMode {

    /**
     * 重建索引
     */
    REBUILD("rebuild"), //

    /**
     * 保留原有索引
     */
    INCREMENTAL("incremental"), //

    /**
     * 默认值：由程序自主选择
     */
    AUTOSELECT("autoselect");

    /** 模式名 */
    private String name;

    private IndexMode(String str) {
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
        return name;
    }

    /**
     * 将字符串转为索引处理模式
     *
     * @param str
     * @return
     */
    public static IndexMode valueof(String str) {
        if ("rebuild".equalsIgnoreCase(str)) {
            return REBUILD;
        } else if ("incremental".equalsIgnoreCase(str)) {
            return INCREMENTAL;
        } else if ("autoselect".equalsIgnoreCase(str)) {
            return AUTOSELECT;
        } else {
            throw new UnsupportedOperationException(ResourcesUtils.getMessage("load.standard.output.msg016", REBUILD.getName(), INCREMENTAL.getName(), AUTOSELECT.getName()));
        }
    }

    /**
     * 返回 true 表示字符串参数 str 是索引处理模式
     *
     * @param str
     * @return
     */
    public static boolean isMode(String str) {
        if ("rebuild".equalsIgnoreCase(str)) {
            return true;
        } else if ("incremental".equalsIgnoreCase(str)) {
            return true;
        } else {
            return "autoselect".equalsIgnoreCase(str);
        }
    }

}
