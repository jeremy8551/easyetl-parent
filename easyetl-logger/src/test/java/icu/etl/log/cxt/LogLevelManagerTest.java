package icu.etl.log.cxt;

import java.util.List;

import icu.etl.log.LogLevel;
import org.junit.Assert;
import org.junit.Test;

public class LogLevelManagerTest {

    @Test
    public void test1() {
        LogLevelManager m = new LogLevelManager();
        m.put("", LogLevel.TRACE);
        Assert.assertEquals(LogLevel.TRACE, m.get(""));

        m.put("root", LogLevel.ERROR);
        Assert.assertEquals(LogLevel.ERROR, m.get(""));

        m.put("*", LogLevel.INFO);
        Assert.assertEquals(LogLevel.INFO, m.get(""));
        Assert.assertEquals(LogLevel.INFO, m.get(null));
    }

    @Test
    public void test2() {
        LogLevelManager m = new LogLevelManager();
        m.put("d", LogLevel.TRACE);
        m.put("a", LogLevel.INFO);
        m.put("d.e.t", LogLevel.ERROR);
        m.put("*", LogLevel.INFO);
        m.put("a.b.c", LogLevel.INFO);
        m.put("a.b.c", LogLevel.FATAL);
        m.put("a.b", LogLevel.OFF);
        m.put("d.e", LogLevel.WARN);

        Assert.assertEquals(LogLevel.INFO, m.get("a"));
        List<LogLevelManager.Entry> list = m.getEntrys();
        Assert.assertEquals(6, list.size());

        // 测试排序
        int i = 0;
        Assert.assertEquals("d.e.t", list.get(i++).name);
        Assert.assertEquals("d.e", list.get(i++).name);
        Assert.assertEquals("d", list.get(i++).name);
        Assert.assertEquals("a.b.c", list.get(i++).name);
        Assert.assertEquals("a.b", list.get(i++).name);
        Assert.assertEquals("a", list.get(i++).name);

        // 测试包或类的日志级别
        Assert.assertEquals(LogLevel.WARN, m.get("d.e"));
        Assert.assertEquals(LogLevel.ERROR, m.get("d.e.t"));
        Assert.assertEquals(LogLevel.FATAL, m.get("a.b.c"));
        Assert.assertEquals(LogLevel.INFO, m.get("a"));
        Assert.assertEquals(LogLevel.INFO, m.get(""));
    }

    @Test
    public void test3() {
        LogLevelManager m = new LogLevelManager();

        try {
            m.put(null, LogLevel.TRACE);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        try {
            m.put("a", null);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

}
