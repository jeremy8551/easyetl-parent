package icu.etl.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import icu.etl.util.ClassUtils;
import icu.etl.util.IO;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatabaseDialectByDBTest {

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
    public void test() throws SQLException {
        String catalog = null;
        String schema = null;
        JdbcDao dao = new JdbcDao(rule.getContext(), this.connection);
        try {
            String packageName = ClassUtils.getPackageName(DatabaseDialectByDBTest.class, 2);
            String tableName = packageName.replace('.', '_') + "_TEST_TEMP".toUpperCase();
            tableName = tableName.toUpperCase();
            System.out.println("tableName: " + tableName);
            String fullName = dao.getDialect().toTableName(catalog, schema, tableName);
            DatabaseTable table = dao.getTable(catalog, schema, tableName);
            if (table != null) {
                dao.dropTable(table);
                dao.commit();
            }

            String sql = "";
            sql += "create table " + tableName + " (                               \n";
            sql += "    SERIALNO  VARCHAR(32) not null, --  xxx                        \n";
            sql += "    OBJECTNO  VARCHAR(40), --  xxx                                \n";
            sql += "    DOCUMENTTYPE  VARCHAR(30), --  xxx                            \n";
            sql += "    PAYDATE  VARCHAR(10), --  xx                                 \n";
            sql += "    ACTUALPAYDATE  VARCHAR(10), --  xx                         \n";
            sql += "    CURRENCY  VARCHAR(3), --  xx                                   \n";
            sql += "    PAYAMT  DECIMAL(20, 2), --  xx                               \n";
            sql += "    ACTUALPAYAMT  DECIMAL(20, 2), --  xx                         \n";
            sql += "    DEDUCTACCNO1  VARCHAR(40), --  xx                            \n";
            sql += "    DEDUCTACCNO2  VARCHAR(40), --  xx                            \n";
            sql += "    DEDUCTACCNO  VARCHAR(40), --  x                             \n";
            sql += "    BILLSTATUS  VARCHAR(10), --  x                                \n";
            sql += "    RETURNCHANNEL  VARCHAR(20), --  xx                           \n";
            sql += "    OBJECTTYPE  VARCHAR(10), --  xx                              \n";
            sql += "    PAYCORPUSAMT  DECIMAL(20, 2), --  xx                         \n";
            sql += "    ACTUALPAYCORPUSAMT  DECIMAL(20, 2), --  xx                   \n";
            sql += "    PAYINTEAMT  DECIMAL(20, 2), --  xx                           \n";
            sql += "    ACTUALPAYINTEAMT  DECIMAL(20, 2), --  xx                     \n";
            sql += "    PAYFINEAMT  DECIMAL(20, 2), --  xx                           \n";
            sql += "    ACTUALFINEAMT  DECIMAL(20, 2), --  xx                        \n";
            sql += "    PAYCOMPDINTEAMT  DECIMAL(20, 2), --  xx                      \n";
            sql += "    ACTUALCOMPDINTEAMT  DECIMAL(20, 2), --  xxx                   \n";
            sql += "    PAYFEEAMT  DECIMAL(20, 2), --  xxx                            \n";
            sql += "    ACTUALFEEAMT  DECIMAL(20, 2), --  xxx                         \n";
            sql += "    ORGID  VARCHAR(20), --  xxx                                     \n";
            sql += "    TBREPAYSERIALNO  VARCHAR(32), --  xxx                \n";
            sql += "    TBLOANSERIALNO  VARCHAR(32), --  xxx                 \n";
            sql += "    FEETYPE  VARCHAR(20), --  中文测试表                                 \n";
            sql += "    CHECKFLAG  VARCHAR(1), --  中文测试表                                \n";
            sql += "    CHECKACCOUNTNO  VARCHAR(32), --  中文测试表                        \n";
            sql += "    CHECKACCOUNTTYPE  VARCHAR(2), --  中文测试表                       \n";
            sql += "    DEDUCTACCNONAME  VARCHAR(80), --  中文测试表                        \n";
            sql += "    PREPAYCORPUS  DECIMAL(20, 2), --  中文测试表                     \n";
            sql += "    ACTUALPREPAYCORPUS  DECIMAL(20, 2), --  中文测试表               \n";
            sql += "    PREPAYINTEREST  DECIMAL(20, 2), --  中文测试表                   \n";
            sql += "    ACTUALPREPAYINTEREST  DECIMAL(20, 2), --  中文测试表             \n";
            sql += "    PAYMENTORDER  DECIMAL(20, 2),                                  \n";
            sql += "    EXPIATIONSUM  DECIMAL(20, 2),                                  \n";
            sql += "    IFSEND  VARCHAR(2),                                            \n";
            sql += "    CUSTOMERID  VARCHAR(32), --  中文测试表                              \n";
            sql += "    CUSTOMERNAME  VARCHAR(80), --  中文测试表                            \n";
            sql += "    BUSINESSTYPE  VARCHAR(18), --  中文测试表                            \n";
            sql += "    PUTOUTDATE  VARCHAR(10), --  中文测试表                               \n";
            sql += "    MATURITYDATE  VARCHAR(10), --  中文测试表                             \n";
            sql += "    CERTID  VARCHAR(40), --  中文测试表                                  \n";
            sql += "    INTEBASEADD  DECIMAL(24, 6), --  中文测试表                        \n";
            sql += "    OLDINTEBASE  DECIMAL(24, 6), --  中文测试表                         \n";
            sql += "    PAYBANKAMT  DECIMAL(20, 2),                                    \n";
            sql += "    PAYCOMPANYAMT  DECIMAL(20, 2),                                 \n";
            sql += "    ACTUALPAYAMTTYPE  VARCHAR(30), --  中文测试表                    \n";
            sql += "    BILLKIND  VARCHAR(30),                                         \n";
            sql += "    ADVANCEPAYMETHOD  VARCHAR(32), --  中文测试表                      \n";
            sql += "    SYSTOLIC  VARCHAR(10), --  中文测试表                                \n";
            sql += "    PROFITAMT  DECIMAL(26, 6), --  中文测试表                          \n";
            sql += "    TRANSCODE  CHAR(4), --  中文测试表                                    \n";
            sql += "    RECORDUSERID  VARCHAR(20), --  中文测试表                             \n";
            sql += "    REPAYMENTTYPE  VARCHAR(8), --  中文测试表                            \n";
            sql += "    REPAYMENTAMT  DECIMAL(22, 2),                                   \n";
            sql += " primary key(SERIALNO) \n";
            sql += ")                             \n";

            dao.execute(sql);
            dao.commit();

            dao.execute("create index " + tableName + "IDX on " + fullName + "(SERIALNO)");
            dao.commit();

            dao.execute("create index " + tableName + "IDX1 on " + fullName + "(OBJECTNO,TRANSCODE)");
            dao.commit();

            dao.execute("create index " + tableName + "IDX2 on " + fullName + "(OBJECTNO,PAYFEEAMT)");
            dao.commit();

            table = dao.getTable(catalog, schema, tableName);
            List<DatabaseIndex> indexs = table.getIndexs();
            List<DatabaseIndex> pks = table.getPrimaryIndexs();

            assertEquals(indexs.size(), 2);
            assertEquals(pks.size(), 1);

            DatabaseTableColumn col = table.getColumns().getColumn("PAYAMT");
            if (col == null) {
                throw new NullPointerException();
            }

            assertEquals(col.getName(), "PAYAMT");
            assertEquals(col.getPosition(), 7);

            col = table.getColumns().getColumn(7);
            if (col == null) {
                throw new NullPointerException();
            }

            DatabaseTableDDL ddl = dao.toDDL(table);
            System.out.println(ddl.getTable());

            for (String str : ddl.getPrimaryKey()) {
                System.out.println(str);
            }

            for (String str : ddl.getIndex()) {
                System.out.println(str);
            }

            for (String str : ddl.getComment()) {
                System.out.println(str);
            }

            dao.commit();
        } catch (Throwable e) {
            e.printStackTrace();
            dao.rollback();
        } finally {
            dao.close();
        }
    }
}
