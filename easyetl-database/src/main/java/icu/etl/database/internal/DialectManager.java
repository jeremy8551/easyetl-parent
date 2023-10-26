package icu.etl.database.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.database.DatabaseDialect;
import icu.etl.ioc.BeanInfo;
import icu.etl.ioc.EasyetlContext;
import icu.etl.ioc.RepeatDefineBeanException;
import icu.etl.util.StringUtils;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class DialectManager implements Comparator<DialectManager.DialectInfo> {

    private CaseSensitivMap<List<DialectInfo>> map;

    public static class DialectInfo {
        String name;
        Class<?> cls;
        String major;
        String minor;
        int level;

        public DialectInfo(String name, Class<?> cls, String major, String minor, int level) {
            this.name = name;
            this.cls = cls;
            this.major = StringUtils.trimBlank(major);
            this.minor = StringUtils.trimBlank(minor);
            this.level = level;
        }
    }

    public DialectManager(EasyetlContext context, List<BeanInfo> list) {
        this.map = new CaseSensitivMap<List<DialectInfo>>();
        for (BeanInfo beanInfo : list) {
            this.add(context, beanInfo);
        }
    }

    /**
     * 根据字符串参数 {@code str} 解析数据库组件名
     *
     * @param str 字符串
     * @return 组件名, 如：db2 oracle mysql
     */
    public String parse(String str) {
        String lower = str.toLowerCase();
        Set<String> names = this.map.keySet();
        for (Iterator<String> it = names.iterator(); it.hasNext(); ) {
            String name = it.next();
            if (lower.contains(name.toLowerCase())) {
                return name;
            }
        }
        throw new UnsupportedOperationException(str);
    }

    public void add(EasyetlContext context, BeanInfo beanInfo) {
        List<DialectInfo> list = this.map.get(beanInfo.getName());
        if (list == null) {
            list = new ArrayList<DialectInfo>();
            this.map.put(beanInfo.getName(), list);
        }

        DatabaseDialect dialect = context.createBean(beanInfo.getType());
        DialectInfo e = new DialectInfo(beanInfo.getName(), beanInfo.getType(), dialect.getDatabaseMajorVersion(), dialect.getDatabaseMinorVersion(), beanInfo.getOrder());

        Collections.sort(list, this);
        if (list.size() > 0) {
            DialectInfo first = list.get(0);
            if (first.major.equals(e.major) && first.minor.equals(e.minor)) {
                if (first.level < e.level) {
                    list.set(0, e);
                }
            }
        }

        list.add(e);
    }

    public int compare(DialectInfo o1, DialectInfo o2) {
        int val = o1.name.compareTo(o2.name);
        if (val == 0) {
            int v1 = o1.major.compareTo(o2.major);
            if (v1 == 0) {
                int v2 = o1.minor.compareTo(o2.minor);
                if (v2 == 0) {
                    return o2.level - o1.level; // 倒序排序
                } else {
                    return v2;
                }
            } else {
                return v1;
            }
        } else {
            return val;
        }
    }

    public Class<?> getDialectClass(String name, String major, String minor) {
        List<DialectInfo> list = this.map.get(name);
        if (list == null) {
            throw new UnsupportedOperationException(name);
        }

        List<DialectInfo> cl = new ArrayList<>();
        if (StringUtils.isNotBlank(major) || StringUtils.isNotBlank(minor)) {
            for (DialectInfo e : list) {
                if (major.equals(e.major) && minor.equals(e.minor)) {
                    cl.add(e);
                }
            }

            if (cl.size() == 0) {
                // 如果版本号都不匹配，则取默认的方言类
            } else if (cl.size() == 1) {
                return cl.get(0).cls;
            } else {
                throw new RepeatDefineBeanException(DatabaseDialect.class, name, list);
            }
        }

        // 取默认的数据库方言（版本号都为空字符串的）
        for (DialectInfo e : list) {
            if ("".equals(e.major) && "".equals(e.minor)) {
                cl.add(e);
            }
        }

        if (cl.size() == 0) {
            throw new UnsupportedOperationException(name);
        } else if (cl.size() == 1) {
            return cl.get(0).cls;
        } else {
            throw new RepeatDefineBeanException(DatabaseDialect.class, name, list);
        }
    }

}
