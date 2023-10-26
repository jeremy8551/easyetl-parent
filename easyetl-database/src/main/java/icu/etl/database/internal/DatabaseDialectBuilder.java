package icu.etl.database.internal;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

import icu.etl.annotation.EasyBean;
import icu.etl.database.DatabaseDialect;
import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.BeanEvent;
import icu.etl.ioc.BeanEventListener;
import icu.etl.ioc.BeanInfo;
import icu.etl.ioc.EasyetlContext;
import icu.etl.log.STD;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * 数据库方言工厂
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-03-06
 */
@EasyBean
public class DatabaseDialectBuilder implements BeanBuilder<DatabaseDialect>, BeanEventListener {

    /** 数据库方言管理类 */
    private final DialectManager manager;

    /**
     * 初始化
     */
    public DatabaseDialectBuilder(EasyetlContext context) {
        List<BeanInfo> list = context.getBeanInfoList(DatabaseDialect.class);
        this.manager = new DialectManager(context, list);
    }

    public DatabaseDialect getBean(EasyetlContext context, Object... args) throws Exception {
        String[] parameters = this.getDatabaseInfo(args);
        String name = parameters[0];
        String major = parameters[1];
        String minor = parameters[2];
        return context.createBean(this.manager.getDialectClass(name, major, minor));
    }

    /**
     * 将参数转为数据库方言实现类注解参数 kind mode major minor 属性
     *
     * @param args 外部参数数组
     * @return 数据库信息数组，第一个元素是数据库简称，第二个元素是数据库大版本号，第三个元素是数据库小版本号
     */
    private String[] getDatabaseInfo(Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object obj = args[i];

            // 数据库连接
            if (obj instanceof Connection) {
                Connection conn = (Connection) obj;
                String[] array = this.parse(conn);
                if (array != null) {
                    return array;
                } else {
                    continue;
                }
            }

            // 数据库连接池
            if (obj instanceof DataSource) {
                Connection conn = ((DataSource) obj).getConnection();
                try {
                    String[] array = this.parse(conn);
                    if (array != null) {
                        return array;
                    } else {
                        continue;
                    }
                } finally {
                    IO.closeQuiet(conn);
                    IO.closeQuietly(conn);
                }
            }
        }

        String[] array = new String[3];
        array[0] = this.manager.parse(StringUtils.join(args, " "));
        array[1] = "";
        array[2] = "";
        return array;
    }

    /**
     * 从数据库连接中解析: 数据库简称，大版本号，小版本号
     *
     * @param conn 数据库连接
     * @return 数据库信息数组，第一个元素是数据库简称，第二个元素是数据库大版本号，第三个元素是数据库小版本号
     */
    private String[] parse(Connection conn) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            String[] array = new String[3];
            array[0] = manager.parse(metaData.getURL());
            array[1] = String.valueOf(metaData.getDatabaseMajorVersion());
            array[2] = String.valueOf(metaData.getDatabaseMinorVersion());
            return array;
        } catch (Throwable e) {
            STD.out.error("parse()", e);
            return null;
        }
    }

    public void addBean(BeanEvent event) {
        BeanInfo beanInfo = event.getBeanInfo();
        if (DatabaseDialect.class.isAssignableFrom(beanInfo.getType())) {
            this.manager.add(event.getContext(), beanInfo);
        }
    }

    public void removeBean(BeanEvent event) {
    }

}