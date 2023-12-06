package icu.etl.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import icu.etl.database.internal.StandardDatabaseIndex;
import icu.etl.database.internal.StandardDatabaseProcedureParameter;
import icu.etl.database.pool.SimpleDatasource;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JdbcDaoTest {

    public final static String tableName = "test_table_name_temp".toUpperCase();

    @Rule
    public WithDBRule rule = new WithDBRule();

    /** 数据库连接 */
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        JdbcDao dao = new JdbcDao(rule.getContext(), rule.getConnection());
        try {
            DatabaseDialect dialect = dao.getDialect();
            if (dialect.containsTable(dao.getConnection(), null, Jdbc.getSchema(tableName), Jdbc.removeSchema(tableName))) {
                dao.execute("drop table " + tableName);
            }

            String catalog = dao.getCatalog();
            String schema = dao.getSchema();
            dao.execute("create table " + tableName + "  (id int, name char(100)  )");
            dao.closeStatement();
            dao.execute("insert into " + tableName + "  (id, name) values (1, '名字1')");
            JdbcDao.execute(dao.getConnection(), "insert into " + tableName + "  (id, name) values (2, '名字2')");
            dao.executeQuietly("drop PROCEDURE TEST_PROC ");

            String sql = "";
            sql += "CREATE PROCEDURE TEST_PROC            " + FileUtils.lineSeparator;
            sql += " (OUT FLAG INTEGER                          " + FileUtils.lineSeparator;
            sql += " )                                            " + FileUtils.lineSeparator;
            sql += "  LANGUAGE SQL                                " + FileUtils.lineSeparator;
            sql += "  NOT DETERMINISTIC                           " + FileUtils.lineSeparator;
            sql += "  CALLED ON NULL INPUT                        " + FileUtils.lineSeparator;
            sql += "  EXTERNAL ACTION                             " + FileUtils.lineSeparator;
            sql += "  OLD SAVEPOINT LEVEL                         " + FileUtils.lineSeparator;
            sql += "  MODIFIES SQL DATA                           " + FileUtils.lineSeparator;
            sql += "  INHERIT SPECIAL REGISTERS                   " + FileUtils.lineSeparator;
            sql += "  BEGIN                                       " + FileUtils.lineSeparator;
            sql += "  SET FLAG = 1;                               " + FileUtils.lineSeparator;
            sql += "  RETURN 0;                                   " + FileUtils.lineSeparator;
            sql += "END                                           " + FileUtils.lineSeparator;
            DatabaseProcedure proc = dialect.getProcedureForceOne(dao.getConnection(), catalog, schema, "TEST_PROC");
            if (proc == null) {
                JdbcDao.execute(dao.getConnection(), sql);
            }

            dao.commit();

            connection = dao.getConnection();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void setDown() {
        IO.close(this.connection);
    }

    @Test
    public void testtoDDL() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            DatabaseProcedure procedure = dao.getDialect().getProcedureForceOne(dao.getConnection(), dao.getCatalog(), dao.getSchema(), "TEST_PROC");
            DatabaseDDL ddl = dao.toDDL(procedure);
            if (ddl == null) {
                throw new NullPointerException();
            }

            String str = ddl.toString();
            System.out.println(str);
            assertTrue(StringUtils.startsWith(str, "create", 0, true, true));
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testgetSchema() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            DatabaseTable table = dao.getTable(dao.getCatalog(), dao.getSchema(), tableName);
            String schema = table.getSchema();
            assertTrue(schema.equalsIgnoreCase(dao.getSchema()));
            assertTrue(dao.containsTable(null, schema, table.getName()));
            assertNotNull(dao.getTable(null, schema, table.getName()));
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testSetConnection() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            assertTrue(dao.existsConnection());
            assertTrue(true);
            dao.rollback();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExistsConnection() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            assertTrue(dao.existsConnection());
            assertTrue(true);
            dao.rollback();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testConnection() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            assertTrue(dao.testConnection());
            dao.rollback();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.commit();
            dao.close();
        }
    }

    @Test
    public void testQueryFirstRowFirstColByJdbcString() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            Integer num = (Integer) dao.queryFirstRowFirstCol("select id, name from " + tableName + " order by id desc");
            dao.commit();
            assertEquals(2, num.intValue());
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQuery() throws SQLException {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            JdbcQueryStatement query = dao.query("select id, name from " + tableName + " where id = ? and name = ? ", -1, -1, 1, "名字1");
            query.query();
            assertTrue(query.next());
            dao.commit();
            assertTrue(true);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Integer count = dao.queryCount("select count(*) from " + tableName + " with ur "); // 查询结果集笔数
            System.out.println("total is " + count);
            Assert.assertNotNull(count);
            Assert.assertTrue(count > 0);
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryFirstColumnByJdbc() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            List<String> list = dao.queryFirstColumn("select id, name from " + tableName + " order by id desc");
            dao.commit();
            assertTrue(list.size() == 2 && StringUtils.objToStr(list.get(0)).equals("2") && StringUtils.objToStr(list.get(1)).equals("1"));
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryCountByJdbcString() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            int num = dao.queryCount("select count(*) from " + tableName + " ");
            dao.commit();
            assertEquals(2, num);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryMapByJdbcString() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            Map<String, String> map = dao.queryMap("select id, name from " + tableName + " ");
            dao.commit();
            assertEquals(2, map.size());
            assertEquals("名字1", map.get("1"));
            assertEquals("名字2", map.get("2"));
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryMapByJdbcStringIntInt() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            Map<String, String> map = dao.queryMap("select id, name from " + tableName + " ", 1, 2);
            dao.commit();
            assertEquals(2, map.size());
            assertEquals("名字1", map.get("1"));
            assertEquals("名字2", map.get("2"));
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryMapByJdbcStringStringString() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            Map<String, String> map = dao.queryMap("select id, name from " + tableName + " ", "id", "name");
            dao.commit();
            assertEquals(2, map.size());
            assertEquals("名字1", map.get("1"));
            assertEquals("名字2", map.get("2"));
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteUpdateByJdbcStringArray() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            String[] array2 = new String[]{"update " + tableName + " set name='名字11' where id = 1", "update " + tableName + " set name='名字22' where id = 2"};
            int[] array = dao.executeUpdate(array2);
            dao.commit();
            assertEquals(2, array.length);
            assertEquals(1, array[0]);
            assertEquals(1, array[1]);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteUpdateByJdbcListOfString() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            List<String> list = ArrayUtils.asList("update " + tableName + " set name='名字11' where id = 1", "update " + tableName + " set name='名字22' where id = 2");
            int[] array = dao.executeUpdate(list);
            dao.commit();
            assertTrue(array.length == 2 && array[0] == 1 && array[1] == 1);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteUpdateByJdbcString() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            int val = dao.executeUpdate("update " + tableName + " set name='名字11' where id = 1");
            dao.commit();
            assertEquals(1, val);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteByJdbcString() {
        assertTrue(true);
    }

    @Test
    public void testQueryListMapByJdbc() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            List<Map<String, String>> list = dao.queryListMap("select * from " + tableName + " order by id asc");

            assertTrue(list.size() == 2 //
                    && list.get(0).get("id").equals("1") //
                    && list.get(0).get("name").equals("名字1") //
                    && list.get(1).get("id").equals("2") //
                    && list.get(1).get("name").equals("名字2") //
            );
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testResultToList() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            PreparedStatement ps = dao.getConnection().prepareStatement("select * from " + tableName + " order by id asc");
            ResultSet result = ps.executeQuery();
            List<Map<String, String>> list = Jdbc.resultToList(result);

            assertTrue(list.size() == 2 //
                    && StringUtils.trimBlank(list.get(0).get("id")).equals("1") //
                    && StringUtils.trimBlank(list.get(0).get("name")).equals("名字1") //
                    && StringUtils.trimBlank(list.get(1).get("id")).equals("2") //
                    && StringUtils.trimBlank(list.get(1).get("name")).equals("名字2") //
            );
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testCurrentRowToMap() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            PreparedStatement ps = dao.getConnection().prepareStatement("select * from " + tableName + " order by id asc");
            ResultSet result = ps.executeQuery();
            if (result.next()) {
                Map<String, String> map = Jdbc.resultToMap(result, true);
                assertEquals("1", map.get("id"));
                assertEquals("名字1", StringUtils.trimBlank(map.get("name")));
            }
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }

    }

    @Test
    public void testResultToMap() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            JdbcQueryStatement query = new JdbcQueryStatement(dao.getConnection(), "select id, name from " + tableName + " order by id asc");
            ResultSet result = query.query();
            Map<String, String> map = Jdbc.resultToMap(result, 1, 2);
            dao.commit();
            assertEquals(2, map.size());
            assertEquals("名字1", StringUtils.trimBlank(map.get("1")));
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExistsTable() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            assertTrue(dao.containsTable(null, null, tableName));
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteByJdbcQuietlyString() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            dao.executeQuietly("drop table tabletestseljlskjdflk ");
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteByJdbcQuiet() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            dao.executeQuiet("drop table tabletestseljlskjdflk ");
            dao.rollback();
            dao.close();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testExecuteUpdateByJdbcQuietly() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            dao.executeUpdateQuietly("delete from tanbl lsdkfjlkjlksdjf ");
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteUpdateByJdbcQuiet() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            dao.executeUpdateQuiet("update " + tableName + "_tset set name='' where 2=1");
            dao.rollback();
            dao.close();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCallProcedureByJdbcString() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            DatabaseProcedure result = dao.callProcedure("call " + dao.getSchema() + ".TEST_PROC(?)");
            assertNotNull(result);
            List<DatabaseProcedureParameter> params = result.getParameters();
            assertTrue(params.size() == 1 //
                    && params.get(0).getName().equalsIgnoreCase("flag") //
                    && params.get(0).getSqlType() == Types.INTEGER //
                    && ((Integer) params.get(0).getValue()).intValue() == 1 //
            );
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.commit();
            dao.close();
        }
    }

    @Test
    public void testCallProcedureByJdbcStringJdbcCallProcedure() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            DatabaseProcedure proc = dao.callProcedure("call " + dao.getSchema() + ".TEST_PROC(?)");
            List<DatabaseProcedureParameter> params = proc.getParameters();
            assertTrue(params.size() == 1 //
                    && params.get(0).getName().equalsIgnoreCase("flag") //
                    && params.get(0).getSqlType() == Types.INTEGER //
                    && ((Integer) params.get(0).getValue()).intValue() == 1 //
            );

            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.commit();
            dao.close();
        }
    }

    @Test
    public void testExecuteUpdateByJdbcConnectionString() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            int result = JdbcDao.executeUpdate(dao.getConnection(), "delete from " + tableName + " where id = 1");
            assertEquals(1, result);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteByJdbcConnectionString() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            boolean result = JdbcDao.execute(dao.getConnection(), "delete from " + tableName + " where id = 2");
            dao.commit();
            assertFalse(result);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteCreateTableConnectionDatabaseDialectDatabaseTableInfoBoolean() {
        EasyBeanContext context = rule.getContext();
        JdbcDao dao = new JdbcDao(context, this.connection);
        try {
            Connection conn = dao.getConnection();
            DatabaseDialect dialect = context.getBean(DatabaseDialect.class, conn);
            List<DatabaseTable> list = dialect.getTable(conn, dao.getCatalog(), dao.getSchema(), Jdbc.removeSchema(tableName));
            if (list.size() > 1) {
                throw new RuntimeException();
            }

            DatabaseTable table = list.get(0);
            DatabaseTableDDL ddl = dao.toDDL(table);
            dao.execute("drop table " + table.getFullName());
            dao.createTable(ddl);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteCreateTableConnectionDatabaseDialectDatabaseTableInfoBooleanBooleanBoolean() {
        EasyBeanContext context = rule.getContext();
        JdbcDao dao = new JdbcDao(context, this.connection);
        try {
            Connection conn = dao.getConnection();
            DatabaseDialect dialect = context.getBean(DatabaseDialect.class, conn);
            List<DatabaseTable> list = dialect.getTable(conn, null, dao.getSchema(), Jdbc.removeSchema(tableName));
            if (list.size() > 1) {
                throw new RuntimeException();
            }

            DatabaseTable table = list.get(0);
            DatabaseTableDDL ddl = dao.toDDL(table);
            dao.execute("drop table " + table.getFullName());
            dao.createTable(ddl);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

//	@Test
//	public void testExecuteDropTableIndexConnectionDatabaseDialectStringStringBoolean() {
//		JdbcDao dao = new JdbcDao(Env.getConnection());
//		try {
//			dao.dropIndex(Jdbcs.getSchemaFromTableName(tableName), Jdbcs.removeSchemaFromTableName(tableName));
//			dao.commit();
//		} catch (Exception e) {
//			dao.rollback();
//			e.printStackTrace();
//			Assert.fail();
//		} finally {
//			dao.close();
//		}
//	}

    @Test
    public void testExecuteDropTableIndexConnectionDatabaseTableInfoBoolean() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            Connection conn = dao.getConnection();
            DatabaseDialect dialect = dao.getDialect();
            List<DatabaseTable> list = dialect.getTable(conn, dao.getCatalog(), dao.getSchema(), Jdbc.removeSchema(tableName));
            if (list.size() > 1) {
                throw new RuntimeException();
            }

            DatabaseTable table = list.get(0);
            dao.dropIndex(table);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteCreateTableIndexConnectionDatabaseDialectDatabaseTableInfoBoolean() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            String schema = dao.getSchema();

            StandardDatabaseIndex idx = new StandardDatabaseIndex();
            idx.setTableCatalog(null);
            idx.setTableSchema(schema);
            idx.setTableName(tableName);
            idx.setTableFullName(dao.getDialect().toTableName(idx.getTableCatalog(), idx.getTableSchema(), idx.getTableName()));
            idx.setSchema(schema);
            idx.setName("idxnametest");
            idx.setFullName(dao.getDialect().toTableName(null, idx.getSchema(), idx.getName()));
            idx.setColumnNames(ArrayUtils.asList("id"));
            idx.setSort(ArrayUtils.asList(new Integer(DatabaseIndex.INDEX_ASC)));

            DatabaseDDL ddl = dao.getDialect().toDDL(dao.getConnection(), idx, false);
            dao.execute(ddl);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteCreateTableIndexConnectionDatabaseDialectDatabaseIndexBoolean() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            String schema = dao.getSchema();

            StandardDatabaseIndex index = new StandardDatabaseIndex();
            index.setTableCatalog(null);
            index.setTableSchema(schema);
            index.setTableName(tableName);
            index.setTableFullName(dao.getDialect().toTableName(index.getTableCatalog(), index.getTableSchema(), index.getTableName()));
            index.setName("idxnametest");
            index.setSchema(schema);
            index.setFullName(dao.getDialect().toIndexName(null, index.getSchema(), index.getName()));
            index.setColumnNames(ArrayUtils.asList("id"));
            index.setSort(ArrayUtils.asList(new Integer(DatabaseIndex.INDEX_ASC)));

            DatabaseDDL ddl = dao.getDialect().toDDL(dao.getConnection(), index, false);
            dao.execute(ddl);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryListMapsByJdbc() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            List<Map<String, String>> list = JdbcDao.queryListMaps(dao.getConnection(), "select * from " + tableName + " order by id asc");
            dao.commit();
            Assert.assertTrue(list.size() == 2 && StringUtils.trimBlank(list.get(0).get("id")).equals("1"));
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryCountByJdbcConnectionString() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            Connection conn = dao.getConnection();
            assertEquals(2, (int) JdbcDao.queryCount(conn, "select count(*) from " + tableName));
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryCountByJdbcDataSourceString() {
        DataSource dataSource = new SimpleDatasource(rule.getContext(), rule.getProperties());
        try {
            assertEquals(2, (int) JdbcDao.queryCount(dataSource, "select count(*) from " + tableName));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            Jdbc.closeDataSource(dataSource);
        }
    }

    @Test
    public void testQueryFirstRowFirstColByJdbcConnectionString() {
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            Connection conn = dao.getConnection();
            Object id = JdbcDao.queryFirstRowFirstCol(conn, "select id, name from " + tableName + " order by id asc");
            assertTrue(id != null && StringUtils.trimBlank(StringUtils.objToStr(id)).equals("1"));
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Assert.fail();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testIsLinuxVariableName() {
        DatabaseProcedureParameter param = new StandardDatabaseProcedureParameter();
        param.setExpression("$1");
        assertTrue(param.isExpression());

        param.setExpression("$abc");
        assertTrue(param.isExpression());
        param.setExpression("$a_bc");
        assertTrue(param.isExpression());
        param.setExpression("$_a_bc");
        assertTrue(param.isExpression());
        param.setExpression("abc");
        assertFalse(param.isExpression());
        param.setExpression("$abc+");
        assertFalse(param.isExpression());
    }
}
