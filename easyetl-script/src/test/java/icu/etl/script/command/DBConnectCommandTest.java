package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class DBConnectCommandTest {

    @Test
    public void test() {
        String str = "db connect to name";
        Pattern compile = Pattern.compile(DBConnectCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertEquals("to name", matcher.group(1));
    }

    @Test
    public void test0() {
        String str = "db connect to test0001";
        Pattern compile = Pattern.compile(DBConnectCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertEquals("to test0001", matcher.group(1));
    }

    @Test
    public void test1() {
        String str = "db connect reset";
        Pattern compile = Pattern.compile(DBConnectCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertEquals("reset", matcher.group(1));
    }

    @Test
    public void test11() {
        String str = " db connect reset";
        Pattern compile = Pattern.compile(DBConnectCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertEquals("reset", matcher.group(1));
    }

    @Test
    public void test111() {
        String str = "db connect to uddb11;";
        Pattern compile = Pattern.compile(DBConnectCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertEquals("to uddb11", matcher.group(1));
    }

    @Test
    public void test1112() {
        String str = "db connect reset;";
        Pattern compile = Pattern.compile(DBConnectCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertEquals("reset", matcher.group(1));
    }

    @Test
    public void test11121() {
        String str = "db connect reset ";
        Pattern compile = Pattern.compile(DBConnectCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertEquals("reset ", matcher.group(1));
    }

}
