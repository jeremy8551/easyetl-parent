package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class UndeclareStatementCommandTest {

    @Test
    public void test() {
        String str = "undeclare name Statement;";
        Pattern compile = Pattern.compile(UndeclareStatementCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertEquals("name", matcher.group(1));
    }

    @Test
    public void test2() {
        String str = "undeclare name Statement  ;";
        Pattern compile = Pattern.compile(UndeclareStatementCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertEquals("name", matcher.group(1));
    }

    @Test
    public void test3() {
        String str = "undeclare name Statement;";
        Pattern compile = Pattern.compile(UndeclareStatementCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertEquals("name", matcher.group(1));
    }

    @Test
    public void test4() {
        String str = "undeclare name Statement ; ";
        Pattern compile = Pattern.compile(UndeclareStatementCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertEquals("name", matcher.group(1));
    }

    @Test
    public void test5() {
        String str = "undeclare nonamecur Statement 1>/var/folders/px/cmxpxt69321_r578dgv5lg2r0000gn/T/testerrlog.log 2> /var/folders/px/cmxpxt69321_r578dgv5lg2r0000gn/T/testerrlog.err";
        Pattern compile = Pattern.compile(UndeclareStatementCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertEquals("nonamecur", matcher.group(1));
    }

}
