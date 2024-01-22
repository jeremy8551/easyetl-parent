package icu.etl.database.db2;

public interface DB2Command {

    /**
     * 返回查询数据库 DDL 语句的命令
     *
     * @param databaseName 数据库名
     * @param schema       模式名
     * @param tableName    表名
     * @param username     用户名
     * @param password     密码
     * @return DDL语句
     */
    String getTableCommand(String databaseName, String schema, String tableName, String username, String password);

    /**
     * 返回查询数据库进程信息的命令
     *
     * @param applicationId 应用ID
     * @return 数据库进程信息
     */
    String getApplicationDetail(String applicationId);

}
