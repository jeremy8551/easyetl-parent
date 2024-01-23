package icu.etl.ioc.scan;

import icu.etl.util.StringUtils;

/**
 * 包扫描通配符
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/8
 */
public class EasyScanPattern {

    public final static char[] EXCLUDE_CHARS = {'^', '!', '！', StringUtils.toFullWidthChar('^')};

    /** 源名: !org.spring.* */
    private String value;

    /** true表示排除模式 */
    private boolean exclude;

    /** 匹配模式，如：org.spring.* */
    private String pattern;

    /** 包名前缀，如：org.spring */
    private String prefix;

    /** 包名，如 !org.spring */
    private String rule;

    public EasyScanPattern(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException(name);
        }

        this.value = StringUtils.trimBlank(name);
        this.exclude = StringUtils.inArray(this.value.charAt(0), EXCLUDE_CHARS);
        this.rule = StringUtils.rtrim(this.value, '.', '*', '?');
        this.pattern = this.exclude ? StringUtils.ltrim(this.value, EXCLUDE_CHARS) : this.value;
        this.prefix = this.parse(this.pattern);
    }

    /**
     * 删除通配符中的匹配字符(* ?)
     *
     * @param str 通配符
     * @return 包名
     */
    protected String parse(String str) {
        String[] array = StringUtils.split(str, '.');
        StringBuilder buf = new StringBuilder(str.length());
        for (int i = 0; i < array.length; i++) {
            String name = array[i];
            if (name.indexOf('*') != -1 || name.indexOf('?') != -1) {
                break;
            } else {
                buf.append(name).append('.');
            }
        }
        return StringUtils.rtrim(buf, '.');
    }

    /**
     * 扫描或排除
     *
     * @return true表示排除模式 false表示扫描模式
     */
    public boolean isExclude() {
        return exclude;
    }

    /**
     * 包名前缀
     *
     * @return 包名前缀，如：org.spring
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 返回true表示包名长度为0
     *
     * @return 返回true表示包名是空的
     */
    public boolean isBlank() {
        return this.prefix.length() == 0;
    }

    /**
     * 包名，如 !org.spring
     *
     * @return 返回扫描规则
     */
    public String getRule() {
        return rule;
    }

    /**
     * 判断是否包含了参数 {@code easyPackage} 指定的范围
     *
     * @param pattern 包名
     * @return 返回true表示已包含 false表示未包含
     */
    public boolean contains(EasyScanPattern pattern) {
        return (pattern.isExclude() == this.isExclude()) && pattern.getPrefix().startsWith(this.getPrefix());
    }

    public boolean equals(Object obj) {
        if (obj instanceof EasyScanPattern) {
            EasyScanPattern e = (EasyScanPattern) obj;
            return e.getRule().equals(this.getRule());
        }
        return false;
    }

    public String toString() {
        return this.value;
    }
}
