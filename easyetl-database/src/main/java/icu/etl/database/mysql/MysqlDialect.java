package icu.etl.database.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import icu.etl.annotation.EasyBean;
import icu.etl.collection.CaseSensitivSet;
import icu.etl.database.DatabaseDDL;
import icu.etl.database.DatabaseIndex;
import icu.etl.database.DatabaseProcedure;
import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.DatabaseURL;
import icu.etl.database.JdbcConverterMapper;
import icu.etl.database.JdbcDao;
import icu.etl.database.internal.AbstractDialect;
import icu.etl.database.internal.StandardDatabaseURL;
import icu.etl.util.StringUtils;

@EasyBean(name = "mysql")
public class MysqlDialect extends AbstractDialect {

    /**
     * 初始化
     */
    public MysqlDialect() {
        super();
    }

    public String toDeleteQuicklySQL(Connection connection, String catalog, String schema, String tableName) {
        if (StringUtils.isBlank(tableName)) {
            throw new IllegalArgumentException(tableName);
        } else {
            return "truncate table " + this.toTableName(catalog, schema, tableName);
        }
    }

    public int getRowNumberStarter() {
        return 0;
    }

    public void setSchema(Connection conn, String schema) {
        throw new UnsupportedOperationException(schema);
    }

    public boolean supportSchema() {
        return false;
    }

    public List<DatabaseURL> parseJdbcUrl(String url) {
        if (url == null) {
            throw new NullPointerException();
        }

        List<DatabaseURL> list = new ArrayList<DatabaseURL>(1);
        String[] part = StringUtils.split(url, "/");
        if (part.length == 4) {
            String databaseType = "mysql";

            /**
             * 解析第一部分
             */
            String[] part1 = StringUtils.split(part[0], ":");
            if (part1.length == 3) {
                databaseType = part1[1];
            } else {
                throw new IllegalArgumentException(url + " error!");
            }

            /**
             * 第二部分
             */
            if (!part[1].equals("")) {
                throw new IllegalArgumentException(url + " error!");
            }

            /**
             * 解析第三部分
             */
            String[] part3 = StringUtils.removeBlank(StringUtils.split(part[2], ","));
            if (part3.length == 0) {
                throw new IllegalArgumentException(url + " error!");
            }

            for (String str : part3) {
                String[] array = StringUtils.split(str, ":");

                StandardDatabaseURL obj = new StandardDatabaseURL(url);
                list.add(obj);

                obj.setDatabaseType(databaseType);
                if (array.length == 1) {
                    obj.setHostname(array[0]);
                    obj.setPort("3306");
                } else if (array.length == 2) {
                    obj.setHostname(array[0]);
                    obj.setPort(array[1]);
                } else {
                    throw new IllegalArgumentException(url);
                }
            }

            /**
             * 解析第四部分
             */
            int p4 = part[3].indexOf('?');
            if (p4 == -1) {
                for (DatabaseURL cfg : list) {
                    ((StandardDatabaseURL) cfg).setDatabaseName(part[3]);
                }
            } else {
                for (DatabaseURL cfg : list) {
                    String dbname = part[3].substring(0, p4);
                    ((StandardDatabaseURL) cfg).setDatabaseName(dbname);

                    String params = part[3].substring(p4 + 1);
                    String[] ps = StringUtils.split(params, '&');
                    for (String str : ps) {
                        String[] array = StringUtils.splitProperty(str);
                        if (array != null) {
                            ((StandardDatabaseURL) cfg).setAttribute(array[0], array[1]);
                        }
                    }
                }
            }

            return list;
        } else {
            throw new IllegalArgumentException(url + " error!");
        }
    }

    public List<DatabaseProcedure> getProcedure(Connection connection, String catalog, String schema, String procedureName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public JdbcConverterMapper getObjectConverters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public boolean isOverLengthException(Throwable e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public boolean isRebuildTableException(Throwable e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public boolean isPrimaryRepeatException(Throwable e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public boolean isIndexExistsException(Throwable e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void reorgRunstatsIndexs(Connection conn, List<DatabaseIndex> indexs) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void openLoadMode(JdbcDao conn, String fullname) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void closeLoadMode(JdbcDao conn, String fullname) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void commitLoadData(JdbcDao conn, String fullname) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public JdbcConverterMapper getStringConverters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public String getCatalog(Connection connection) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public DatabaseDDL toDDL(Connection connection, DatabaseProcedure procedure) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public String getKeepAliveSQL() {
        return "select 1 from dual";
    }

    public boolean supportedMergeStatement() {
        return true;
    }

    public String toMergeStatement(String tableName, List<DatabaseTableColumn> columns, List<String> mergeColumn) {
        CaseSensitivSet set = new CaseSensitivSet(mergeColumn);
        String sql = "INSERT INTO " + tableName + " (";
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            sql += it.next().getName();
            if (it.hasNext()) {
                sql += ", ";
            }
        }
        sql += ") VALUES (";
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            it.next();
            sql += "?";
            if (it.hasNext()) {
                sql += ", ";
            }
        }
        sql += ")";
        sql += " ON DUPLICATE KEY UPDATE ";

        boolean comma = false; // true表示添加逗号
        // 拼写 字段=值 表达式
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            DatabaseTableColumn col = it.next();
            String name = col.getName();
            if (set.contains(name)) {
                continue;
            }

            if (comma) {
                sql += ", ";
            }
            sql += name + "=VALUES(" + name + ")";
            comma = true;
        }
        return sql;
    }

}