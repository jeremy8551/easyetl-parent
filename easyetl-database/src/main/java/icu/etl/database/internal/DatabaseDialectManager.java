package icu.etl.database.internal;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.database.DB;
import icu.etl.database.DatabaseDialect;
import icu.etl.ioc.BeanInfo;
import icu.etl.ioc.BeanInfoFilter;
import icu.etl.ioc.BeanInfoTableRow;
import icu.etl.ioc.BeanInfoRegister;
import icu.etl.ioc.BeanInfoCell;
import icu.etl.ioc.EasyBeanInfo;
import icu.etl.ioc.EasyContext;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class DatabaseDialectManager {

    private CaseSensitivMap<BeanInfoTableRow> map;

    public static class DialectInfo extends EasyBeanInfo {
        String major;
        String minor;

        public DialectInfo(BeanInfo beanInfo, String major, String minor) {
            super(beanInfo.getType());
            this.major = StringUtils.defaultString(major, "");
            this.minor = StringUtils.defaultString(minor, "");
        }

        public int compare(BeanInfo o1, BeanInfo o2) {
            return this.compareTo((DialectInfo) o1, (DialectInfo) o2);
        }

        public int compareTo(DialectInfo o1, DialectInfo o2) {
//            int tc = o1.getType().getName().compareTo(o2.getType().getName());
//            if (tc != 0) {
//                return tc;
//            }

            int ac = o1.getName().compareTo(o2.getName());
            if (ac != 0) {
                return ac;
            }

            int jc = o1.major.compareTo(o2.major);
            if (jc != 0) {
                return jc;
            }

            int nc = o1.minor.compareTo(o2.minor);
            if (nc != 0) {
                return nc;
            }

            return o1.getPriority() - o2.getPriority(); // 倒序排序
        }
    }

    public DatabaseDialectManager(EasyContext context, List<BeanInfo> list) {
        this.map = new CaseSensitivMap<BeanInfoTableRow>();
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
    public String parseBeanName(String str) {
        String lower = str.toLowerCase();
        Set<String> names = this.map.keySet();
        for (Iterator<String> it = names.iterator(); it.hasNext(); ) {
            String name = it.next();
            if (lower.contains(name.toLowerCase())) {
                return name;
            }
        }

        throw new UnsupportedOperationException(ResourcesUtils.getDatabaseMessage(5, str));
    }

    public synchronized void add(EasyContext context, BeanInfo beanInfo) {
        BeanInfoTableRow list = this.map.get(beanInfo.getName());
        if (list == null) {
            list = new BeanInfoTableRow(DatabaseDialect.class);
            this.map.put(beanInfo.getName(), list);
        }

        // 创建一个数据库方言的实例
        DatabaseDialect dialect;
        if (beanInfo instanceof BeanInfoCell) {
            BeanInfoCell cell = (BeanInfoCell) beanInfo;
            dialect = (cell.getBean() == null) ? context.createBean(beanInfo.getType()) : cell.getBean();
            cell.setBean(dialect);
        } else {
            dialect = context.createBean(beanInfo.getType());
        }

        // 注册方言
        DialectInfo dialectInfo = new DialectInfo(beanInfo, dialect.getDatabaseMajorVersion(), dialect.getDatabaseMinorVersion());
        if (!list.contains(dialectInfo)) {
            if (DB.out.isDebugEnabled()) {
                DB.out.debug(ResourcesUtils.getDatabaseMessage(3, beanInfo.getName(), beanInfo.getType().getName())); // 注册数据库方言类 {0} -> {1}
            }
            list.add(dialectInfo);
            list.sortByDesc();
        }
    }

    public Class<?> getDialectClass(String name, String major, String minor) {
        BeanInfoTableRow list = this.map.get(name);
        if (list == null) {
            throw new UnsupportedOperationException(ResourcesUtils.getDatabaseMessage(5, name));
        }

        // 如果查询条件包含版本号，则根据版本号进行过滤
        if (StringUtils.isNotBlank(major) || StringUtils.isNotBlank(minor)) {
            BeanInfoTableRow row = list.indexOf(new BeanInfoFilter() {
                public boolean accept(BeanInfoRegister beanInfo) {
                    DialectInfo dialectInfo = (DialectInfo) beanInfo;
                    return dialectInfo.major.equals(major) && dialectInfo.minor.equals(minor);
                }
            });

            BeanInfoRegister beanInfo = row.getBeanInfo();
            if (beanInfo != null) {
                return beanInfo.getType();
            }
        }

        // 如果没有与数据库版本匹配的方言类，则取版本号为空的作为默认
        BeanInfoRegister beanInfo = list.indexOf(new BeanInfoFilter() {
            public boolean accept(BeanInfoRegister beanInfo) {
                DialectInfo dialectInfo = (DialectInfo) beanInfo;
                return dialectInfo.major.equals("") && dialectInfo.minor.equals("");
            }
        }).getBeanInfo();

        if (beanInfo == null) {
            throw new UnsupportedOperationException(ResourcesUtils.getDatabaseMessage(5, name));
        } else {
            return beanInfo.getType();
        }
    }

}
