package icu.etl.script.command;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.collection.CaseSensitivSet;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.util.Attribute;
import icu.etl.util.StringUtils;

/**
 * 脚本命令属性集合
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-26
 */
public class CommandAttribute implements Attribute<String> {

    /** 属性信息 */
    private Map<String, String> attributes;

    /** 有属性值的范围 */
    private Set<String> value;

    /** 无属性值的范围 */
    private Set<String> novalue;

    /**
     * 初始化
     */
    public CommandAttribute() {
        this.attributes = new CaseSensitivMap<String>();
        this.value = new CaseSensitivSet();
        this.novalue = new CaseSensitivSet();
    }

    /**
     * 初始化
     *
     * @param names 支持的属性名数组（添加数组之外的属性会抛出异常）<br>
     *              属性名右侧没有半角冒号表示不存在属性值（会抛出异常）<br>
     *              属性名右侧使用半角冒号表示存在属性值 <br>
     */
    public CommandAttribute(String... names) {
        this();
        if (names.length == 0) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (name == null) {
                throw new NullPointerException();
            } else if (name.endsWith(":")) {
                this.value.add(StringUtils.removeSuffix(name));
            } else {
                this.novalue.add(name);
            }
        }
    }

    /**
     * 返回属性信息
     *
     * @return 属性集合
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public boolean contains(String key) {
        return this.attributes.containsKey(key);
    }

    public String getAttribute(String key) {
        return this.attributes.get(key);
    }

    /**
     * 返回整数型属性值
     *
     * @param key 属性名（大小写不敏感）
     * @return 整数
     */
    public int getIntAttribute(String key) {
        return Integer.parseInt(this.attributes.get(key));
    }

    /**
     * 返回属性值
     *
     * @param key   属性名
     * @param array 默认值数组
     * @return 属性值
     */
    public String getAttribute(String key, String... array) {
        String value = this.attributes.get(key);
        if (StringUtils.isBlank(value)) {
            for (String str : array) {
                if (StringUtils.isNotBlank(str)) {
                    return str;
                }
            }
        }
        return value;
    }

    public void setAttribute(String name, String value) {
        if (value != null && value.length() > 0 && this.novalue.contains(name)) {
            throw new UnsupportedOperationException(name);
        } else if (!this.value.contains(name) && !this.novalue.contains(name)) {
            throw new UnsupportedOperationException(name);
        } else {
            this.attributes.put(name, value);
        }
    }

    public CommandAttribute clone(UniversalScriptSession session, UniversalScriptContext context) {
        CommandAttribute obj = new CommandAttribute();
        obj.value.addAll(this.value);
        obj.novalue.addAll(this.novalue);

        UniversalScriptAnalysis analysis = session.getAnalysis();
        Iterator<String> it = this.attributes.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = this.attributes.get(key);
            String newvalue = analysis.replaceShellVariable(session, context, value, true, true, true, false);
            obj.attributes.put(key, newvalue);
        }
        return obj;
    }

    public CommandAttribute clone() {
        CommandAttribute obj = new CommandAttribute();
        obj.attributes.putAll(this.attributes);
        obj.value.addAll(this.value);
        obj.novalue.addAll(this.novalue);
        return obj;
    }
}
