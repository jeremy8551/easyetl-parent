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
import icu.etl.ioc.BeanContext;
import icu.etl.log.LogFactory;
import icu.etl.log.STD;
import icu.etl.util.ArrayUtils;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JdbcDaoTest {

    public final static String tableName = "test_table_name_temp".toUpperCase();

    public final static BeanContext context = new BeanContext();

    @Before
    public void setUp() throws Exception {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            DatabaseDialect dialect = dao.getDialect();
            if (dialect.containsTable(dao.getConnection(), null, Jdbc.getSchema(tableName), Jdbc.removeSchema(tableName))) {
                dao.executeByJdbc("drop table " + tableName);
            }

            String catalog = dao.getCatalog();
            String schema = dao.getSchema();
            dao.executeByJdbc("create table " + tableName + "  (id int, name char(100)  )");
            dao.closeStatement();
            dao.executeByJdbc("insert into " + tableName + "  (id, name) values (1, '名字1')");
            JdbcDao.executeByJdbc(dao.getConnection(), "insert into " + tableName + "  (id, name) values (2, '名字2')");

            dao.executeByJdbcQuietly("drop PROCEDURE TEST_PROC ");

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
                JdbcDao.executeByJdbc(dao.getConnection(), sql);
            }

            dao.commit();
            assertTrue(true);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testtoDDL() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            DatabaseProcedure procedure = dao.getDialect().getProcedureForceOne(dao.getConnection(), dao.getCatalog(), dao.getSchema(), "TEST_PROC");
            DatabaseDDL ddl = dao.toDDL(procedure);
            if (ddl == null) {
                throw new NullPointerException();
            }

            String str = ddl.toString();
            STD.out.debug(str);
            assertTrue(StringUtils.startsWith(str, "create", 0, true, true));
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testgetSchema() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            DatabaseTable table = dao.getTable(dao.getCatalog(), dao.getSchema(), tableName);
            String schema = table.getSchema();
            assertTrue(schema.equalsIgnoreCase(dao.getSchema()));

            assertTrue(dao.containsTable(null, schema, table.getName()));

            assertTrue(dao.getTable(null, schema, table.getName()) != null);

            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
        } finally {
            dao.close();
        }
    }

    @Test
    public void testSetConnection() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            assertTrue(dao.existsConnection());
            assertTrue(true);
            dao.rollback();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExistsConnection() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            assertTrue(dao.existsConnection());
            assertTrue(true);
            dao.rollback();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testConnection() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            assertTrue(dao.testConnection());
            dao.rollback();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.commit();
            dao.close();
        }
    }

    @Test
    public void testQueryFirstRowFirstColByJdbcString() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            Integer num = (Integer) dao.queryFirstRowFirstColByJdbc("select id, name from " + tableName + " order by id desc");
            dao.commit();
            assertTrue(num.intValue() == 2);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQuery() throws SQLException {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            JdbcQueryStatement query = dao.query("select id, name from " + tableName + " where id = ? and name = ? ", -1, -1, 1, "名字1");
            query.query();
            assertTrue(query.next());
            dao.commit();
            assertTrue(true);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            Integer count = dao.queryCountByJdbc("select count(*) from " + tableName + " with ur "); // 查询结果集笔数
            System.out.println("total is " + count);
            Ensure.isTrue(count != null && count > 0);
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryFirstColumnByJdbc() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            List<String> list = dao.queryFirstColumnByJdbc("select id, name from " + tableName + " order by id desc");
            dao.commit();
            assertTrue(list.size() == 2 && StringUtils.objToStr(list.get(0)).equals("2") && StringUtils.objToStr(list.get(1)).equals("1"));
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryCountByJdbcString() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            int num = dao.queryCountByJdbc("select count(*) from " + tableName + " ");
            dao.commit();
            assertTrue(num == 2);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryMapByJdbcString() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            Map<String, String> map = dao.queryMapByJdbc("select id, name from " + tableName + " ");
            dao.commit();

            assertTrue(map.size() == 2 & map.get("1").equals("名字1") && map.get("2").equals("名字2"));
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryMapByJdbcStringIntInt() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            Map<String, String> map = dao.queryMapByJdbc("select id, name from " + tableName + " ", 1, 2);
            dao.commit();

            assertTrue(map.size() == 2 & map.get("1").equals("名字1") && map.get("2").equals("名字2"));
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryMapByJdbcStringStringString() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            Map<String, String> map = dao.queryMapByJdbc("select id, name from " + tableName + " ", "id", "name");
            dao.commit();

            assertTrue(map.size() == 2 & map.get("1").equals("名字1") && map.get("2").equals("名字2"));
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteUpdateByJdbcStringArray() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            String[] array2 = new String[]{"update " + tableName + " set name='名字11' where id = 1", "update " + tableName + " set name='名字22' where id = 2"};
            int[] array = dao.executeUpdateByJdbc(array2);
            dao.commit();

            assertTrue(array.length == 2 && array[0] == 1 && array[1] == 1);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteUpdateByJdbcListOfString() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            List<String> list = ArrayUtils.asList("update " + tableName + " set name='名字11' where id = 1", "update " + tableName + " set name='名字22' where id = 2");
            int[] array = dao.executeUpdateByJdbc(list);
            dao.commit();
            assertTrue(array.length == 2 && array[0] == 1 && array[1] == 1);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteUpdateByJdbcString() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            int val = dao.executeUpdateByJdbc("update " + tableName + " set name='名字11' where id = 1");
            dao.commit();

            assertTrue(val == 1);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
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
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            List<Map<String, String>> list = dao.queryListMapByJdbc("select * from " + tableName + " order by id asc");

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
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testResultToList() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
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
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testCurrentRowToMap() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            PreparedStatement ps = dao.getConnection().prepareStatement("select * from " + tableName + " order by id asc");
            ResultSet result = ps.executeQuery();
            if (result.next()) {
                Map<String, String> map = Jdbc.resultToMap(result, true);
                assertTrue(map.get("id").equals("1"));
                assertTrue(StringUtils.trimBlank(map.get("name")).equals("名字1"));
            }
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }

    }

    @Test
    public void testResultToMap() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            JdbcQueryStatement query = new JdbcQueryStatement(dao.getConnection(), "select id, name from " + tableName + " order by id asc");
            ResultSet result = query.query();
            Map<String, String> map = Jdbc.resultToMap(result, 1, 2);
            dao.commit();

            assertTrue(map.size() == 2 && StringUtils.trimBlank(map.get("1")).equals("名字1"));
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExistsTable() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            assertTrue(dao.containsTable(null, null, tableName));
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteByJdbcQuietlyString() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            dao.executeByJdbcQuietly("drop table tabletestseljlskjdflk ");

            assertTrue(true);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteByJdbcQuiet() {
        LogFactory.turnOff();
        JdbcDao dao = null;
        try {
            dao = new JdbcDao(context, TestEnv.getConnection());
            dao.executeByJdbcQuiet("drop table tabletestseljlskjdflk ");

            dao.rollback();
            dao.close();
        } catch (Exception e) {
            dao.rollback();
            assertTrue(false);
        } finally {
            LogFactory.turnOn();
        }
    }

    @Test
    public void testExecuteUpdateByJdbcQuietly() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            dao.executeUpdateByJdbcQuietly("delete from tanbl lsdkfjlkjlksdjf ");
            assertTrue(true);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteUpdateByJdbcQuiet() {
        LogFactory.turnOff();
        JdbcDao dao = null;
        try {
            dao = new JdbcDao(context, TestEnv.getConnection());
            dao.executeUpdateByJdbcQuiet("update " + tableName + "_tset set name='' where 2=1");
            dao.rollback();
            dao.close();
            assertTrue(true);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            LogFactory.turnOn();
        }
    }

    @Test
    public void testCallProcedureByJdbcString() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            DatabaseProcedure result = dao.callProcedureByJdbc("call " + dao.getSchema() + ".TEST_PROC(?)");
            assertTrue(result != null);
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
            assertTrue(false);
        } finally {
            dao.commit();
            dao.close();
        }
    }

    @Test
    public void testCallProcedureByJdbcStringJdbcCallProcedure() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            DatabaseProcedure proc = dao.callProcedureByJdbc("call " + dao.getSchema() + ".TEST_PROC(?)");
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
            assertTrue(false);
        } finally {
            dao.commit();
            dao.close();
        }
    }

    @Test
    public void testExecuteUpdateByJdbcConnectionString() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            int result = JdbcDao.executeUpdateByJdbc(dao.getConnection(), "delete from " + tableName + " where id = 1");
            assertTrue(result == 1);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteByJdbcConnectionString() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            boolean result = JdbcDao.executeByJdbc(dao.getConnection(), "delete from " + tableName + " where id = 2");
            dao.commit();
            assertTrue(!result);
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteCreateTableConnectionDatabaseDialectDatabaseTableInfoBoolean() {
        BeanContext cxt = new BeanContext();
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            Connection conn = dao.getConnection();
            DatabaseDialect dialect = cxt.get(DatabaseDialect.class, conn);
            List<DatabaseTable> list = dialect.getTable(conn, dao.getCatalog(), dao.getSchema(), Jdbc.removeSchema(tableName));
            if (list.size() > 1) {
                throw new RuntimeException();
            }

            DatabaseTable table = list.get(0);
            DatabaseTableDDL ddl = dao.toDDL(table);
            dao.executeByJdbc("drop table " + table.getFullName());
            dao.createTable(ddl);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteCreateTableConnectionDatabaseDialectDatabaseTableInfoBooleanBooleanBoolean() {
        BeanContext cxt = new BeanContext();
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            Connection conn = dao.getConnection();
            DatabaseDialect dialect = cxt.get(DatabaseDialect.class, conn);
            List<DatabaseTable> list = dialect.getTable(conn, null, dao.getSchema(), Jdbc.removeSchema(tableName));
            if (list.size() > 1) {
                throw new RuntimeException();
            }

            DatabaseTable table = list.get(0);
            DatabaseTableDDL ddl = dao.toDDL(table);

            dao.executeByJdbc("drop table " + table.getFullName());
            dao.createTable(ddl);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
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
//			assertTrue(false);
//		} finally {
//			dao.close();
//		}
//	}

    @Test
    public void testExecuteDropTableIndexConnectionDatabaseTableInfoBoolean() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
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
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteCreateTableIndexConnectionDatabaseDialectDatabaseTableInfoBoolean() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
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
            dao.executeByJdbc(ddl);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testExecuteCreateTableIndexConnectionDatabaseDialectDatabaseIndexBoolean() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
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
            dao.executeByJdbc(ddl);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryListMapsByJdbc() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            List<Map<String, String>> list = JdbcDao.queryListMapsByJdbc(dao.getConnection(), "select * from " + tableName + " order by id asc");
            dao.commit();
            assertTrue(list.size() == 2 && StringUtils.trimBlank(list.get(0).get("id")).equals("1"));
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryCountByJdbcConnectionString() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            Connection conn = dao.getConnection();

            assertTrue(JdbcDao.queryCountByJdbc(conn, "select count(*) from " + tableName) == 2);
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
        } finally {
            dao.close();
        }
    }

    @Test
    public void testQueryCountByJdbcDataSourceString() {
        BeanContext context = new BeanContext();
        DataSource dataSource = new SimpleDatasource(context, TestEnv.getJdbcconfig());
        try {
            assertTrue(JdbcDao.queryCountByJdbc(dataSource, "select count(*) from " + tableName) == 2);
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            Jdbc.closeDataSource(dataSource);
        }
    }

    @Test
    public void testQueryFirstRowFirstColByJdbcConnectionString() {
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            Connection conn = dao.getConnection();

            Object id = JdbcDao.queryFirstRowFirstColByJdbc(conn, "select id, name from " + tableName + " order by id asc");
            assertTrue(id != null && StringUtils.trimBlank(StringUtils.objToStr(id)).equals("1"));
            dao.commit();
        } catch (Exception e) {
            dao.rollback();
            e.printStackTrace();
            assertTrue(false);
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
        assertTrue(!param.isExpression());
        param.setExpression("$abc+");
        assertTrue(!param.isExpression());
    }
}
