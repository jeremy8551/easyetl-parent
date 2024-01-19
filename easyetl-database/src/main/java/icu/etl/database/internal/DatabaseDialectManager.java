package icu.etl.database.internal;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.database.DatabaseDialect;
import icu.etl.ioc.EasyBeanInfo;
import icu.etl.ioc.EasyBeanInfoValue;
import icu.etl.ioc.EasyBeanTableFilter;
import icu.etl.ioc.EasyBeanTableRow;
import icu.etl.ioc.EasyBeanValue;
import icu.etl.ioc.EasyContext;
import icu.etl.ioc.impl.EasyBeanInfoImpl;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 数据库方言管理器
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class DatabaseDialectManager {
    private final static Log log = LogFactory.getLog(DatabaseDialectManager.class);

    private CaseSensitivMap<EasyBeanTableRow> map;

    public static class DialectInfo extends EasyBeanInfoImpl {
        String major;
        String minor;

        public DialectInfo(EasyBeanInfo beanInfo, String major, String minor) {
            super(beanInfo.getType());
            this.major = StringUtils.defaultString(major, "");
            this.minor = StringUtils.defaultString(minor, "");
        }

        public int compare(EasyBeanInfo o1, EasyBeanInfo o2) {
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

    public DatabaseDialectManager(EasyContext context, List<EasyBeanInfo> list) {
        this.map = new CaseSensitivMap<EasyBeanTableRow>();
        for (EasyBeanInfo beanInfo : list) {
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

        throw new UnsupportedOperationException(ResourcesUtils.getMessage("database.standard.output.msg005", str));
    }

    public synchronized void add(EasyContext context, EasyBeanInfo beanInfo) {
        EasyBeanTableRow list = this.map.get(beanInfo.getName());
        if (list == null) {
            list = new EasyBeanTableRow(DatabaseDialect.class);
            this.map.put(beanInfo.getName(), list);
        }

        // 创建一个数据库方言的实例
        DatabaseDialect dialect;
        if (beanInfo instanceof EasyBeanValue) {
            EasyBeanValue cell = (EasyBeanValue) beanInfo;
            dialect = (DatabaseDialect) ((cell.getBean() == null) ? context.createBean(beanInfo.getType()) : cell.getBean());
            cell.setBean(dialect);
        } else {
            dialect = context.createBean(beanInfo.getType());
        }

        // 注册方言
        DialectInfo dialectInfo = new DialectInfo(beanInfo, dialect.getDatabaseMajorVersion(), dialect.getDatabaseMinorVersion());
        if (!list.contains(dialectInfo)) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("database.standard.output.msg003", beanInfo.getName(), beanInfo.getType().getName())); // 注册数据库方言类 {0} -> {1}
            }
            list.add(dialectInfo);
            list.sortByDesc();
        }
    }

    public Class<?> getDialectClass(String name, final String major, final String minor) {
        EasyBeanTableRow list = this.map.get(name);
        if (list == null) {
            throw new UnsupportedOperationException(ResourcesUtils.getMessage("database.standard.output.msg005", name));
        }

        // 如果查询条件包含版本号，则根据版本号进行过滤
        if (StringUtils.isNotBlank(major) || StringUtils.isNotBlank(minor)) {
            EasyBeanTableRow row = list.indexOf(new EasyBeanTableFilter() {
                public boolean accept(EasyBeanInfoValue beanInfo) {
                    DialectInfo dialectInfo = (DialectInfo) beanInfo;
                    return dialectInfo.major.equals(major) && dialectInfo.minor.equals(minor);
                }
            });

            EasyBeanInfoValue beanInfo = row.getBeanInfo();
            if (beanInfo != null) {
                return beanInfo.getType();
            }
        }

        // 如果没有与数据库版本匹配的方言类，则取版本号为空的作为默认
        EasyBeanInfoValue beanInfo = list.indexOf(new EasyBeanTableFilter() {
            public boolean accept(EasyBeanInfoValue beanInfo) {
                DialectInfo dialectInfo = (DialectInfo) beanInfo;
                return dialectInfo.major.equals("") && dialectInfo.minor.equals("");
            }
        }).getBeanInfo();

        if (beanInfo == null) {
            throw new UnsupportedOperationException(ResourcesUtils.getMessage("database.standard.output.msg005", name));
        } else {
            return beanInfo.getType();
        }
    }

}
