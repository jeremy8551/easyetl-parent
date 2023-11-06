package icu.etl.expression;

import java.io.CharArrayReader;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 测试 shell 命令表达式是否正确
 */
public class CommandExpressionTest {

    @Test
    public void test() {
        ScriptReader analysis = new ScriptReader(new CharArrayReader("".toCharArray()));

        CommandExpression p = new CommandExpression(analysis, "echo -i: -e -b -c {0-1|4}", "echo -i a -e b c -b d -c e");
        assertEquals("echo", p.getName());
        assertTrue(p.containsOption("-i"));
        assertTrue(p.containsOption("-e"));
        assertEquals("a", p.getOptionValue("-i"));
        assertNull(p.getOptionValue("-e"));
        List<String> list = p.getParameters();
        assertTrue(list.size() == 4 && "b".equals(list.get(0)) && "c".equals(list.get(1)));

        try {
            new CommandExpression(analysis, "echo -i: -e -b -c", "echo -i a -e b c -b d -c e -d");
            fail();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            assertTrue(true);
        }

        p = new CommandExpression(analysis, "-tvf -nme: --prefix: --o  --you:", "tar -vf -t parameter -n filename -m t1 --prefix ab --o  --you hello p2 ");
        assertEquals("tar", p.getName());
        assertTrue(p.containsOption("-v"));
        assertTrue(p.containsOption("-f"));
        assertTrue(p.containsOption("-t"));
        assertTrue(p.containsOption("-t", "-v"));
        assertTrue(p.containsOption("-t", "-v", "-f"));
        assertTrue(p.containsOption("-o"));
        assertEquals("filename", p.getOptionValue("-n"));
        assertEquals("t1", p.getOptionValue("-m"));
        assertEquals("ab", p.getOptionValue("-prefix"));
        assertEquals("hello", p.getOptionValue("-you"));

        assertEquals(2, p.getParameters().size());
        assertEquals("parameter", p.getParameters().get(0));
        assertEquals("p2", p.getParameters().get(1));
    }

    @Test
    public void test1() {
        ScriptReader analysis = new ScriptReader(new CharArrayReader("".toCharArray()));
        LoginExpression p = new LoginExpression(analysis, "ssh user@127.0.0.1:22?password=passwd&alive=true&d=");
        assertEquals("ssh", p.getName());
        assertEquals("user", p.getLoginUsername());
        assertEquals("passwd", p.getLoginPassword());
        assertEquals("127.0.0.1", p.getLoginHost());
        assertEquals("22", p.getLoginPort());
        assertEquals("true", p.getAttribute("alive"));
        assertEquals("", p.getAttribute("d"));
        assertEquals("", p.getAttribute("d"));
    }

    @Test
    public void test11() {
        ScriptReader analysis = new ScriptReader(new CharArrayReader("".toCharArray()));
        LoginExpression p = new LoginExpression(analysis, "ssh us@er@127.0.0.1:22?password=pass@wd&alive=true@&d=&c=@");
        assertEquals("ssh", p.getName());
        assertEquals("us@er", p.getLoginUsername());
        assertEquals("pass@wd", p.getLoginPassword());
        assertEquals("127.0.0.1", p.getLoginHost());
        assertEquals("22", p.getLoginPort());
        assertEquals("true@", p.getAttribute("alive"));
        assertEquals("", p.getAttribute("d"));
        assertEquals("@", p.getAttribute("c"));
    }

    @Test
    public void test2() {
        ScriptReader analysis = new ScriptReader(new CharArrayReader("".toCharArray()));
        CommandExpression p = new CommandExpression(analysis, "!isfile -i: -e -b -c {4}", "!isfile -i a -e b c -b d -c e");
        assertEquals("isfile", p.getName());
        assertTrue(p.isReverse());
        assertTrue(p.containsOption("-i"));
        assertTrue(p.containsOption("-e"));
        assertEquals("a", p.getOptionValue("-i"));
        assertNull(p.getOptionValue("-e"));
        List<String> list = p.getParameters();
        assertTrue(list.size() == 4 && "b".equals(list.get(0)) && "c".equals(list.get(1)));
    }

    @Test
    public void test3() {
        ScriptReader analysis = new ScriptReader(new CharArrayReader("".toCharArray()));
        CommandExpression p = new CommandExpression(analysis, "!isfile -i: -e -b -c {4}", "isfile -i a -e b c -b d -c e");
        assertEquals("isfile", p.getName());
        assertFalse(p.isReverse());
        assertTrue(p.containsOption("-i"));
        assertTrue(p.containsOption("-e"));
        assertEquals("a", p.getOptionValue("-i"));
        assertNull(p.getOptionValue("-e"));
        List<String> list = p.getParameters();
        assertTrue(list.size() == 4 && "b".equals(list.get(0)) && "c".equals(list.get(1)));
    }

    @Test
    public void test4() {
        ScriptReader analysis = new ScriptReader(new CharArrayReader("".toCharArray()));
        CommandExpression p = new CommandExpression(analysis, "!test [-t:|-s|-d:date] {0}", "test -t tp");
        assertEquals("test", p.getName());
        assertFalse(p.isReverse());
        assertEquals("tp", p.getOptionValue("-t"));

        p = new CommandExpression(analysis, "!test [-t:|-s|-d:date] {0}", "test -d 2020-01-01");
        assertEquals("test", p.getName());
        assertFalse(p.isReverse());
        assertEquals("2020-01-01", p.getOptionValue("-d"));

        p = new CommandExpression(analysis, "!test [-t:|-s|-d:date] {0}", "test -s");
        assertEquals("test", p.getName());
        assertFalse(p.isReverse());
        assertTrue(p.containsOption("-s"));

        p = new CommandExpression(analysis, "!test (-t:|-s|-d:date) {0}", "test -s");
        assertEquals("test", p.getName());
        assertFalse(p.isReverse());
        assertTrue(p.containsOption("-s"));

        try {
            new CommandExpression(analysis, "!test (-t:|-s|-d:date) {0}", "test");
            fail();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            assertTrue(true);
        }

        p = new CommandExpression(analysis, "!test [-t:] [-s] [-d:date] {0}", "test -s -t tv -d 20200101");
        assertEquals("test", p.getName());
        assertFalse(p.isReverse());
        assertTrue(p.containsOption("-s"));
        assertTrue(p.containsOption("-t"));
        assertTrue(p.containsOption("-d"));
        assertEquals("20200101", p.getOptionValue("-d"));
        assertEquals("tv", p.getOptionValue("-t"));
        assertNull(p.getOptionValue("-s"));

        try {
            new CommandExpression(analysis, "!test [-t:|-s|-d:date] {0}", "test -t tp -s");
            fail();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            assertTrue(true);
        }
    }

    @Test
    public void test5() {
        ScriptReader analysis = new ScriptReader(new CharArrayReader("".toCharArray()));
        CommandExpression p = new CommandExpression(analysis, "!isfile --prefix: -if ", "isfile --prefix=test value -i");
        assertEquals("isfile", p.getName());
        assertFalse(p.isReverse());
        assertTrue(p.containsOption("-i"));
        assertEquals("test", p.getOptionValue("-prefix"));
        assertEquals(1, p.getParameters().size()); // 参数个数只能是1
        assertEquals("value", p.getParameter());
    }

}
