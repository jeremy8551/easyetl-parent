package icu.etl.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.log.LogLevel;
import icu.etl.util.Dates;
import icu.etl.util.IO;
import org.junit.Assert;
import org.junit.Test;

/**
 * 测试慢速并发任务
 */
public class SlowTest {
    private final static Log log = LogFactory.getLog(SlowTest.class);

    @Test
    public void test1() {
        LogFactory.getContext().updateLevel("*", LogLevel.DEBUG);
        ThreadSourceImpl source = new ThreadSourceImpl();
        EasyJobService service = source.getJobService(2);

        List<EasyJob> list = new ArrayList<EasyJob>();
        for (int i = 0; i < 10; i++) {
            list.add(new Task(i + 1));
        }

        service.executeForce(new EasyJobReaderImpl(list));
        IO.close(source);

        Assert.assertEquals(0, service.getAliveJob());
        Assert.assertEquals(0, service.getErrorJob());
        Assert.assertEquals(10, service.getStartJob());
    }

    @Test
    public void test2() {
        LogFactory.getContext().updateLevel("*", LogLevel.DEBUG);
        ThreadSource source = new ThreadSourceImpl();
        EasyJobService service = source.getJobService(2);

        List<EasyJob> list = new ArrayList<EasyJob>();
        for (int i = 0; i < 10; i++) {
            if (i == 3) {
                list.add(new TaskThrowable(i + 1));
            } else {
                list.add(new Task(i + 1));
            }
        }

        EasyJobWriterImpl out = new EasyJobWriterImpl();
        service.execute(new EasyJobReaderImpl(list), out);
        source.close();

        Assert.assertEquals(0, service.getAliveJob());
        Assert.assertEquals(1, service.getErrorJob());
        Assert.assertEquals(10, service.getStartJob());
        Assert.assertEquals(1, out.getMessages().size());
        log.info(out.getMessages().get(0), out.getThrowables().get(0));
    }

    private static class TaskThrowable extends Task {

        public TaskThrowable(int n) {
            super(n);
        }

        public int execute() throws Exception {
            Random random = new Random();

            for (int i = 0; i < 2; i++) {
                int v = random.nextInt(10);
                if (v == 0) {
                    v = 1;
                }

                log.info(this.getName() + " " + " sleep " + v + " second!");
                Dates.sleep(v * 1000);
            }

            throw new RuntimeException("测试抛出异常");
        }
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
            Random random = new Random();

            for (int i = 0; i < 2; i++) {
                int v = random.nextInt(10);
                if (v == 0) {
                    v = 1;
                }

                log.info(this.getName() + " " + " sleep " + v + " second!");
                Dates.sleep(v * 1000);
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
