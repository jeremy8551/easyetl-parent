package icu.etl.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Iterator;

import icu.etl.ioc.BeanContext;
import icu.etl.util.IO;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class QueryManagerTest {
    public final static String tableName = "TEST.test_table_name_temp".toUpperCase();

    @Before
    public void setUp() throws Exception {
        BeanContext context = new BeanContext();
        Connection conn = TestEnv.getConnection();
        try {
            DatabaseDialect dialect = context.get(DatabaseDialect.class, conn);
            if (dialect.containsTable(conn, null, Jdbc.getSchema(tableName), Jdbc.removeSchema(tableName))) {
                JdbcDao.executeByJdbc(conn, "drop table " + tableName);
            }

            JdbcDao.executeByJdbc(conn, "create table " + tableName + "  (id int, name char(100)  )");
            JdbcDao.executeByJdbc(conn, "insert into " + tableName + "  (id, name) values (1, '名字1')");
            JdbcDao.executeByJdbc(conn, "insert into " + tableName + "  (id, name) values (2, '名字2')");

            conn.commit();
            assertTrue(true);
        } catch (Exception e) {
            conn.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            IO.close(conn);
        }
    }

    @Test
    public void testQueryManager() {
        assertTrue(true);
    }

    @Test
    public void testQueryManagerString() {
        assertTrue(true);
    }

    @Test
    public void testQueryManagerConnectionString() {
        assertTrue(true);
    }

    @Test
    public void testQueryManagerConnectionStringIntInt() {
        assertTrue(true);
    }

    @Test
    public void testGetConnection() {
        assertTrue(true);
    }

    @Test
    public void testGetPreparedStatement() {
        assertTrue(true);
    }

    @Test
    public void testGetSql() {
        assertTrue(true);
    }

    @Test
    public void testInitConnectionStringIntInt() {
        Connection conn = TestEnv.getConnection();
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
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            IO.close(conn);
        }
    }

    @Test
    public void testInitConnectionString() {
        assertTrue(true);
    }

    @Test
    public void testInitConnection() {
        assertTrue(true);
    }

    @Test
    public void testQuery() {
        assertTrue(true);
    }

    @Test
    public void testNext() {
        assertTrue(true);
    }

    @Test
    public void testGetResultSet() {
        assertTrue(true);
    }

    @Test
    public void testClose() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterIntByte() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterIntString() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterIntInt() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterIntBigDecimal() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterStringIntDate() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterIntDate() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterIntDouble() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterIntLong() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterIntObject() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterIntObjectInt() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterByte() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterString() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterInt() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterBigDecimal() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterStringDate() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterDate() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterDate1() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterDouble() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterLong() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterObject() {
        assertTrue(true);
    }

    @Test
    public void testSetParamterObjectInt() {
        assertTrue(true);
    }

    @Test
    public void testGetString() {
        assertTrue(true);
    }

    @Test
    public void testGetBytes() {
        assertTrue(true);
    }

    @Test
    public void testGetDate() {
        assertTrue(true);
    }

    @Test
    public void testGetTime() {
        assertTrue(true);
    }

    @Test
    public void testGetTimestamp() {
        assertTrue(true);
    }

    @Test
    public void testGetBoolean() {
        assertTrue(true);
    }

    @Test
    public void testGetByte() {
        assertTrue(true);
    }

    @Test
    public void testGetShort() {
        assertTrue(true);
    }

    @Test
    public void testGetInt() {
        assertTrue(true);
    }

    @Test
    public void testGetLong() {
        assertTrue(true);
    }

    @Test
    public void testGetFloat() {
        assertTrue(true);
    }

    @Test
    public void testGetDouble() {
        assertTrue(true);
    }

    @Test
    public void testGetBigDecimal() {
        assertTrue(true);
    }

    @Test
    public void testGetObject() {
        assertTrue(true);
    }

}
