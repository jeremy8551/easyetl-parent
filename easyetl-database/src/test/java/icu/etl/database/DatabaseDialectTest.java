package icu.etl.database;

import java.util.List;

import icu.etl.database.db2.DB2Dialect;
import icu.etl.database.mysql.MysqlDialect;
import icu.etl.database.oracle.OracleDialect;
import icu.etl.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DatabaseDialectTest {

    @Test
    public void test() {
        DatabaseDialect d = new DB2Dialect();
        List<DatabaseURL> list = d.parseJdbcUrl("jdbc:db2://130.1.10.103:50001/TESTDB:currentSchema=HYCS;");
        assertEquals(1, list.size());
        DatabaseURL u = list.get(0);
        assertEquals(u.getHostname(), "130.1.10.103");
        assertEquals(u.getDatabaseName(), "TESTDB");
        assertEquals(u.getType(), "db2");
        assertEquals(u.getPort(), "50001");
        assertEquals(u.getSchema(), "HYCS");

        List<DatabaseURL> l = d.parseJdbcUrl("jdbc:db2:TESTDB");
        assertEquals(1, l.size());
        System.out.println(StringUtils.toString(l.get(0).toProperties()));

        d = new MysqlDialect();
        List<DatabaseURL> list0 = d.parseJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?user=root&password=&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false");
        assertEquals(1, list0.size());
        u = list0.get(0);
        assertEquals(u.getHostname(), "127.0.0.1");
        assertEquals(u.getDatabaseName(), "test");
        assertEquals(u.getType(), "mysql");
        assertEquals(u.getPort(), "3306");

        List<DatabaseURL> list1 = d.parseJdbcUrl("jdbc:mysql://127.0.0.1/test?user=root&password=&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false");
        assertEquals(1, list1.size());
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
        assertNull(u.getUsername());
        assertNull(u.getPassword());
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
        assertNull(u.getDatabaseName());
        assertEquals(u.getType(), "oracle");
        assertEquals(u.getPort(), "1521");
        assertNull(u.getSID());
        assertEquals(u.getDriverType(), "thin");
        assertEquals(u.getAttribute("protocol"), "tcp1");
    }
}
