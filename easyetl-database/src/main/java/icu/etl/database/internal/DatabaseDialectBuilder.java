package icu.etl.database.internal;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Iterator;
import java.util.List;
import javax.sql.DataSource;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.collection.CaseSensitivMap;
import icu.etl.database.DatabaseDialect;
import icu.etl.database.Jdbc;
import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.BeanConfig;
import icu.etl.ioc.BeanContext;
import icu.etl.ioc.BeanEvent;
import icu.etl.ioc.BeanEventListener;
import icu.etl.log.STD;
import icu.etl.util.ClassUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * 数据库方言工厂
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-03-06
 */
public class DatabaseDialectBuilder implements BeanBuilder<DatabaseDialect>, BeanEventListener {

    /** true 表示不支持 */
    private volatile boolean notinit;

    /** 支持的所有数据库实现类注解 kind 与 mode 的映射关系 */
    private CaseSensitivMap<String> dialectMap;

    /**
     * 初始化
     */
    public DatabaseDialectBuilder() {
        this.dialectMap = new CaseSensitivMap<String>();
        this.notinit = true;
    }

    public DatabaseDialect build(BeanContext context, Object... array) throws Exception {
        this.init(context);
        String[] parameters = this.toParameters(array);
        Class<DatabaseDialect> cls = context.getImplement(DatabaseDialect.class, parameters[0], parameters[1], parameters[2], parameters[3]);
        return cls == null ? null : ClassUtils.newInstance(cls);
    }

    /**
     * 加载所有数据库方言实现类信息
     *
     * @param context
     */
    private void init(BeanContext context) {
        if (this.notinit) {
            List<BeanConfig> list = context.getImplements(DatabaseDialect.class);
            for (BeanConfig obj : list) {
                EasyBeanClass anno = obj.getAnnotationAsImplement();
                if (anno != null) {
                    String kind = StringUtils.trimBlank(anno.kind());
                    String mode = StringUtils.trimBlank(anno.mode());
                    this.dialectMap.put(kind, mode);
                }
            }
            this.notinit = false;
        }
    }

    /**
     * 将参数转为数据库方言实现类注解参数 kind mode major minor 属性
     *
     * @param array
     * @return
     */
    private String[] toParameters(Object... array) {
        // 返回数据库连接对应的实现类参数
        String[] parameters = null;
        for (int i = 0; i < array.length; i++) {
            Object obj = array[i];

            if (obj instanceof Connection) {
                Connection conn = (Connection) obj;
                parameters = this.toParameters(conn);
            } else if (obj instanceof DataSource) {
                Connection conn = Jdbc.getConnection((DataSource) obj);
                try {
                    parameters = this.toParameters(conn);
                } finally {
                    IO.closeQuiet(conn);
                    IO.closeQuietly(conn);
                }
            }
        }

        if (parameters == null) {
            String kind = this.toKind(StringUtils.join(array, " "));
            String mode = this.dialectMap.get(kind);

            parameters = new String[4];
            parameters[0] = kind;
            parameters[1] = mode;
            parameters[2] = "";
            parameters[3] = "";
        }
        return parameters;
    }

    /**
     * 从数据库连接中查询标识符，大版本号，小版本号
     *
     * @param conn 数据库连接
     * @return
     */
    private String[] toParameters(Connection conn) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            String kind = this.toKind(metaData.getURL());
            String mode = this.dialectMap.get(kind);

            String[] array = new String[4];
            array[0] = kind;
            array[1] = mode;
            array[2] = String.valueOf(metaData.getDatabaseMajorVersion());
            array[3] = String.valueOf(metaData.getDatabaseMinorVersion());
            return array;
        } catch (Throwable e) {
            STD.out.error("parse()", e);
            return null;
        }
    }

    /**
     * 从 JDBC URL 中提取数据库名
     *
     * @param url
     * @return
     */
    private String toKind(String url) {
        String str = url.toLowerCase();
        for (Iterator<String> it = this.dialectMap.keySet().iterator(); it.hasNext(); ) {
            String name = it.next();
            String key = name.toLowerCase();
            if (str.indexOf(key) != -1) {
                return name;
            }
        }
        throw new UnsupportedOperationException(url);
    }

    public void addImplement(BeanEvent event) {
        EasyBeanClass anno = (EasyBeanClass) event.getAnnotation();
        String kind = StringUtils.trimBlank(anno.kind());
        String mode = StringUtils.trimBlank(anno.mode());
        this.dialectMap.put(kind, mode);
    }

    public void removeImplement(BeanEvent event) {
    }

}