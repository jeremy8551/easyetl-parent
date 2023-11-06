package icu.etl.database.db2;

import icu.etl.annotation.EasyBean;

@EasyBean(name = "db2", description = "")
public class DB2LinuxCommand implements DB2Command {

    public String getTableCommand(String databaseName, String schema, String tableName, String username, String password) {
        return "db2look -e -d " + databaseName + " -i " + username + " -w " + password + " -z " + schema + " -tw " + tableName;
    }

    public String getApplicationDetail(String applicationId) {
        return "db2 list applications show detail | grep \"" + applicationId + "\"";
    }

}
