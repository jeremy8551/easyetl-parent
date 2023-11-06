package icu.etl.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Iterator;

import icu.etl.ioc.EasyBeanContext;
import icu.etl.util.IO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class QueryManagerTest { // TODO 需要重新测试

    public final static String tableName = "TEST.test_table_name_temp".toUpperCase();

    @Rule
    public WithDBRule rule = new WithDBRule();

    /** 数据库连接 */
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        EasyBeanContext context = rule.getContext();
        this.connection = rule.getConnection();
        try {
            DatabaseDialect dialect = context.getBean(DatabaseDialect.class, this.connection);
            if (dialect.containsTable(this.connection, null, Jdbc.getSchema(tableName), Jdbc.removeSchema(tableName))) {
                JdbcDao.execute(this.connection, "drop table " + tableName);
            }

            JdbcDao.execute(this.connection, "create table " + tableName + "  (id int, name char(100)  )");
            JdbcDao.execute(this.connection, "insert into " + tableName + "  (id, name) values (1, '名字1')");
            JdbcDao.execute(this.connection, "insert into " + tableName + "  (id, name) values (2, '名字2')");

            this.connection.commit();
            assertTrue(true);
        } catch (Exception e) {
            this.connection.rollback();
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testInitConnectionStringIntInt() {
        Connection conn = this.connection;
        try {
            JdbcQueryStatement query = new JdbcQueryStatement(conn, "select * from " + tableName, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            ResultSet result = query.query();
            String[] colNames = Jdbc.getColumnName(result);
            while (query.next()) {
                Iterator<String> it = Arrays.asList(colNames).iterator();
                StringBuilder sb = new StringBuilder();
                while (it.hasNext()) {
                    String name = it.next();
                    sb.append(result.getObject(name));
                    if (it.hasNext()) {
                        sb.append(", ");
                    }
                }
                System.out.println(sb.toString());
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            IO.close(conn);
        }
    }

}
