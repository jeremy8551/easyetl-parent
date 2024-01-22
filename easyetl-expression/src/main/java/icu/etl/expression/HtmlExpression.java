package icu.etl.expression;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.util.Attribute;
import icu.etl.util.Property;
import icu.etl.util.StringUtils;
import icu.etl.util.XMLUtils;

public class HtmlExpression implements Attribute<String> {

    /** 命名空间 */
    private String namespace;

    /** 标签名 */
    private String name;

    /** 属性集合 */
    private CaseSensitivMap<String> attributes;

    /** 文本内容 */
    private String text;

    /**
     * 解析xml表达式
     *
     * @param xml 表达式 <br>
     *            {@literal <html key=value />} <br>
     *            {@literal <html key=value > </html>}
     */
    public HtmlExpression(String xml) {
        this.attributes = new CaseSensitivMap<String>();
        this.namespace = "";
        this.text = "";
        this.parse(xml);
    }

    /**
     * 解析 XML 标签
     *
     * @param xml 标签
     */
    private void parse(String xml) {
        String str = StringUtils.trimBlank(xml);
        if (!str.startsWith("<") || !str.endsWith(">")) {
            throw new IllegalArgumentException(xml);
        }

        int start = StringUtils.indexOfNotBlank(str, 1, -1);
        if (start == -1) {
            throw new IllegalArgumentException(str);
        }

        int end = this.indexOfNameEnd(str, start + 1);
        if (end == -1) {
            throw new IllegalArgumentException(str);
        }

        String prefix = str.substring(start, end);
        String[] array = StringUtils.split(prefix, ':');
        if (array.length == 1) {
            this.name = array[0];
        } else if (array.length == 2) {
            this.namespace = array[0];
            this.name = array[1];
        } else {
            throw new IllegalArgumentException(str);
        }

        if (str.endsWith("/>")) { // 样式: <xxx name=value />
            int m = str.length() - 2;
            String attrExpr = str.substring(end, m);
            List<Property> attrs = XMLUtils.splitXmlProperties(attrExpr);
            for (Property p : attrs) {
                this.attributes.put(p.getKey(), p.getString());
            }
        } else { // <xxx > </xxx>
            AnalysisImpl analysis = new AnalysisImpl();
            int m = analysis.indexOf(str, ">", 1, 2, 2);
            if (m == -1) {
                throw new IllegalArgumentException(str);
            }

            String attrExpr = str.substring(end, m);
            List<Property> attrs = XMLUtils.splitXmlProperties(attrExpr);
            for (Property p : attrs) {
                this.attributes.put(p.getKey(), p.getString());
            }

            int t = analysis.lastIndexOf(str, "</", str.length() - 1, 2, 2);
            if (t == -1) {
                throw new IllegalArgumentException(str);
            }

            // 如果最后不是 </xxx> 结尾，则从字符 > 开始一直截取到最后一个字符作为标签的文本
            String ns = StringUtils.removeBlank(str.substring(t));
            if (!StringUtils.startsWith(ns, "</" + this.name + ">", 0, true, true)) {
                t = str.length();
            }

            this.text = str.substring(m + 1, t);
        }
    }

    /**
     * 从指定位置开始搜索字符串中标记名结束的位置
     *
     * @param str  字符串
     * @param from 搜索起始位置
     * @return 位置
     */
    protected int indexOfNameEnd(String str, int from) {
        for (int i = from; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!StringUtils.isLetter(c) && c != ':') {
                return i;
            }
        }
        return -1;
    }

    /**
     * 返回命名空间
     *
     * @return 命名空间
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * 返回标签名
     *
     * @return 标签名
     */
    public String getTagName() {
        return this.name;
    }

    /**
     * 返回属性值
     *
     * @param name 属性名（忽略大小写）
     * @return 属性值
     */
    public String getAttribute(String name) {
        return this.attributes.get(name);
    }

    /**
     * 判断是否存在属性名
     *
     * @param name 属性名（忽略大小写）
     * @return 返回true表示存在属性名 false表示不存在属性名
     */
    public boolean contains(String name) {
        return this.attributes.containsKey(name);
    }

    /**
     * 添加一个属性
     */
    public void setAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    /**
     * 返回属性集合
     *
     * @return 属性名与属性值的映射集合
     */
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(this.attributes);
    }

    /**
     * 返回标记的文本内容
     *
     * @return 文本内容
     */
    public String getText() {
        return this.text;
    }

}
