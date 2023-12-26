package icu.etl.expression;

import java.util.List;

import org.junit.Assert;
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
        TestAnalysisImpl analysis = new TestAnalysisImpl();

        CommandExpression expression = new CommandExpression(analysis, "echo -i: -e -b -c {0-1|4}", "echo -i a -e b c -b d -c e");
        assertEquals("echo", expression.getName());
        assertTrue(expression.containsOption("-i"));
        assertTrue(expression.containsOption("-e"));
        assertEquals("a", expression.getOptionValue("-i"));
        assertNull(expression.getOptionValue("-e"));
        List<String> list = expression.getParameters();
        assertTrue(list.size() == 4 && "b".equals(list.get(0)) && "c".equals(list.get(1)));

        try {
            new CommandExpression(analysis, "echo -i: -e -b -c", "echo -i a -e b c -b d -c e -d");
            fail();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            assertTrue(true);
        }

        expression = new CommandExpression(analysis, "-tvf -nme: --prefix: --o  --you:", "tar -vf -t parameter -n filename -m t1 --prefix ab --o  --you hello p2 ");
        assertEquals("tar", expression.getName());
        assertTrue(expression.containsOption("-v"));
        assertTrue(expression.containsOption("-f"));
        assertTrue(expression.containsOption("-t"));
        assertTrue(expression.containsOption("-t", "-v"));
        assertTrue(expression.containsOption("-t", "-v", "-f"));
        assertTrue(expression.containsOption("-o"));
        assertEquals("filename", expression.getOptionValue("-n"));
        assertEquals("t1", expression.getOptionValue("-m"));
        assertEquals("ab", expression.getOptionValue("-prefix"));
        assertEquals("hello", expression.getOptionValue("-you"));

        assertEquals(2, expression.getParameters().size());
        assertEquals("parameter", expression.getParameters().get(0));
        assertEquals("p2", expression.getParameters().get(1));
    }

    @Test
    public void test1() {
        TestAnalysisImpl analysis = new TestAnalysisImpl();
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
    public void test2() {
        TestAnalysisImpl analysis = new TestAnalysisImpl();
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
        TestAnalysisImpl analysis = new TestAnalysisImpl();
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
        TestAnalysisImpl analysis = new TestAnalysisImpl();
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
        TestAnalysisImpl analysis = new TestAnalysisImpl();
        CommandExpression p = new CommandExpression(analysis, "!isfile --prefix: -if ", "isfile --prefix=test value -i");
        assertEquals("isfile", p.getName());
        assertFalse(p.isReverse());
        assertTrue(p.containsOption("-i"));
        assertEquals("test", p.getOptionValue("-prefix"));
        assertEquals(1, p.getParameters().size()); // 参数个数只能是1
        assertEquals("value", p.getParameter());
    }

    @Test
    public void test6() {
        TestAnalysisImpl analysis = new TestAnalysisImpl();
        String pattern = "!isfile --prefix: -if ";
        String command = "isfile --prefix=  -i";
        CommandExpression p = new CommandExpression(analysis, pattern, command);
        assertEquals("isfile", p.getName());
        assertFalse(p.isReverse());
        assertTrue(p.containsOption("-i"));
        assertNull(p.getOptionValue("-prefix"));
        assertEquals(0, p.getParameters().size()); // 参数个数只能是1
        Assert.assertEquals(2, p.getOptionNames().length);
        Assert.assertEquals(0, p.getParameterSize());
        Assert.assertEquals(pattern, p.getPattern());
        Assert.assertEquals(command, p.toString());
    }

    @Test
    public void test7() {
        TestAnalysisImpl analysis = new TestAnalysisImpl();
        CommandExpression p = new CommandExpression(analysis, "!isfile --prefix: -if {0-1} ", "isfile --prefix=test -i   this is a test world!  ");
        assertEquals("isfile", p.getName());
        assertFalse(p.isReverse());
        assertTrue(p.containsOption("-i"));
        assertFalse(p.containsOption("-d"));
        assertEquals("test", p.getOptionValue("-prefix"));
        assertEquals(1, p.getParameters().size()); // 参数个数只能是1
        assertEquals("this is a test world!", p.getParameter());
    }

    @Test
    public void test8() {
        TestAnalysisImpl analysis = new TestAnalysisImpl();
        CommandExpression p = new CommandExpression(analysis, "!isfile --prefix: -if ", "isfile --prefix");
        assertEquals("isfile", p.getName());
        assertFalse(p.isReverse());
        assertFalse(p.containsOption("-i"));
        assertNull(p.getOptionValue("-prefix"));
        assertEquals(0, p.getParameters().size()); // 参数个数只能是1
    }

    // 测试参数中包含选项
    @Test
    public void test9() {
        TestAnalysisImpl analysis = new TestAnalysisImpl();
        try {
            new CommandExpression(analysis, "!isfile --prefix: -if {0-1} ", "isfile --prefix=test -i this is a -f test world!  ");
            Assert.fail();
        } catch (ExpressionException e) {
            Assert.assertTrue(e.getLocalizedMessage().contains("-f"));
            e.printStackTrace();
        }
    }

    // 测试参数中包含选项
    @Test
    public void test10() {
        TestAnalysisImpl analysis = new TestAnalysisImpl();
        CommandExpression p = new CommandExpression(analysis, "set [-E|-e] {0-1} ", "set name=`wc -l xxx | grep test`");
        assertEquals("set", p.getName());
        assertFalse(p.isReverse());
        assertFalse(p.containsOption("-e"));
        assertEquals(1, p.getParameters().size()); // 参数个数只能是1
    }

    @Test
    public void test11() {
        TestAnalysisImpl analysis = new TestAnalysisImpl();
        CommandExpression p = new CommandExpression(analysis, "date -d:", "date -d '2020-12-13 03:14:56' 'H'");
        assertEquals("date", p.getName());
        assertFalse(p.isReverse());
        Assert.assertTrue(p.containsOption("-d"));
        Assert.assertEquals("'2020-12-13 03:14:56'", p.getOptionValue("-d"));
        assertEquals(1, p.getParameters().size()); // 参数个数只能是1
        Assert.assertEquals("'H'", p.getParameter());
    }

    @Test
    public void test12() {
        TestAnalysisImpl analysis = new TestAnalysisImpl();
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

}
