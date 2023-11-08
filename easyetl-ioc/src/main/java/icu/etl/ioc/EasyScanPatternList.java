package icu.etl.ioc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import icu.etl.util.Settings;
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

    public EasyScanPattern get(int position) {
        return this.list.get(position);
    }

    public int size() {
        return this.list.size();
    }

    public List<EasyScanPattern> getScanPattern() {
        List<EasyScanPattern> list = new ArrayList<EasyScanPattern>();
        for (EasyScanPattern pattern : this.list) {
            if (!pattern.isExclude()) {
                list.add(pattern);
            }
        }
        return list;
    }

    public void addProperty() {
        this.addProperty(ClassScanner.PROPERTY_SCANNPKG);
    }

    public void addProperty(String key) {
        if (StringUtils.isNotBlank(key)) {
            String value = System.getProperty(key);
            if (StringUtils.isNotBlank(value)) {
                String[] array = StringUtils.split(value, DELIMITER);
                this.addAll(array);
            }
        }
    }

    public void addArgument(String... args) {
        if (args != null) {
            for (String value : args) {
                String[] array = StringUtils.split(value, DELIMITER);
                this.addAll(array);
            }
        }
    }

    public void addAll(Collection<String> c) {
        if (c != null) {
            for (String str : c) {
                this.add(str);
            }
        }
    }

    public void exclude(Collection<String> c) {
        if (c != null) {
            for (String str : c) {
                if (str != null) {
                    this.add("!" + str);
                }
            }
        }
    }

    private void addAll(String[] args) {
        if (args != null) {
            for (String str : args) {
                this.add(str);
            }
        }
    }

    public boolean addGroupID() {
        return this.addFirst(Settings.getGroupID());
    }

    /**
     * 添加到第一个位置上
     *
     * @param value 包名
     */
    public boolean addFirst(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        EasyScanPattern obj = new EasyScanPattern(value);
        if (obj.isBlank()) {
            return false;
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
        return true;
    }

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
