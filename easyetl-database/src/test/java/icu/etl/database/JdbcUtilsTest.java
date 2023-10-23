package icu.etl.database;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import icu.etl.collection.ByteBuffer;
import icu.etl.collection.CaseSensitivSet;
import icu.etl.database.db2.DB2ExportFile;
import icu.etl.database.internal.StandardDatabaseProcedure;
import icu.etl.database.oracle.OracleDialect;
import icu.etl.ioc.BeanContext;
import icu.etl.os.OSConnectCommand;
import icu.etl.util.ArrayUtils;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.JVM)
public class JdbcUtilsTest {

    @Test
    public void testremoveSchema() {
        assertEquals(Jdbc.removeSchema(""), "");
        assertEquals(Jdbc.removeSchema("table"), "table");
        assertEquals(Jdbc.removeSchema(".table"), "table");
        assertEquals(Jdbc.removeSchema("1.table"), "table");
        assertEquals(Jdbc.removeSchema("schema.table"), "table");
        assertEquals(Jdbc.removeSchema("schema.sdsf.table"), "table");
    }

    @Test
    public void testgetSchema() {
        assertEquals(Jdbc.getSchema(""), null);
        assertEquals(Jdbc.getSchema("table"), null);
        assertEquals(Jdbc.getSchema(".table"), null);
        assertEquals(Jdbc.getSchema("1.table"), "1");
        assertEquals(Jdbc.getSchema("schema.table"), "schema");
        assertEquals(Jdbc.getSchema("schema.sdsf.table"), "schema");
    }

    @Test
    public void testGetTypeInfo() {
        Connection conn = TestEnv.getConnection();
        try {
            DatabaseTypeSet typeInfo = Jdbc.getTypeInfo(conn);
            System.out.println(typeInfo.toString());

            Jdbc.rollback(conn);
        } catch (Exception e) {
            Jdbc.rollback(conn);
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            IO.closeQuietly(conn);
        }
    }

    @Test
    public void testQuote() {
        assertTrue(StringUtils.quote(null) == null);
        assertTrue(StringUtils.quote("").equals("''"));
        assertTrue(StringUtils.quote(" ").equals("' '"));
    }

    @Test
    public void test2() {
        assertTrue(StringUtils.left("1234567890", 10, null, ' ').equals("1234567890")); // 判断字符串是否相等
        assertTrue(StringUtils.left("1234567890", 11, null, ' ').equals("1234567890 ")); // 判断字符串是否相等
        assertTrue(StringUtils.left("1234567890", 12, null, ' ').equals("1234567890  ")); // 判断字符串是否相等
        Assert.assertEquals("1234567截", StringUtils.left("1234567截取", 10, null, ' '));
        Assert.assertEquals("1234567截取字", StringUtils.left("1234567截取", 16, null, '字'));
    }

    @Test
    public void test3() {
//		assertTrue("abcABC一二三四壹 ".toUpperCase(Locale.CHINESE));
        assertTrue(StringUtils.toCase("abcABC", false, null).equals("ABCABC"));
        assertTrue(StringUtils.toString(StringUtils.toCase(ArrayUtils.asList("a", "b", "c"), false, null)).equals("ArrayList[A, B, C]"));
        assertTrue(StringUtils.toString(StringUtils.toCase(new String[]{"a", "b", "cd"}, false, null)).equals("String[A, B, CD]"));
        assertTrue(StringUtils.toString(new Integer[]{0, 1, 2}).equals("Integer[0, 1, 2]"));
        assertTrue(StringUtils.toString(new int[]{1, 2, 3}).equals("int[1, 2, 3]"));
        assertTrue(StringUtils.toString(StringUtils.toCase(new char[]{'a', 'b', 'c', '1'}, false, null)).equals("char[A, B, C, 1]"));
    }

    @Test
    public void test4() {
        Date date = new Date();
        String time = StringUtils.right(Dates.format21(date), 3); // 毫秒数
        String str = StringUtils.left(Dates.format21(date), 20);
        String str1 = DB2ExportFile.toDB2ExportString(new Timestamp(date.getTime()));
        System.out.println("测试db2数据库中数据日期是否正确");
        System.out.println(str1);
        System.out.println(str + "000" + time);
        String result = str + "000" + time;
        assertTrue(str1.equals(result.replace(':', '.')));
    }

    @Test
    public void test51() {
        Connection conn = TestEnv.getConnection();
        try {
            CaseSensitivSet set = Jdbc.getSQLKeywords(conn);
            System.out.println(StringUtils.join(set, "\n"));
            Jdbc.rollback(conn);
        } catch (Exception e) {
            Jdbc.rollback(conn);
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            IO.closeQuietly(conn);
        }
    }

//	@Test
//	public void test5() {
//		Connection conn = UDSFDB.getConnection();
//		try {
//			DatabaseDialect dialect = DatabaseDialectFactory.getDialect(conn);
//			String schema = dialect.getSchema(conn);
//			DatabaseTableInfo table = dialect.getDatabaseTableInfoForceOne(conn, null, schema, "ECC_ENSURECONTRACTS_R"); // 创建测试表
//			assertTrue(table.matchTableName("", "ECC_ENSURECONTRACTS_R"));
//			assertTrue(table.matchTableName("UDSFADM", "ECC_ENSURECONTRACTS_R"));
//			assertTrue(table.matchTableName("1", "ECC_ENSURECONTRACTS_R") == false);
//			Jdbcs.rollback(conn);
//		} catch (Exception e) {
//			e.printStackTrace();
//			Jdbcs.rollback(conn);
//		} finally {
//			IOUtils.closeQuietly(conn);
//		}
//	}

    @Test
    public void test6() {
        String[][] array1 = OracleDialect.resolveDatabaseProcedureParam("procedure name(v1 number(12, 2), dt in varchar2char(100), r  out  int, t char, d in decimal(10,2) )");
        assertTrue(StringUtils.toString(array1[0]).equals("String[V1, IN, NUMBER(12, 2)]"));
        assertTrue(StringUtils.toString(array1[1]).equals("String[DT, IN, VARCHAR2CHAR(100)]"));
        assertTrue(StringUtils.toString(array1[4]).equals("String[D, IN, DECIMAL(10,2)]"));

        String[] array2 = StandardDatabaseProcedure.resolveDatabaseProcedureDDLName("CREATE OR REPLACE PROCEDURE \"LHBB\".\"CUSTAUM_APPEND\" (dt in varchar2)--yyyymmdd \n as");
        assertTrue(StringUtils.toString(array2).equals("String[\"LHBB\", \"CUSTAUM_APPEND\"]"));
    }

//	@Test
//	public void test7() {
//		Connection conn = UDSFDB.getConnection();
//		try {
//			DatabaseDialect dialect = DatabaseDialectFactory.getDialect(conn);
//			String schema = dialect.getSchema(conn);
//			DatabaseTableInfo table1 = dialect.getDatabaseTableInfoForceOne(conn, null, schema, "ECC_ENSURECONTRACTS_R");
//			DatabaseTableInfo table2 = dialect.getDatabaseTableInfoForceOne(conn, null, schema, "ECC_ENSURECONTRACTS");
////			List<DatabaseTableColumn> columns = table1.getColumns();
//			
//			List<DatabaseIndex> primaryIndexs = table1.getPrimaryIndexs();
//			DatabaseIndex pkg = primaryIndexs.get(0);
//			DatabaseTableColumn[] searchDatabaseTableColumn = table1.indexOfColumns(Collections.toArray(pkg.getColumnName()));
//			String[] databaseTableFieldNames = DatabaseTableColumn.toDatabaseTableColumnNames(searchDatabaseTableColumn);
//			List<String> list = Arrays.toList(databaseTableFieldNames);
//			
//			System.out.println(Jdbcs.getDoubleDatabaseTableDeleteFromSql(conn, null, table2.getSchema(), table2.getName(), list, table1.getSchema(), table1.getName(), list));
//			assertTrue(true);
//			
//			Jdbcs.rollback(conn);
//		} catch (Exception e) {
//			e.printStackTrace();
//			Jdbcs.rollback(conn);
//		} finally {
//			IOUtils.closeQuietly(conn);
//		}
//	}

    @Test
    public void test8() throws SQLException {
        BeanContext context = new BeanContext();
        String tablename = "";
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            tablename = "test".toUpperCase() + Dates.format17(new Date());
            dao.executeByJdbc("create table " + tablename + "(f1 char(100) not null, f2 char(10), primary key(f1) ) ");
            dao.commit();

            dao.executeByJdbc("create index " + tablename + "idx on " + tablename + "(f2)");
            dao.commit();

            String schema = dao.getSchema();
            DatabaseTable table = dao.getTable(null, schema, tablename);
//			assertTrue(dialect.getDatabaseTableDDL(table));
//			assertTrue(table.getIndexs().size());
            List<DatabaseIndex> primaryIndexs = table.getPrimaryIndexs();
//			assertTrue(primaryIndexs.size());

            DatabaseTable clone = table.clone();
//			assertTrue(dialect.getDatabaseTableDDL(clone));
//			assertTrue(clone.getIndexs().size());
//			assertTrue(clone.getPrimaryIndexs().size());

            assertTrue(clone.getIndexs().contains(primaryIndexs.get(0), false, false) || clone.getPrimaryIndexs().contains(primaryIndexs.get(0), false, false));
            assertTrue(primaryIndexs.get(0).equals(clone.getPrimaryIndexs().get(0), true, true));

            dao.rollback();
        } finally {
            try {
                dao.executeByJdbc("drop table " + tablename);
                dao.commit();
            } finally {
                dao.close();
            }
        }
    }

    @Test
    public void test9() throws SQLException {
        BeanContext context = new BeanContext();
        String tablename = "";
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            assertTrue(dao.testConnection());

            tablename = "test".toUpperCase() + Dates.format17(new Date());
            dao.executeByJdbc("create table " + tablename + "(f1 char(100) not null, f2 char(10), primary key(f1) ) ");
            dao.commit();
            dao.executeByJdbc("create index " + tablename + "idx on " + tablename + "(f2)");
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            dao.close();
        }
    }

    class CharOutputStream extends OutputStream {
        ByteBuffer buf = new ByteBuffer();

        @Override
        public void write(int b) throws IOException {
            buf.append((byte) b);
        }

        public String toString() {
            return this.buf.toString();
        }

    }

    @Test
    public void test10() throws Exception {
        BeanContext context = new BeanContext();
        String tablename = "";
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            System.out.println("testing");
            assertTrue(dao.testConnection());
            System.out.println("finsih test ");

            tablename = "test" + Dates.format17(new Date());
            dao.executeByJdbc("create table " + tablename + "(f1 char(100), f2 char(10) ) ");
            dao.commit();
            System.out.println("finsih create table");

            JdbcQueryStatement qryLastCreditLine = new JdbcQueryStatement(dao.getConnection(), "select * from " + tablename + " a where f1 = ? and f2 < ? ");
            qryLastCreditLine.setParameter("2301052016000008");
            qryLastCreditLine.setParameter("2017-04-25");
            ResultSet result = qryLastCreditLine.query();
            if (result.next()) {
//				int val = result.getInt("credit_line");
//				assertTrue(val + ", " + result.wasNull());
            }

            qryLastCreditLine.close();

            dao.commit();
        } finally {
            try {
                dao.executeByJdbc("drop table " + tablename);
                dao.commit();
            } finally {
                dao.close();
            }
        }
    }

    @Test
    public void testStoreProperties() throws IOException {
        BeanContext context = new BeanContext();
        Properties p = new Properties();
        p.put(Jdbc.driverClassName, "com.ibm.db2.jcc.DB2Driver");
        p.put(Jdbc.url, "jdbc:db2://130.1.10.103:50000/UDSFDB");
        p.put(OSConnectCommand.username, "udsfadm");
        p.put(OSConnectCommand.password, "udsfadm");
        p.put(Jdbc.schema, "UDSF");

        File pf = new File(getFile().getParentFile(), "jdbc.txt");
        FileUtils.delete(pf);
        File jdbcFile = FileUtils.storeProperties(p, pf);

        Properties jdbc = Jdbc.loadJdbcFile(context, jdbcFile.getAbsolutePath());
        Ensure.isTrue(jdbc.getProperty(Jdbc.driverClassName).equals("com.ibm.db2.jcc.DB2Driver"));
        Ensure.isTrue(jdbc.getProperty(Jdbc.url).equals("jdbc:db2://130.1.10.103:50000/UDSFDB"));
        Ensure.isTrue(jdbc.getProperty(OSConnectCommand.username).equals("udsfadm"));
        Ensure.isTrue(jdbc.getProperty(OSConnectCommand.password).equals("udsfadm"));
        Ensure.isTrue(jdbc.getProperty(Jdbc.schema).equals("UDSF"));
    }

    /**
     * 返回一个临时文件
     *
     * @return
     */
    public static File getFile() {
        return getFile(null);
    }

    /**
     * 使用指定用户名创建一个文件
     *
     * @param name
     * @return
     */
    public static File getFile(String name) {
        if (StringUtils.isBlank(name)) {
            name = FileUtils.getFilenameRandom("testfile", "_tmp") + ".txt";
        }

        File dir = new File(FileUtils.getTempDir(FileUtils.class), "单元测试");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("创建目录 " + dir.getAbsolutePath() + " 失败!");
        } else {
            return new File(dir, name); // 返回一个临时文件信息
        }
    }

}
