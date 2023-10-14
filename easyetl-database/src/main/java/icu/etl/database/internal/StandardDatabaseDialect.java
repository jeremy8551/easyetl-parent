package icu.etl.database.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import icu.etl.database.DatabaseDDL;
import icu.etl.database.DatabaseIndex;
import icu.etl.database.DatabaseProcedure;
import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.DatabaseURL;
import icu.etl.database.JdbcConverterMapper;
import icu.etl.database.JdbcDao;

public class StandardDatabaseDialect extends AbstractDialect {

    public StandardDatabaseDialect() {
        super();
    }

    public boolean supportSchema() {
        throw new UnsupportedOperationException();
    }

    public void setSchema(Connection connection, String schema) {
        throw new UnsupportedOperationException();
    }

    public List<DatabaseURL> parseJdbcUrl(String url) {
        throw new UnsupportedOperationException();
    }

    public int getRowNumberStarter() {
        throw new UnsupportedOperationException();
    }

    public List<DatabaseProcedure> getProcedure(Connection connection, String catalog, String schema, String procedureName) {
        throw new UnsupportedOperationException();
    }

    public JdbcConverterMapper getObjectConverters() {
        throw new UnsupportedOperationException();
    }

    public boolean isOverLengthException(Throwable e) {
        throw new UnsupportedOperationException();
    }

    public boolean isRebuildTableException(Throwable e) {
        throw new UnsupportedOperationException();
    }

    public boolean isPrimaryRepeatException(Throwable e) {
        throw new UnsupportedOperationException();
    }

    public boolean isIndexExistsException(Throwable e) {
        throw new UnsupportedOperationException();
    }

    public void reorgRunstatsIndexs(Connection conn, List<DatabaseIndex> indexs) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void openLoadMode(JdbcDao conn, String fullname) throws SQLException {
    }

    public void closeLoadMode(JdbcDao conn, String fullname) throws SQLException {
    }

    public void commitLoadData(JdbcDao conn, String fullname) throws SQLException {
    }

    public JdbcConverterMapper getStringConverters() {
        throw new UnsupportedOperationException();
    }

    public String getCatalog(Connection connection) throws SQLException {
        return null;
    }

    public DatabaseDDL toDDL(Connection connection, DatabaseProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    public String getKeepAliveSQL() {
        throw new UnsupportedOperationException();
    }

    public boolean supportedMergeStatement() {
        return false;
    }

    public String toMergeStatement(String tableName, List<DatabaseTableColumn> columns, List<String> mergeColumn) {
        throw new UnsupportedOperationException();
    }

}
