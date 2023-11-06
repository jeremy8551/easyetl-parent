package icu.etl.database.db2;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import icu.etl.database.DatabaseDialect;
import icu.etl.database.Jdbc;
import icu.etl.database.WithDBRule;
import icu.etl.database.pool.SimpleDatasource;
import icu.etl.util.TimeWatch;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.fail;

public class DB2TerminateConnectionTest {

    @Rule
    public WithDBRule rule = new WithDBRule();

    /** 数据库连接 */
    private DataSource dataSource;

    @Before
    public void setUp() {
        this.dataSource = new SimpleDatasource(rule.getContext(), rule.getProperties());
    }

    @After
    public void setDown() {
        Jdbc.closeDataSource(this.dataSource);
    }

    @Test
    public void test() throws SQLException {
        Connection conn = this.dataSource.getConnection();
        try {
            DatabaseDialect dialect = rule.getContext().getBean(DatabaseDialect.class, conn);

            TestThread thread = new TestThread(dialect, conn);
            thread.start();

            TimeWatch watch = new TimeWatch();
            while (watch.useSeconds() < 5 || thread.isAlive()) {
            }

            Assert.assertFalse(thread.isError());

            try {
                conn.commit();
                fail();
            } catch (Throwable e) {
                Assert.assertTrue(true);
            }
        } catch (Exception e) {
            try {
                conn.rollback();
                fail();
            } catch (Throwable e1) {
                Assert.assertTrue(true);
            }
        } finally {
            try {
                conn.close();
                Assert.fail();
            } catch (Throwable e2) {
                Assert.assertTrue(true);
                System.out.println("数据库连接中断测试成功!");
            }
        }
    }

}

