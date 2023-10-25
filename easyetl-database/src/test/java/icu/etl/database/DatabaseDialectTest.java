package icu.etl.database;

import java.sql.SQLException;
import java.util.List;

import icu.etl.database.db2.DB2Dialect;
import icu.etl.database.mysql.MysqlDialect;
import icu.etl.database.oracle.OracleDialect;
import icu.etl.ioc.AnnotationEasyetlContext;
import icu.etl.util.Settings;
import icu.etl.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DatabaseDialectTest {

    @Test
    public void test1() {
        DatabaseDialect d = new DB2Dialect();
        List<DatabaseURL> list = d.parseJdbcUrl("jdbc:db2://130.1.10.103:50001/TESTDB:currentSchema=HYCS;");
        assertTrue(list.size() == 1);
        DatabaseURL u = list.get(0);
        assertEquals(u.getHostname(), "130.1.10.103");
        assertEquals(u.getDatabaseName(), "TESTDB");
        assertEquals(u.getType(), "db2");
        assertEquals(u.getPort(), "50001");
        assertEquals(u.getSchema(), "HYCS");

        List<DatabaseURL> l = d.parseJdbcUrl("jdbc:db2:TESTDB");
        assertTrue(l.size() == 1);
        System.out.println(StringUtils.toString(l.get(0).toProperties()));

        d = new MysqlDialect();
        List<DatabaseURL> list0 = d.parseJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?user=root&password=&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false");
        assertTrue(list0.size() == 1);
        u = list0.get(0);
        assertEquals(u.getHostname(), "127.0.0.1");
        assertEquals(u.getDatabaseName(), "test");
        assertEquals(u.getType(), "mysql");
        assertEquals(u.getPort(), "3306");

        List<DatabaseURL> list1 = d.parseJdbcUrl("jdbc:mysql://127.0.0.1/test?user=root&password=&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false");
        assertTrue(list1.size() == 1);
        u = list1.get(0);
        assertEquals(u.getHostname(), "127.0.0.1");
        assertEquals(u.getDatabaseName(), "test");
        assertEquals(u.getType(), "mysql");
        assertEquals(u.getPort(), "3306");
        System.out.println(StringUtils.toString(u.toProperties()));

        List<DatabaseURL> list2 = d.parseJdbcUrl("jdbc:mysql://127.0.0.2,127.0.0.1/test?user=root&password=&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false");
        assertEquals(list2.size(), 2);
        u = list2.get(0);
        assertEquals(u.getHostname(), "127.0.0.2");
        assertEquals(u.getDatabaseName(), "test");
        assertEquals(u.getType(), "mysql");
        assertEquals(u.getPort(), "3306");
        System.out.println(StringUtils.toString(u.toProperties()));

        u = list2.get(1);
        assertEquals(u.getHostname(), "127.0.0.1");
        assertEquals(u.getDatabaseName(), "test");
        assertEquals(u.getType(), "mysql");
        assertEquals(u.getPort(), "3306");
        System.out.println(StringUtils.toString(list2.get(1).toProperties()));

        d = new OracleDialect();
        List<DatabaseURL> list3 = d.parseJdbcUrl("jdbc:oracle:thin:@130.1.10.104:1521:sid");
        assertEquals(list3.size(), 1);
        u = list3.get(0);
        assertEquals(u.getHostname(), "130.1.10.104");
        assertEquals(u.getDatabaseName(), "sid");
        assertEquals(u.getType(), "oracle");
        assertEquals(u.getPort(), "1521");
        assertEquals(u.getSID(), "sid");
        assertEquals(u.getDriverType(), "thin");
        System.out.println(StringUtils.toString(u.toProperties()));

        List<DatabaseURL> list4 = d.parseJdbcUrl("jdbc:oracle:thin:user/pass@130.1.10.104:1521:sid");
        assertEquals(list4.size(), 1);
        u = list4.get(0);
        assertEquals(u.getHostname(), "130.1.10.104");
        assertEquals(u.getDatabaseName(), "sid");
        assertEquals(u.getType(), "oracle");
        assertEquals(u.getPort(), "1521");
        assertEquals(u.getSID(), "sid");
        assertEquals(u.getDriverType(), "thin");
        assertEquals(u.getUsername(), "user");
        assertEquals(u.getPassword(), "pass");
        System.out.println(StringUtils.toString(u.toProperties()));

        List<DatabaseURL> list5 = d.parseJdbcUrl("jdbc:oracle:thin:@130.1.10.104:1521/sid");
        assertEquals(list5.size(), 1);
        u = list5.get(0);
        assertEquals(u.getHostname(), "130.1.10.104");
        assertEquals(u.getDatabaseName(), "sid");
        assertEquals(u.getType(), "oracle");
        assertEquals(u.getPort(), "1521");
        assertEquals(u.getSID(), "sid");
        assertEquals(u.getDriverType(), "thin");
        assertEquals(u.getUsername(), null);
        assertEquals(u.getPassword(), null);
        System.out.println(StringUtils.toString(u.toProperties()));

        List<DatabaseURL> list6 = d.parseJdbcUrl("jdbc:oracle:thin:user/pass@130.1.10.104:1521/sid");
        assertEquals(list6.size(), 1);
        u = list6.get(0);
        assertEquals(u.getHostname(), "130.1.10.104");
        assertEquals(u.getDatabaseName(), "sid");
        assertEquals(u.getType(), "oracle");
        assertEquals(u.getPort(), "1521");
        assertEquals(u.getSID(), "sid");
        assertEquals(u.getDriverType(), "thin");
        assertEquals(u.getUsername(), "user");
        assertEquals(u.getPassword(), "pass");
        System.out.println(StringUtils.toString(u.toProperties()));

        List<DatabaseURL> list7 = d.parseJdbcUrl("jdbc:oracle:thin:@(description=(address_list= (address=(host=rac1) (protocol=tcp1)(port=1521))(address=(host=rac2)(protocol=tcp2) (port=1522)) (load_balance=yes)(failover=yes))(connect_data=(SERVER=DEDICATED)(service_name= oratest)))");
        assertEquals(list7.size(), 2);
        u = list7.get(0);
        System.out.println(StringUtils.toString(list7.get(0).toProperties()));
        System.out.println(StringUtils.toString(list7.get(1).toProperties()));
        assertEquals(u.getHostname(), "rac1");
        assertEquals(u.getDatabaseName(), null);
        assertEquals(u.getType(), "oracle");
        assertEquals(u.getPort(), "1521");
        assertEquals(u.getSID(), null);
        assertEquals(u.getDriverType(), "thin");
        assertEquals(u.getAttribute("protocol"), "tcp1");
    }

    @Test
    public void test2() throws SQLException {
        AnnotationEasyetlContext context = new AnnotationEasyetlContext();
        String catalog = null;
        String schema = null;
        JdbcDao dao = new JdbcDao(context, TestEnv.getConnection());
        try {
            String tableName = Settings.getGroupID().replace('.', '_') + "_TEST_TEMP".toUpperCase();
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
            sql += "    SERIALNO  VARCHAR(32) not null, --  流水号                        \n";
            sql += "    OBJECTNO  VARCHAR(40), --  对象编号                                \n";
            sql += "    DOCUMENTTYPE  VARCHAR(30), --  单据类型                            \n";
            sql += "    PAYDATE  VARCHAR(10), --  还款日期                                 \n";
            sql += "    ACTUALPAYDATE  VARCHAR(10), --  实际还款日期                         \n";
            sql += "    CURRENCY  VARCHAR(3), --  币种                                   \n";
            sql += "    PAYAMT  DECIMAL(20, 2), --  应还金额                               \n";
            sql += "    ACTUALPAYAMT  DECIMAL(20, 2), --  实还金额                         \n";
            sql += "    DEDUCTACCNO1  VARCHAR(40), --  扣款账号                            \n";
            sql += "    DEDUCTACCNO2  VARCHAR(40), --  扣款账号                            \n";
            sql += "    DEDUCTACCNO  VARCHAR(40), --  扣款账号                             \n";
            sql += "    BILLSTATUS  VARCHAR(10), --  状态                                \n";
            sql += "    RETURNCHANNEL  VARCHAR(20), --  还款渠道                           \n";
            sql += "    OBJECTTYPE  VARCHAR(10), --  对象类型                              \n";
            sql += "    PAYCORPUSAMT  DECIMAL(20, 2), --  应还本金                         \n";
            sql += "    ACTUALPAYCORPUSAMT  DECIMAL(20, 2), --  实还本金                   \n";
            sql += "    PAYINTEAMT  DECIMAL(20, 2), --  应还利息                           \n";
            sql += "    ACTUALPAYINTEAMT  DECIMAL(20, 2), --  实还利息                     \n";
            sql += "    PAYFINEAMT  DECIMAL(20, 2), --  应还罚息                           \n";
            sql += "    ACTUALFINEAMT  DECIMAL(20, 2), --  实还罚息                        \n";
            sql += "    PAYCOMPDINTEAMT  DECIMAL(20, 2), --  应还复利                      \n";
            sql += "    ACTUALCOMPDINTEAMT  DECIMAL(20, 2), --  实还复利                   \n";
            sql += "    PAYFEEAMT  DECIMAL(20, 2), --  应还费用                            \n";
            sql += "    ACTUALFEEAMT  DECIMAL(20, 2), --  实还费用                         \n";
            sql += "    ORGID  VARCHAR(20), --  机构                                     \n";
            sql += "    TBREPAYSERIALNO  VARCHAR(32), --  网银还款指令号                \n";
            sql += "    TBLOANSERIALNO  VARCHAR(32), --  网银借款指令号                 \n";
            sql += "    FEETYPE  VARCHAR(20), --  费用类型                                 \n";
            sql += "    CHECKFLAG  VARCHAR(1), --  金额类型                                \n";
            sql += "    CHECKACCOUNTNO  VARCHAR(32), --  检查账户编号                        \n";
            sql += "    CHECKACCOUNTTYPE  VARCHAR(2), --  检查账户类型                       \n";
            sql += "    DEDUCTACCNONAME  VARCHAR(80), --  还款账户名                        \n";
            sql += "    PREPAYCORPUS  DECIMAL(20, 2), --  提前还款应还本金                     \n";
            sql += "    ACTUALPREPAYCORPUS  DECIMAL(20, 2), --  提前还款实还本金               \n";
            sql += "    PREPAYINTEREST  DECIMAL(20, 2), --  提前还款应还利息                   \n";
            sql += "    ACTUALPREPAYINTEREST  DECIMAL(20, 2), --  提前还款实还利息             \n";
            sql += "    PAYMENTORDER  DECIMAL(20, 2),                                  \n";
            sql += "    EXPIATIONSUM  DECIMAL(20, 2),                                  \n";
            sql += "    IFSEND  VARCHAR(2),                                            \n";
            sql += "    CUSTOMERID  VARCHAR(32), --  客户ID                              \n";
            sql += "    CUSTOMERNAME  VARCHAR(80), --  客户姓名                            \n";
            sql += "    BUSINESSTYPE  VARCHAR(18), --  业务品种                            \n";
            sql += "    PUTOUTDATE  VARCHAR(10), --  发放日                               \n";
            sql += "    MATURITYDATE  VARCHAR(10), --  到期日                             \n";
            sql += "    CERTID  VARCHAR(40), --  证件号码                                  \n";
            sql += "    INTEBASEADD  DECIMAL(24, 6), --  本次追加挂息                        \n";
            sql += "    OLDINTEBASE  DECIMAL(24, 6), --  原挂息金额                         \n";
            sql += "    PAYBANKAMT  DECIMAL(20, 2),                                    \n";
            sql += "    PAYCOMPANYAMT  DECIMAL(20, 2),                                 \n";
            sql += "    ACTUALPAYAMTTYPE  VARCHAR(30), --  提前还款金额类型                    \n";
            sql += "    BILLKIND  VARCHAR(30),                                         \n";
            sql += "    ADVANCEPAYMETHOD  VARCHAR(32), --  提前还款方式                      \n";
            sql += "    SYSTOLIC  VARCHAR(10), --  缩期类型                                \n";
            sql += "    PROFITAMT  DECIMAL(26, 6), --  银行损益金额                          \n";
            sql += "    TRANSCODE  CHAR(4), --  交易码                                    \n";
            sql += "    RECORDUSERID  VARCHAR(20), --  记账人                             \n";
            sql += "    REPAYMENTTYPE  VARCHAR(8), --  代偿标识                            \n";
            sql += "    REPAYMENTAMT  DECIMAL(22, 2),                                   \n";
            sql += " primary key(SERIALNO) \n";
            sql += ")                             \n";

            dao.executeByJdbc(sql);
            dao.commit();

            dao.executeByJdbc("create index " + tableName + "IDX on " + fullName + "(SERIALNO)");
            dao.commit();

            dao.executeByJdbc("create index " + tableName + "IDX1 on " + fullName + "(OBJECTNO,TRANSCODE)");
            dao.commit();

            dao.executeByJdbc("create index " + tableName + "IDX2 on " + fullName + "(OBJECTNO,PAYFEEAMT)");
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
