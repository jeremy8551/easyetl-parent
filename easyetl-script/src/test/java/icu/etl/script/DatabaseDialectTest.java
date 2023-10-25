package icu.etl.script;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import icu.etl.TestEnv;
import icu.etl.database.DatabaseDDL;
import icu.etl.database.DatabaseDialect;
import icu.etl.database.DatabaseProcedure;
import icu.etl.database.DatabaseProcedureParameter;
import icu.etl.database.Jdbc;
import icu.etl.database.JdbcDao;
import icu.etl.database.SQL;
import icu.etl.database.db2.DB2Dialect;
import icu.etl.database.internal.AbstractDialect;
import icu.etl.database.internal.StandardDatabaseDialect;
import icu.etl.database.internal.StandardDatabaseProcedure;
import icu.etl.database.mysql.MysqlDialect;
import icu.etl.database.oracle.OracleDialect;
import icu.etl.ioc.AnnotationEasyetlContext;
import icu.etl.util.ClassUtils;
import icu.etl.util.Dates;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

import static org.junit.Assert.assertTrue;

public class DatabaseDialectTest {

    public static void main7(String[] args) {
        System.setProperty("icu.etl.test.mode", "server54");
        Connection conn = TestEnv.getConnection();
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println(ClassUtils.toString(metaData, true, true, "get", "to"));
            System.out.println(StringUtils.toString(Jdbc.getSchemas(conn)));
            System.out.println();
            System.out.println();

            System.out.println("getCatalogs:");
            Jdbc.toString(conn.getMetaData().getCatalogs());
            System.out.println();
            System.out.println();

            System.out.println("getTableTypes:");
            Jdbc.toString(conn.getMetaData().getTableTypes());
        } catch (Exception e) {
            try {
                conn.rollback();
                assertTrue(false);
            } catch (Throwable e1) {
                assertTrue(true);
            }
        } finally {
            try {
                conn.close();
                assertTrue(false);
            } catch (Throwable e2) {
                assertTrue(true);
                System.out.println("数据库连接中断测试成功!");
            }
        }
    }

    public static void main6(String[] args) throws SQLException {
        AbstractDialect d = new StandardDatabaseDialect();
        Connection conn = TestEnv.getConnection();
        System.out.println(d.getSchema(conn));
    }

    public static void main5(String[] args) {
        System.out.println(SQL.isFieldName(null) == false);
        System.out.println(SQL.isFieldName("") == false);
        System.out.println(SQL.isFieldName("1") == false);
        System.out.println(SQL.isFieldName("a") == true);
        System.out.println(SQL.isFieldName("_") == false);
        System.out.println(SQL.isFieldName("_1") == true);
    }

    public static void main(String[] args) throws SQLException {
        AnnotationEasyetlContext context = new AnnotationEasyetlContext();
//		Connection conn = JT.getConnection("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@ //110.1.5.37:1521/dadb", "lhbb", "lhbb");
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            System.out.println(StringUtils.toString(Jdbc.getTypeInfo(dao.getConnection())));

            DatabaseProcedure proc = dao.getDialect().getProcedureForceOne(dao.getConnection(), null, null, "PROC_QYZX_SBC_BAOHANS");
            DatabaseDDL ddl = dao.getDialect().toDDL(dao.getConnection(), proc);
            DatabaseProcedure bj = StandardDatabaseProcedure.toProcedure(dao, ddl.get(0));
            System.out.println(bj.toCallProcedureString());
        } finally {
            dao.close();
        }
    }

    public static void main2(String[] args) throws SQLException {
        AnnotationEasyetlContext context = new AnnotationEasyetlContext();
        ClassUtils.loadClass("oracle.jdbc.driver.OracleDriver");
        Connection conn = Jdbc.getConnection("jdbc:oracle:thin:@ //110.1.5.37:1521/dadb", "lhbb", "lhbb");
        // Connection conn = TESTDB.getConnection();
        try {
            DatabaseDialect dialect = context.get(DatabaseDialect.class, conn);
            // System.out.println(JT.getDatabaseTypeInfo(conn));

            DatabaseProcedure p = dialect.getProcedureForceOne(conn, null, "LHBB", "CUSTAUM_APPEND");
            // DatabaseProcedure p = dialect.getDatabaseProcedureForceOne(conn, null, "TESTADM", "PROC_QYZX_SBC_LOAN");
            System.out.println(Dates.format21(p.getCreateTime()));
            System.out.println(StringUtils.toString(p));

            for (DatabaseProcedureParameter pm : p.getParameters()) {
                System.out.println(StringUtils.toString(pm));
                System.out.println();
                System.out.println();
                System.out.println();
            }
        } finally {
            IO.closeQuietly(conn);
        }
    }

    public static void main1(String[] args) {
        DatabaseDialect d = new DB2Dialect();
        System.out.println(StringUtils.toString(d.parseJdbcUrl("jdbc:db2://130.1.10.103:50001/TESTDB:currentSchema=HYCS;")));
        System.out.println(StringUtils.toString(d.parseJdbcUrl("jdbc:db2://130.1.10.103/TESTDB:currentSchema=HYCS;")));
        System.out.println(StringUtils.toString(d.parseJdbcUrl("jdbc:db2:TESTDB")));

        d = new MysqlDialect();
        System.out.println(StringUtils.toString(d.parseJdbcUrl("jdbc:mysql://localhost:3306/test?user=root&password=&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false")));
        System.out.println(StringUtils.toString(d.parseJdbcUrl("jdbc:mysql://localhost/test?user=root&password=&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false")));
        System.out.println(StringUtils.toString(d.parseJdbcUrl("jdbc:mysql://localhost,127.0.0.1/test?user=root&password=&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false")));

        d = new OracleDialect();
        System.out.println(StringUtils.toString(d.parseJdbcUrl("jdbc:oracle:thin:@130.1.10.104:1521:sid")));
        System.out.println(StringUtils.toString(d.parseJdbcUrl("jdbc:oracle:thin:user/pass@130.1.10.104:1521:sid")));
        System.out.println(StringUtils.toString(d.parseJdbcUrl("jdbc:oracle:thin:@130.1.10.104:1521/sid")));
        System.out.println(StringUtils.toString(d.parseJdbcUrl("jdbc:oracle:thin:user/pass@130.1.10.104:1521/sid")));
        System.out.println(StringUtils.toString(d.parseJdbcUrl("jdbc:oracle:thin:@(description=(address_list= (address=(host=rac1) (protocol=tcp1)(port=1521))(address=(host=rac2)(protocol=tcp2) (port=1522)) (load_balance=yes)(failover=yes))(connect_data=(SERVER=DEDICATED)(service_name= oratest)))")));
    }
}
