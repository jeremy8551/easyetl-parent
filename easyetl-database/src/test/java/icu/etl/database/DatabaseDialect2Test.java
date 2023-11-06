package icu.etl.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import icu.etl.database.db2.DB2Dialect;
import icu.etl.database.internal.AbstractDialect;
import icu.etl.database.internal.StandardDatabaseDialect;
import icu.etl.database.internal.StandardDatabaseProcedure;
import icu.etl.database.mysql.MysqlDialect;
import icu.etl.database.oracle.OracleDialect;
import icu.etl.util.ClassUtils;
import icu.etl.util.Dates;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore
public class DatabaseDialect2Test {

    @Rule
    public WithDBRule rule = new WithDBRule();

    /** 数据库连接 */
    private Connection connection;

    @Before
    public void setUp() {
        this.connection = rule.getConnection();
    }

    @After
    public void setDown() {
        IO.closeQuiet(this.connection);
    }

    @Test
    public void test1() throws SQLException {
        Connection conn = this.connection;
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
            conn.rollback();
            Assert.fail();
        } finally {
            IO.closeQuiet(conn);
        }
    }

    @Test
    public void test2() throws SQLException {
        AbstractDialect d = new StandardDatabaseDialect();
        Connection conn = this.connection;
        System.out.println(d.getSchema(conn));
    }

    @Test
    public void test3() throws SQLException {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            System.out.println(StringUtils.toString(Jdbc.getTypeInfo(dao.getConnection())));

            DatabaseProcedure proc = dao.getDialect().getProcedureForceOne(dao.getConnection(), null, null, "PROC_TEST_xxx");
            DatabaseDDL ddl = dao.getDialect().toDDL(dao.getConnection(), proc);
            DatabaseProcedure bj = StandardDatabaseProcedure.toProcedure(dao, ddl.get(0));
            System.out.println(bj.toCallProcedureString());
        } finally {
            dao.close();
        }
    }

    @Test
    public void test4() throws SQLException {
        ClassUtils.loadClass("oracle.jdbc.driver.OracleDriver");
        Connection conn = Jdbc.getConnection("jdbc:oracle:thin:@ //110.1.5.37:1521/dadb", "lhbb", "lhbb");
        // Connection conn = TESTDB.getConnection();
        try {
            DatabaseDialect dialect = rule.getContext().getBean(DatabaseDialect.class, conn);
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

    @Test
    public void test5() throws SQLException {
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
