package icu.etl.database.internal;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import icu.etl.collection.CaseSensitivSet;
import icu.etl.database.DatabaseDDL;
import icu.etl.database.DatabaseDialect;
import icu.etl.database.DatabaseException;
import icu.etl.database.DatabaseIndex;
import icu.etl.database.DatabaseIndexList;
import icu.etl.database.DatabaseProcedure;
import icu.etl.database.DatabaseTable;
import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.DatabaseTableDDL;
import icu.etl.database.DatabaseTypeSet;
import icu.etl.database.Jdbc;
import icu.etl.database.JdbcDao;
import icu.etl.database.SQL;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ClassUtils;
import icu.etl.util.CollectionUtils;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.Property;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public abstract class AbstractDialect implements DatabaseDialect {
    private final static Log log = LogFactory.getLog(AbstractDialect.class);

    /** 关键字字符串集合 */
    protected Set<String> keyword;

    /**
     * 初始化
     */
    public AbstractDialect() {
        this.keyword = new CaseSensitivSet();
    }

    public String getDatabaseMajorVersion() {
        return "";
    }

    public String getDatabaseMinorVersion() {
        return "";
    }

    public String dropPrimaryKey(Connection connection, DatabaseIndex index) throws SQLException {
        if (index == null) {
            throw new NullPointerException();
        }

        String sql = "alter table " + index.getTableFullName() + " drop primary key ";
        int count = 0;
        while (true) {
            if (++count > 10) {
                break;
            }

            try {
                JdbcDao.execute(connection, sql);
                break;
            } catch (Throwable e) {
                Dates.sleep(2000);
                continue;
            }
        }
        return sql;
    }

    public DatabaseTableDDL toDDL(Connection connection, DatabaseTable table) throws SQLException {
        StringBuilder str = this.toDDL(table);

        // 建表语句
        StandardDatabaseTableDDL ddl = new StandardDatabaseTableDDL();
        ddl.setTable(str.toString());

        // 索引
        DatabaseIndexList indexs = table.getIndexs();
        for (DatabaseIndex index : indexs) {
            ddl.getIndex().addAll(this.toDDL(connection, index, false));
        }

        // 主键
        DatabaseIndexList pks = table.getPrimaryIndexs();
        for (DatabaseIndex index : pks) {
            ddl.getPrimaryKey().addAll(this.toDDL(connection, index, true));
        }
        return ddl;
    }

    /**
     * 生成数据库表的 DDL 语句
     *
     * @param table 数据库表信息
     * @return DDL 语句
     */
    protected StringBuilder toDDL(DatabaseTable table) {
        String schema = table.getSchema();
        String name = table.getName();
        List<DatabaseTableColumn> columns = table.getColumns();

        StringBuilder buf = new StringBuilder();
        buf.append("create table ");
        if (StringUtils.isNotBlank(schema)) {
            buf.append(schema).append(".");
        }
        buf.append(name).append(" (").append(FileUtils.lineSeparator);
        for (Iterator<DatabaseTableColumn> it = columns.iterator(); it.hasNext(); ) {
            DatabaseTableColumn col = it.next();
            buf.append("    ");
            buf.append(col.getName()).append("  ");
            buf.append(col.getFieldType());

            // 带一个长度范围的字段类型
            if (col.getSqlType() == Types.CHAR //
                    || col.getSqlType() == Types.VARCHAR //
                    || col.getSqlType() == Types.LONGVARCHAR //
                    || col.getSqlType() == Types.ARRAY //
                    || col.getSqlType() == Types.BINARY //
                    || col.getSqlType() == Types.LONGVARBINARY //
                    || col.getSqlType() == Types.VARBINARY //
            ) {
                buf.append("(");
                buf.append(col.length());
                buf.append(")");
            }

            // 不带长度范围的字段类型
            else if (col.getSqlType() == Types.INTEGER //
                    || col.getSqlType() == Types.BIGINT //
                    || col.getSqlType() == Types.BLOB //
                    || col.getSqlType() == Types.BOOLEAN //
                    || col.getSqlType() == Types.CLOB //
                    || col.getSqlType() == Types.DATE //
                    || col.getSqlType() == Types.TIME //
                    || col.getSqlType() == Types.TIMESTAMP //
                    || col.getSqlType() == Types.DOUBLE //
                    || col.getSqlType() == Types.FLOAT //
                    || col.getSqlType() == Types.JAVA_OBJECT //
                    || col.getSqlType() == Types.REAL //
                    || col.getSqlType() == Types.SMALLINT //
                    || col.getSqlType() == Types.TINYINT //
            ) {
            }

            // 带二个长度范围的字段类型
            else if (col.getSqlType() == Types.DECIMAL //
                    || col.getSqlType() == Types.NUMERIC //
            ) {
                buf.append("(");
                buf.append(col.length());
                buf.append(", ");
                buf.append(col.getDigit());
                buf.append(")");
            } else {
                throw new UnsupportedOperationException(String.valueOf(col.getSqlType()));
            }

            if ("NO".equals(col.getNullAble())) {
                buf.append(" not null");
            }

            if (it.hasNext()) {
                buf.append(",");
            }

            String remarks = col.getRemark();
            if (StringUtils.isNotBlank(remarks)) {
                buf.append(" -- ").append(remarks);
            }
            buf.append(FileUtils.lineSeparator);
        }
        buf.append(")");
        return buf;
    }

    public DatabaseDDL toDDL(Connection connection, DatabaseIndex index, boolean primary) throws SQLException {
        StandardDatabaseDDL ddl = new StandardDatabaseDDL();
        if (primary) {
            StringBuilder buf = new StringBuilder();
            buf.append("ALTER TABLE ");
            buf.append(index.getTableFullName());
            buf.append(" PRIMARY KEY(");
            List<String> indexColumnName = index.getColumnNames();
            for (int i = 0; i < indexColumnName.size(); i++) {
                String name = indexColumnName.get(i);
                buf.append(name);
                if (i != indexColumnName.size() - 1) {
                    buf.append(", ");
                }
            }
            buf.append(")");
            ddl.add(buf.toString());
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append("CREATE ");
            if (index.isUnique()) {
                buf.append("UNIQUE ");
            }
            buf.append("index ").append(index.getFullName()).append(" on ");
            buf.append(index.getTableFullName());
            buf.append("(");
            List<String> indexColumnName = index.getColumnNames();
            List<Integer> indexColumnSort = index.getDirections();
            for (int i = 0; i < indexColumnName.size(); i++) {
                String name = indexColumnName.get(i);
                Integer sort = indexColumnSort.get(i);
                buf.append(name);
                if (sort != null) {
                    if (DatabaseIndex.INDEX_ASC == sort) {
                        buf.append(" asc");
                    }
                    if (DatabaseIndex.INDEX_DESC == sort) {
                        buf.append(" desc");
                    }
                }
                if (i != indexColumnName.size() - 1) {
                    buf.append(", ");
                }
            }
            buf.append(")");
            ddl.add(buf.toString());
        }
        return ddl;
    }

    public String getSchema(Connection conn) throws SQLException {
        try {
            return (String) ClassUtils.executeMethod(conn, "getSchema", new Object[0]);
//			return conn.getSchema();
        } catch (Exception e) {
            Property p = CollectionUtils.firstElement(Jdbc.getSchemas(conn));
            return p == null ? null : p.getKey();
        }
    }

    public Set<String> getKeyword(Connection conn) throws SQLException {
        Set<String> keys = (conn == null) ? new CaseSensitivSet() : Jdbc.getSQLKeywords(conn);
        keys.addAll(this.keyword);
        return keys;
    }

    /**
     * 查询数据库表的索引或主键信息
     *
     * @param connection 数据库连接
     * @param catalog    类别名称，因为存储在此数据库中，所以它必须匹配类别名称。该参数为 "" 则检索没有类别的描述，为 null 则表示该类别名称不应用于缩小搜索范围
     * @param schema     模式名称，因为存储在此数据库中，所以它必须匹配模式名称。该参数为 "" 则检索那些没有模式的描述，为 null 则表示该模式名称不应用于缩小搜索范围
     * @param tableName  表名（大小写敏感）
     * @param name       索引名
     * @return 索引集合
     * @throws SQLException 数据库错误
     */
    public List<DatabaseIndex> getIndexs(Connection connection, String catalog, String schema, String tableName, String name) throws SQLException {
        Map<String, StandardDatabaseIndex> map = new HashMap<String, StandardDatabaseIndex>();
        ResultSet resultSet = null;
        try {
            List<DatabaseIndex> list = new ArrayList<DatabaseIndex>();
            resultSet = connection.getMetaData().getIndexInfo(catalog, schema, tableName, false, false);
            while (resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                short order = resultSet.getShort("ORDINAL_POSITION");
                String asc = resultSet.getString("ASC_OR_DESC");

                // 设置排序规则
                int sort = DatabaseIndex.INDEX_UNKNOWN;
                if ("A".equalsIgnoreCase(asc)) {
                    sort = DatabaseIndex.INDEX_ASC;
                }
                if ("D".equalsIgnoreCase(asc)) {
                    sort = DatabaseIndex.INDEX_DESC;
                }

                if (StringUtils.isBlank(indexName)) {
                    continue;
                }

                if (!StringUtils.isBlank(name) && !indexName.equalsIgnoreCase(name)) {
                    continue;
                }

                StandardDatabaseIndex index = map.get(indexName);
                if (index == null) {
                    index = new StandardDatabaseIndex();
                    index.setName(indexName);
                    index.setTableCatalog(StringUtils.trimBlank(resultSet.getString("TABLE_CAT")));
                    index.setTableName(StringUtils.trim(resultSet.getString("TABLE_NAME")));
                    index.setTableSchema(StringUtils.trim(resultSet.getString("TABLE_SCHEM")));
                    index.setTableFullName(this.toTableName(index.getTableCatalog(), index.getTableSchema(), index.getTableName()));
                    index.setSchema(index.getTableSchema()); // 默认使用表模式作为索引模式，可在数据库方言类中修改这个值
                    index.setFullName(index.getName()); // 默认使用索引名所在全名，可在数据库方言累中修改这个值
                    index.setUnique(!resultSet.getBoolean("NON_UNIQUE"));
                    map.put(indexName, index);
                    list.add(index);
                }
                index.getPositions().add(Integer.valueOf((int) order)); // 设置字段在索引中的位置信息，从1开始
                index.getColumnNames().add(columnName); // 设置索引中的字段名集合
                index.getDirections().add(Integer.valueOf(sort)); // 设置索引字段的排序方式
            }

            return list;
        } catch (SQLException e) {
            throw new DatabaseException(this.toTableName(catalog, schema, tableName), e);
        } finally {
            IO.closeQuietly(resultSet);
            map.clear();
        }
    }

    public List<DatabaseIndex> getIndexs(Connection connection, String catalog, String schema, String tableName) throws SQLException {
        Map<String, StandardDatabaseIndex> map = new HashMap<String, StandardDatabaseIndex>();
        ResultSet resultSet = null;
        try {
            DatabaseMetaData metaData = connection.getMetaData();

            /** ---- 查询主键索引名称 ---- */
            HashSet<String> pksNames = new HashSet<String>(1);
            ResultSet pksRes = metaData.getPrimaryKeys(catalog, schema, tableName);
            try {
                while (pksRes.next()) {
                    String idxname = pksRes.getString("PK_NAME");
                    pksNames.add(idxname);
                }
            } finally {
                IO.closeQuietly(pksRes);
            }
            /** ---- 查询主键索引名称 ---- */

            List<DatabaseIndex> list = new ArrayList<DatabaseIndex>();
            resultSet = metaData.getIndexInfo(catalog, schema, tableName, false, false);
            while (resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                short order = resultSet.getShort("ORDINAL_POSITION");
                String asc = resultSet.getString("ASC_OR_DESC");

                int sort = DatabaseIndex.INDEX_UNKNOWN;
                if ("A".equalsIgnoreCase(asc)) {
                    sort = DatabaseIndex.INDEX_ASC;
                }
                if ("D".equalsIgnoreCase(asc)) {
                    sort = DatabaseIndex.INDEX_DESC;
                }

                if (StringUtils.isBlank(indexName)) {
                    continue;
                }

                if (pksNames.contains(indexName)) {// 不能为主键
                    continue;
                }

                StandardDatabaseIndex index = map.get(indexName);
                if (index == null) {
                    index = new StandardDatabaseIndex();
                    index.setName(indexName);
                    index.setTableCatalog(StringUtils.trimBlank(resultSet.getString("TABLE_CAT")));
                    index.setTableName(StringUtils.trimBlank(resultSet.getString("TABLE_NAME")));
                    index.setTableSchema(StringUtils.trimBlank(resultSet.getString("TABLE_SCHEM")));
                    index.setTableFullName(this.toTableName(index.getTableCatalog(), index.getTableSchema(), index.getTableName()));
                    index.setSchema(index.getTableSchema()); // 默认使用表模式作为索引模式，可在数据库方言类中修改这个值
                    index.setFullName(index.getName()); // 默认使用索引名所在全名，可在数据库方言累中修改这个值
                    index.setUnique(!resultSet.getBoolean("NON_UNIQUE"));
                    map.put(indexName, index);
                    list.add(index);
                }
                index.getPositions().add(Integer.valueOf((int) order));
                index.getColumnNames().add(columnName);
                index.getDirections().add(Integer.valueOf(sort));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException(this.toIndexName(catalog, schema, tableName), e);
        } finally {
            IO.closeQuietly(resultSet);
            map.clear();
        }
    }

    /**
     * 查询表的主键信息
     *
     * @param connection 数据库连接
     * @param catalog    类别名称，因为存储在此数据库中，所以它必须匹配类别名称。该参数为 "" 则检索没有类别的描述，为 null 则表示该类别名称不应用于缩小搜索范围
     * @param schema     模式名称，因为存储在此数据库中，所以它必须匹配模式名称。该参数为 "" 则检索那些没有模式的描述，为 null 则表示该模式名称不应用于缩小搜索范围
     * @param tableName  表名（大小写敏感）
     * @return 索引集合
     */
    public List<DatabaseIndex> getPrimaryIndex(Connection connection, String catalog, String schema, String tableName) {
        Map<String, StandardDatabaseIndex> map = new HashMap<String, StandardDatabaseIndex>();
        ResultSet resultSet = null;
        try {
            List<DatabaseIndex> list = new ArrayList<DatabaseIndex>();
            DatabaseMetaData metaData = connection.getMetaData();
            resultSet = metaData.getPrimaryKeys(catalog, schema, tableName);
            while (resultSet.next()) {
                String indexName = resultSet.getString("PK_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                short order = resultSet.getShort("KEY_SEQ");

                if (StringUtils.isBlank(indexName)) {
                    continue;
                }

                StandardDatabaseIndex index = map.get(indexName);
                if (index == null) {
                    index = new StandardDatabaseIndex();
                    index.setName(indexName);
                    index.setTableCatalog(StringUtils.trim(resultSet.getString("TABLE_CAT")));
                    index.setTableSchema(StringUtils.trim(resultSet.getString("TABLE_SCHEM")));
                    index.setTableName(StringUtils.trim(resultSet.getString("TABLE_NAME")));
                    index.setTableFullName(this.toTableName(index.getTableCatalog(), index.getTableSchema(), index.getTableName()));
                    index.setSchema(index.getTableSchema()); // 默认使用表模式作为索引模式，可在数据库方言类中修改这个值
                    index.setFullName(index.getName()); // 默认使用索引名所在全名，可在数据库方言累中修改这个值
                    index.setUnique(true);
                    map.put(indexName, index);
                    list.add(index);
                }
                index.getPositions().add(Integer.valueOf((int) order));
                index.getColumnNames().add(columnName);
                index.getDirections().add(Integer.valueOf(DatabaseIndex.INDEX_UNKNOWN));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException(this.toTableName(catalog, schema, tableName), e);
        } finally {
            IO.closeQuietly(resultSet);
            map.clear();
        }
    }

    public List<DatabaseTable> getTable(Connection connection, String catalog, String schema, String tableName) throws SQLException {
        DatabaseTypeSet types = Jdbc.getTypeInfo(connection);
        ResultSet resultSet = null;
        try {
            List<DatabaseTable> list = new ArrayList<DatabaseTable>();
            resultSet = connection.getMetaData().getTables(catalog, schema, tableName, null);
            while (resultSet.next()) {
                StandardDatabaseTable table = new StandardDatabaseTable();
                table.setName(StringUtils.trim(resultSet.getString("TABLE_NAME")));
                table.setSchema(StringUtils.trim(resultSet.getString("TABLE_SCHEM")));
                table.setCatalog(StringUtils.trim(resultSet.getString("TABLE_CAT")));
                table.setType(StringUtils.trim(resultSet.getString("TABLE_TYPE")));
                table.setRemark(StringUtils.trim(resultSet.getString("REMARKS")));
                table.setFullName(this.toTableName(table.getCatalog(), table.getSchema(), table.getName()));

                // 索引信息
                List<DatabaseIndex> indexs = this.getIndexs(connection, catalog, table.getSchema(), table.getName());

                // 主键信息
                List<DatabaseIndex> pks = this.getPrimaryIndex(connection, catalog, table.getSchema(), table.getName());

                Jdbc.removePrimaryKey(indexs, pks);
                table.setIndexs(indexs);
                table.setPrimaryIndexs(pks);

                // 列信息
                List<DatabaseTableColumn> columns = this.getTableColumns(connection, types, catalog, table.getSchema(), table.getName());
                table.setColumns(columns);
                list.add(table);
            }

            return list;
        } catch (SQLException e) {
            throw new DatabaseException(this.toTableName(catalog, schema, tableName), e);
        } finally {
            IO.closeQuietly(resultSet);
        }
    }

    protected List<DatabaseTableColumn> getTableColumns(Connection connection, DatabaseTypeSet types, String catalog, String schema, String tableName) {
        ResultSet resultSet = null;
        try {
            List<DatabaseTableColumn> list = new ArrayList<DatabaseTableColumn>();
            resultSet = connection.getMetaData().getColumns(catalog, schema, tableName, null);

            while (resultSet.next()) {
                String COLUMN_NAME = resultSet.getString("COLUMN_NAME");
                int DATA_TYPE = resultSet.getInt("DATA_TYPE");
                String TYPE_NAME = resultSet.getString("TYPE_NAME");
                int COLUMN_SIZE = resultSet.getInt("COLUMN_SIZE");
                // int BUFFER_LENGTH = res.getInt("BUFFER_LENGTH");
                int DECIMAL_DIGITS = resultSet.getInt("DECIMAL_DIGITS");
                int NUM_PREC_RADIX = resultSet.getInt("NUM_PREC_RADIX");
//				int NULLABLE = resultSet.getInt("NULLABLE");
                String REMARKS = resultSet.getString("REMARKS");
                String COLUMN_DEF = resultSet.getString("COLUMN_DEF");
                // String SQL_DATA_TYPE = res.getString("SQL_DATA_TYPE");
                // String SQL_DATETIME_SUB = res.getString("SQL_DATETIME_SUB");
                int CHAR_OCTET_LENGTH = resultSet.getInt("CHAR_OCTET_LENGTH");
                int ORDINAL_POSITION = resultSet.getInt("ORDINAL_POSITION");
                String IS_NULLABLE = resultSet.getString("IS_NULLABLE");
                String IS_AUTOINCREMENT = resultSet.getString("IS_AUTOINCREMENT");
                // String SOURCE_DATA_TYPE = res.getString("SOURCE_DATA_TYPE");

                StandardDatabaseTableColumn column = new StandardDatabaseTableColumn();
                column.setName(COLUMN_NAME);
                column.setSqlType(DATA_TYPE);
                column.setFieldType(TYPE_NAME);
                column.setLength(COLUMN_SIZE);
                column.setRemark(REMARKS);
                column.setDefault(COLUMN_DEF);
                column.setMaxLength(CHAR_OCTET_LENGTH);
                column.setPosition(ORDINAL_POSITION);
                column.setNullAble(IS_NULLABLE);
                column.setDigit(DECIMAL_DIGITS);
                column.setRadix(NUM_PREC_RADIX);
                column.setTableCatalog(StringUtils.trimBlank(resultSet.getString("TABLE_CAT")));
                column.setTableSchema(StringUtils.trimBlank(resultSet.getString("TABLE_SCHEM")));
                column.setTableName(StringUtils.trimBlank(resultSet.getString("TABLE_NAME")));
                column.setTableFullName(this.toTableName(column.getTableCatalog(), column.getTableSchema(), column.getTableName()));
                // col.setScopeCatlog(ST.trimBlank(res.getString("SCOPE_CATLOG")));
                // col.setScopeSchema(ST.trimBlank(res.getString("SCOPE_SCHEMA")));
                // col.setScopeTable(ST.trimBlank(res.getString("SCOPE_TABLE")));
                column.setIncrement(IS_AUTOINCREMENT);
                column.setType(types.get(TYPE_NAME));
                list.add(column);
            }

            return list;
        } catch (SQLException e) {
            throw new DatabaseException(this.toTableName(catalog, schema, tableName), e);
        } finally {
            IO.closeQuietly(resultSet);
        }
    }

    public String toDeleteQuicklySQL(Connection connection, String catalog, String schema, String tableName) {
        return "delete from " + this.toTableName(catalog, schema, tableName);
    }

    public String toTableName(String catalog, String schema, String tableName) {
        return SQL.toTableName(schema, tableName);
    }

    public String toIndexName(String catalog, String schema, String tableName) {
        return SQL.toTableName(schema, tableName);
    }

    public boolean containsTable(Connection connection, String catalog, String schema, String tableName) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = connection.getMetaData().getTables(catalog, schema, tableName, null);
            return resultSet.next() && StringUtils.isNotBlank(resultSet.getString("TABLE_NAME"));
        } catch (SQLException e) {
            throw new DatabaseException(this.toTableName(catalog, schema, tableName), e);
        } finally {
            IO.closeQuietly(resultSet);
        }
    }

    public DatabaseProcedure getProcedureForceOne(Connection connection, String catalog, String schema, String procedureName) throws SQLException {
        List<DatabaseProcedure> list = this.getProcedure(connection, catalog, schema, procedureName);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() != 1) {
            throw new DatabaseException(ResourcesUtils.getMessage("database.standard.output.msg033", catalog, schema, procedureName, StringUtils.join(list, ", ")));
        } else {
            return list.get(0);
        }
    }

    public boolean terminate(Connection conn, Properties p) throws SQLException {
        try {
            if (conn != null) {
                conn.close();
            }
            return true;
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(StringUtils.toString(p), e);
            }
            return false;
        }
    }

    public Properties getAttributes(Connection conn) {
        return new Properties();
    }

    public List<String> alterTableColumn(Connection connection, DatabaseTableColumn oc, DatabaseTableColumn nc) throws SQLException {
        throw new UnsupportedOperationException();
    }

}