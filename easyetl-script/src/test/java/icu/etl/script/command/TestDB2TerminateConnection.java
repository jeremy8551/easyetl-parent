package icu.etl.script.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import icu.etl.TestEnv;
import icu.etl.database.DatabaseDialect;
import icu.etl.ioc.BeanFactory;
import icu.etl.time.Timer;
import icu.etl.util.TimeWatch;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestDB2TerminateConnection {

    @Test
    public void test() throws SQLException {
        Connection conn = TestEnv.getConnection();
        try {
            DatabaseDialect dialect = BeanFactory.get(DatabaseDialect.class, conn);

//			System.out.println(conn);
//			com.ibm.db2.jcc.t4.b c = (com.ibm.db2.jcc.t4.b) conn;
//			DB2SystemMonitor db2SystemMonitor = c.getDB2SystemMonitor();
//			
//			System.out.println("application id: " + c.getDB2Correlator());
//			System.out.println();
//			System.out.println("db2SystemMonitor: ");
//			ClassUtils.printGetFunctionValue(db2SystemMonitor);
//			System.out.println();
//			System.out.println();

//			System.out.println(Jdbc.toString(conn));
//			System.out.println(Objects.toString(conn, ""));
//			Properties databaseMetaData = 
//			dialect.getDatabaseMetaData(conn, config.getProperty("admin"), config.getProperty("adminPw"));
//			System.out.println(StringUtils.toString(databaseMetaData).replace(',', '\n'));
//			Thread t = new Thread(new Runnable() {
//				public void run() {
//					try {
//						Thread.sleep(2 * 1000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					
//					try {
//						Properties p = dialect.getDatabaseConnectionProperties(conn);
//						p.put(Jdbc.username, config.getProperty("admin"));
//						p.put(Jdbc.password, config.getProperty("adminPw"));
//						
//						if (!dialect.terminateConnection(conn, p)) {
//							System.out.println("关闭数据库连接失败");
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			});

            TestThread thread = new TestThread(dialect, conn);
            thread.start();

            TimeWatch watch = new TimeWatch();
            while (watch.useSeconds() < 5 || thread.isAlive()) {
            }

            assertTrue(!thread.isError());

            try {
                conn.commit();
                assertTrue(false);
            } catch (Throwable e) {
                assertTrue(true);
            }
        } catch (Exception e) {
            try {
                conn.rollback();
                assertTrue(false);
            } catch (Throwable e1) {
                assertTrue(true);
            }
        } finally {
            try {
                conn.close();
                assertTrue(false);
            } catch (Throwable e2) {
                assertTrue(true);
                System.out.println("数据库连接中断测试成功!");
            }
        }
    }

}

class TestThread extends Thread {

    private DatabaseDialect dialect;
    private Connection conn;
    private boolean error;
    private Properties attrs;

    public TestThread(DatabaseDialect dialect, Connection conn) {
        super();
        this.dialect = dialect;
        this.conn = conn;
        this.error = false;
        this.attrs = this.dialect.getAttributes(this.conn);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void run() {
        Timer.sleep(2 * 1000);

        try {
            if (!this.dialect.terminate(this.conn, this.attrs)) {
                System.err.println("终止数据库连接失败!");
                this.error = true;
            } else {
                this.error = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isError() {
        return error;
    }

}