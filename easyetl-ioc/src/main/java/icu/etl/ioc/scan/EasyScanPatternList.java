package icu.etl.ioc.scan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import icu.etl.Easyetl;
import icu.etl.util.StringUtils;

/**
 * 扫描通配符的集合
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/8
 */
public class EasyScanPatternList {

    public final static char DELIMITER = ',';

    private List<EasyScanPattern> list;

    public EasyScanPatternList() {
        this.list = new ArrayList<EasyScanPattern>();
    }

    /**
     * 查询通配符
     *
     * @param position 位置信息，从0开始
     * @return 通配符
     */
    public EasyScanPattern get(int position) {
        return this.list.get(position);
    }

    /**
     * 返回通配符的个数
     *
     * @return 个数
     */
    public int size() {
        return this.list.size();
    }

    /**
     * 返回包扫描通配符的集合
     *
     * @return 通配符的集合
     */
    public List<EasyScanPattern> getScanPattern() {
        List<EasyScanPattern> list = new ArrayList<EasyScanPattern>();
        for (EasyScanPattern pattern : this.list) {
            if (!pattern.isExclude()) {
                list.add(pattern);
            }
        }
        return list;
    }

    /**
     * 从虚拟机属性值中读取包扫描通配符
     */
    public void addProperty() {
        this.addProperty(ClassScanner.PROPERTY_SCANNPKG);
    }

    /**
     * 添加虚拟机属性信息
     *
     * @param key 属性名
     */
    public void addProperty(String key) {
        if (StringUtils.isNotBlank(key)) {
            String value = System.getProperty(key);
            if (StringUtils.isNotBlank(value)) {
                String[] array = StringUtils.split(value, DELIMITER);
                this.addAll(array);
            }
        }
    }

    /**
     * 添加参数数组
     *
     * @param args 参数数组
     */
    public void addArgument(String... args) {
        if (args != null) {
            for (String value : args) {
                String[] array = StringUtils.split(value, DELIMITER);
                this.addAll(array);
            }
        }
    }

    /**
     * 将集合中的所有字符串，作为包扫描通配符，添加到集合中
     *
     * @param c 包扫描通配符集合
     */
    public void addAll(Collection<String> c) {
        if (c != null) {
            for (String str : c) {
                this.add(str);
            }
        }
    }

    /**
     * 将集合中的所有字符串，作为包排除通配符，添加到集合中
     *
     * @param c 包排除通配符集合
     */
    public void exclude(Collection<String> c) {
        if (c != null) {
            for (String str : c) {
                if (str != null) {
                    this.add("!" + str);
                }
            }
        }
    }

    /**
     * 添加通配符
     *
     * @param args 包扫描通配符数组
     */
    private void addAll(String[] args) {
        if (args != null) {
            for (String str : args) {
                this.add(str);
            }
        }
    }

    /**
     * 将包名添加到集合中的第一个位置上
     */
    public void addGroupID() {
        this.addFirst(Easyetl.class.getPackage().getName());
    }

    /**
     * 添加到第一个位置上
     *
     * @param value 包名
     */
    public void addFirst(String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }

        EasyScanPattern obj = new EasyScanPattern(value);
        if (obj.isBlank()) {
            return;
        }

        while (this.list.size() > 0) {
            boolean loop = false;
            Iterator<EasyScanPattern> it = this.list.iterator();
            while (it.hasNext()) {
                EasyScanPattern next = it.next();
                if (next.equals(obj)) {
                    it.remove();
                    loop = true;
                    break;
                }

                if (next.contains(obj)) { // 已包含
                    obj = next;
                    it.remove();
                    loop = true;
                    break;
                }

                if (obj.contains(next)) { // 替换规则
                    it.remove();
                    loop = true;
                    break;
                }
            }

            if (!loop) {
                break;
            }
        }

        this.list.add(0, obj);
    }

    /**
     * 添加包扫描通配符
     *
     * @param value 包扫描通配符
     * @return 返回true表示添加成功 false表示添加失败
     */
    public boolean add(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        EasyScanPattern obj = new EasyScanPattern(value);
        if (obj.isBlank()) {
            return false;
        }

        for (int i = 0; i < this.list.size(); i++) {
            EasyScanPattern next = this.list.get(i);
            if (next.equals(obj)) { // 不能重复添加
                return false;
            }

            if (next.contains(obj)) { // 已添加
                return false;
            }

            if (obj.contains(next)) { // 替换规则
                this.list.remove(i);
                continue;
            }
        }

        this.list.add(obj);
        return true;
    }

    /**
     * 返回通配符表达式，如: org.test,!org.spring
     *
     * @return 字符串
     */
    public String toArgumentString() {
        StringBuilder buf = new StringBuilder(100);
        for (EasyScanPattern pattern : this.list) {
            buf.append(pattern.getRule()).append(DELIMITER);
        }

        if (buf.length() > 0) {
            buf.setLength(buf.length() - String.valueOf(DELIMITER).length()); // 删除最后一个分隔符
        }
        return buf.toString();
    }

    /**
     * 返回包扫描通配符数组
     *
     * @return 字符串数组
     */
    public String[] toArray() {
        String[] array = new String[this.list.size()];
        for (int i = 0; i < this.list.size(); i++) {
            EasyScanPattern next = this.list.get(i);
            array[i] = next.getRule();
        }
        return array;
    }

    public String toString() {
        return this.toArgumentString();
    }
}
