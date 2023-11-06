package icu.etl.database.internal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.RowSetInternal;
import javax.sql.RowSetMetaData;

public class StandardRowSetInternal implements RowSetInternal {

    private Statement statement;
    private ResultSet resultSet;

    public StandardRowSetInternal(Statement statement, ResultSet resultSet) {
        this.statement = statement;
        this.resultSet = resultSet;
    }

    public void setMetaData(RowSetMetaData metaData) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Object[] getParams() throws SQLException {
        return new Object[0];
    }

    public ResultSet getOriginalRow() throws SQLException {
        return resultSet;
    }

    public ResultSet getOriginal() throws SQLException {
        if (resultSet.isBeforeFirst()) {
            return resultSet;
        }

        try {
            resultSet.beforeFirst();
            return resultSet;
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public Connection getConnection() throws SQLException {
        return statement.getConnection();
    }

}
