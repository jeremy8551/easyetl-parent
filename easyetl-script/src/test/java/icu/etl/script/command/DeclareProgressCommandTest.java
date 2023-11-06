package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class DeclareProgressCommandTest {

    @Test
    public void test() {
        String str = "declare name progress use step print ' this is text!!  ' total 999 times;";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertTrue(matcher.group(1).trim().equals("name"));
        Assert.assertTrue(matcher.group(2).equals("step"));
        Assert.assertTrue(matcher.group(3).equals("' this is text!!  '"));
        Assert.assertTrue(matcher.group(4).equals("999"));
    }

    @Test
    public void test1() {
        String str = "declare name progress use step print ' this is text!!  ' total 999 times ";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertTrue(matcher.group(1).trim().equals("name"));
        Assert.assertTrue(matcher.group(2).equals("step"));
        Assert.assertTrue(matcher.group(3).equals("' this is text!!  '"));
        Assert.assertTrue(matcher.group(4).equals("999"));
    }

    @Test
    public void test11() {
        String str = "declare name progress use step print ' this is text!!  ' total 999 times  ; ";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertTrue(matcher.group(1).trim().equals("name"));
        Assert.assertTrue(matcher.group(2).equals("step"));
        Assert.assertTrue(matcher.group(3).equals("' this is text!!  '"));
        Assert.assertTrue(matcher.group(4).equals("999"));
    }

    @Test
    public void test111() {
        String str = " declare name progress use step print ' this is text!!  ' total 999 times  ; ";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertTrue(matcher.group(1).trim().equals("name"));
        Assert.assertTrue(matcher.group(2).equals("step"));
        Assert.assertTrue(matcher.group(3).equals("' this is text!!  '"));
        Assert.assertTrue(matcher.group(4).equals("999"));
    }

    @Test
    public void test1112() {
        String str = "declare progress use out print \"测试进度输出已执行 ${process}%, 总共${totalRecord}个记录${leftTime}\" total 100000 times";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertTrue(matcher.group(1).equals(""));
        Assert.assertTrue(matcher.group(2).equals("out"));
        Assert.assertTrue(matcher.group(3).equals("\"测试进度输出已执行 ${process}%, 总共${totalRecord}个记录${leftTime}\""));
        Assert.assertTrue(matcher.group(4).equals("100000"));
    }

    @Test
    public void test11121() {
        String str = "declare global progress use out print \"测试进度输出已执行 ${process}%, 总共${totalRecord}个记录${leftTime}\" total 100000 times";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertTrue(matcher.group(1).equals("global "));
        Assert.assertTrue(matcher.group(2).equals("out"));
        Assert.assertTrue(matcher.group(3).equals("\"测试进度输出已执行 ${process}%, 总共${totalRecord}个记录${leftTime}\""));
        Assert.assertTrue(matcher.group(4).equals("100000"));
    }

    @Test
    public void test111213() {
        String str = "declare global test progress use out print \"测试进度输出已执行 ${process}%, 总共${totalRecord}个记录${leftTime}\" total 100000 times";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertTrue(matcher.group(1).equals("global test "));
        Assert.assertTrue(matcher.group(2).equals("out"));
        Assert.assertTrue(matcher.group(3).equals("\"测试进度输出已执行 ${process}%, 总共${totalRecord}个记录${leftTime}\""));
        Assert.assertTrue(matcher.group(4).equals("100000"));
    }

}
