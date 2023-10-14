package icu.etl.database.db2;

public interface DB2Command {

    /**
     * 返回查询数据库 DDL 语句的命令
     *
     * @param databaseName
     * @param schema
     * @param tableName
     * @param username
     * @param password
     * @return
     */
    String getTableCommand(String databaseName, String schema, String tableName, String username, String password);

    /**
     * 返回查询数据库进程信息的命令
     *
     * @param applicationId
     * @return
     */
    String getApplicationDetail(String applicationId);

}
