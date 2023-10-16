package icu.etl.script.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import icu.etl.collection.CaseSensitivSet;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptVariableMethod;
import icu.etl.util.CharTable;
import icu.etl.util.ClassUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎中变量方法的工厂类
 *
 * @author jeremy8551@qq.com
 */
public class VariableMethodRepository {

    /** 变量方法名与变量方法的映射关系, 变量方法名全部为大写字母 */
    private HashMap<String, UniversalScriptVariableMethod> map;

    /**
     * 初始化变量方法集合
     *
     * @param context 脚本引擎上下文信息
     */
    public VariableMethodRepository(UniversalScriptContext context) {
        this.map = new HashMap<String, UniversalScriptVariableMethod>();
        new VariableMethodScanner(context, this);
    }

    /**
     * 判断变量方法是否存在
     *
     * @param cls 变量方法的类信息
     * @return
     */
    public boolean contains(Class<? extends UniversalScriptVariableMethod> cls) {
        for (Iterator<UniversalScriptVariableMethod> it = this.map.values().iterator(); it.hasNext(); ) {
            UniversalScriptVariableMethod method = it.next();
            if (method != null && method.getClass().getName().equals(cls.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加一个变量方法的工厂
     *
     * @param name   变量方法名
     * @param method 变量方法
     */
    public void add(String name, UniversalScriptVariableMethod method) {
        this.map.put(name.toUpperCase(), method);
    }

    /**
     * 返回脚本变量方法
     *
     * @param name 变量方法名
     * @return
     */
    public UniversalScriptVariableMethod get(String name) {
        UniversalScriptVariableMethod method = this.map.get(name.toUpperCase());
        if (method == null) {
            return method;
        } else {
            String className = method.getClass().getName();
            return ClassUtils.newInstance(className);
        }
    }

    /**
     * 打印所有变量方法的使用说明
     */
    public String toString() {
        return this.toString(null, false);
    }

    /**
     * 返回所有脚本变量方法的使用说明
     *
     * @param charsetName 字符集
     * @param ltrim       true表示删除表格左侧的空白字符
     * @return
     */
    public String toString(String charsetName, boolean ltrim) {
        String[] titles = StringUtils.split(ResourcesUtils.getMessage("script.engine.usage.msg004"), ',');
        CharTable ct = new CharTable(charsetName);
        ct.addTitle(CharTable.ALIGN_LEFT, titles[0]);
        ct.addTitle(CharTable.ALIGN_LEFT, titles[1]);
        ct.addTitle(CharTable.ALIGN_LEFT, titles[2]);
        ct.addTitle(CharTable.ALIGN_LEFT, titles[3]);
        ct.addTitle(CharTable.ALIGN_LEFT, titles[4]);
        ct.addTitle(CharTable.ALIGN_LEFT, titles[5]);

        // 全部转为小写英文字符
        List<String> methodNameList = ResourcesUtils.getPropertyMiddleName("script.variable.method");
        CaseSensitivSet set = new CaseSensitivSet();
        set.addAll(ResourcesUtils.getPropertyMiddleName("script.variable.method"));

        List<String> names = new ArrayList<String>(this.map.keySet()); // 所有有效变量方法名的集合

        // 按变量方法的使用说明顺序排序
        Collections.sort(names, new ComparatorImpl(methodNameList));

        // 按变量方法的使用说明顺序打印变量方法的使用说明
        for (String name : names) {
            UniversalScriptVariableMethod obj = this.map.get(name);

            String methodName = set.contains(name) ? set.get(name) : "";
            ct.addValue(name); // 显示注解中配置的变量方法名
            ct.addValue(ResourcesUtils.getMessage("script.variable.method." + methodName + ".synopsis"));
            ct.addValue(ResourcesUtils.getMessage("script.variable.method." + methodName + ".descriptions"));
            ct.addValue(ResourcesUtils.getMessage("script.variable.method." + methodName + ".parameters"));
            ct.addValue(ResourcesUtils.getMessage("script.variable.method." + methodName + ".return"));
            ct.addValue(obj.getClass().getName());
        }

        // 删除表格左侧的空白字符
        if (ltrim) {
            ct.removeLeftBlank();
        }
        return ct.toStandardShape();
    }

    private class ComparatorImpl implements Comparator<String> {

        private List<String> usage;

        public ComparatorImpl(List<String> usage) {
            super();
            this.usage = StringUtils.toCase(usage, true, null);
        }

        public int compare(String n1, String n2) {
            int i1 = this.usage.indexOf(n1.toLowerCase()); // 查询变量方法在使用说明中的位置
            if (i1 == -1) {
                i1 = Integer.MAX_VALUE; // 无使用说明放到最后
            }

            int i2 = this.usage.indexOf(n2.toLowerCase()); // 查询变量方法在使用说明中的位置
            if (i2 == -1) {
                i2 = Integer.MAX_VALUE;
            }

            return i1 - i2;
        }
    }

    /**
     * 返回 true 表示未加载任何脚本引擎变量方法
     *
     * @return
     */
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    /**
     * 清空所有信息
     */
    public void clear() {
        this.map.clear();
    }

}