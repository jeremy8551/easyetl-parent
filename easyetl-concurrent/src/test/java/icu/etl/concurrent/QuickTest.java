package icu.etl.concurrent;

import java.util.ArrayList;
import java.util.List;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.log.LogLevel;
import icu.etl.util.IO;
import org.junit.Assert;
import org.junit.Test;

/**
 * 测试快速并发任务
 */
public class QuickTest {
    private final static Log log = LogFactory.getLog(QuickTest.class);

    /**
     * 因为涉及到多线程计算，所以重复测试30次，防止出现并发问题
     */
    @Test
    public void test() throws Exception {
        for (int i = 0; i < 30; i++) {
            this.run();
            log.info("");
            log.info("");
            log.info("");
        }
    }

    public void run() throws Exception {
        LogFactory.getContext().updateLevel("*", LogLevel.DEBUG);
        ThreadSourceImpl source = new ThreadSourceImpl();
        EasyJobService service = source.getJobService(5);

        int size = 70;
        List<EasyJob> list = new ArrayList<EasyJob>();
        for (int i = 0; i < size; i++) {
            list.add(new Task(i + 1));
        }

        service.execute(new EasyJobReaderImpl(list));
        IO.close(source);

        Assert.assertEquals(0, service.getAliveJob());
        Assert.assertEquals(0, service.getErrorJob());
        Assert.assertEquals(size, service.getStartJob());
    }

    private static class Task implements EasyJob {
        protected int n;

        public Task(int n) {
            this.n = n;
        }

        public String getName() {
            return "JOB" + this.n;
        }

        public int execute() throws Exception {

            for (int i = 0; i < 2000000; i++) {
                i += 3;
            }

            log.info(this.getName() + " " + " over!");
            return 0;
        }

        public boolean isTerminate() {
            return false;
        }

        public void terminate() {
        }
    }

}
