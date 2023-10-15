package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icu.etl.util.Ensure;
import org.junit.Test;

public class DeclareProgressCommandTest {

    @Test
    public void test() {
        String str = "declare name progress use step print ' this is text!!  ' total 999 times;";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).trim().equals("name"), str);
        Ensure.isTrue(matcher.group(2).equals("step"), str);
        Ensure.isTrue(matcher.group(3).equals("' this is text!!  '"), str);
        Ensure.isTrue(matcher.group(4).equals("999"), str);
    }

    @Test
    public void test1() {
        String str = "declare name progress use step print ' this is text!!  ' total 999 times ";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).trim().equals("name"), str);
        Ensure.isTrue(matcher.group(2).equals("step"), str);
        Ensure.isTrue(matcher.group(3).equals("' this is text!!  '"), str);
        Ensure.isTrue(matcher.group(4).equals("999"), str);
    }

    @Test
    public void test11() {
        String str = "declare name progress use step print ' this is text!!  ' total 999 times  ; ";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).trim().equals("name"), str);
        Ensure.isTrue(matcher.group(2).equals("step"), str);
        Ensure.isTrue(matcher.group(3).equals("' this is text!!  '"), str);
        Ensure.isTrue(matcher.group(4).equals("999"), str);
    }

    @Test
    public void test111() {
        String str = " declare name progress use step print ' this is text!!  ' total 999 times  ; ";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).trim().equals("name"), matcher.group(1));
        Ensure.isTrue(matcher.group(2).equals("step"), matcher.group(2));
        Ensure.isTrue(matcher.group(3).equals("' this is text!!  '"), matcher.group(3));
        Ensure.isTrue(matcher.group(4).equals("999"), matcher.group(4));
    }

    @Test
    public void test1112() {
        String str = "declare progress use out print \"测试进度输出已执行 ${process}%, 总共${totalRecord}个记录${leftTime}\" total 100000 times";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals(""), str);
        Ensure.isTrue(matcher.group(2).equals("out"), str);
        Ensure.isTrue(matcher.group(3).equals("\"测试进度输出已执行 ${process}%, 总共${totalRecord}个记录${leftTime}\""), str);
        Ensure.isTrue(matcher.group(4).equals("100000"), str);
    }

    @Test
    public void test11121() {
        String str = "declare global progress use out print \"测试进度输出已执行 ${process}%, 总共${totalRecord}个记录${leftTime}\" total 100000 times";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("global "), str);
        Ensure.isTrue(matcher.group(2).equals("out"), str);
        Ensure.isTrue(matcher.group(3).equals("\"测试进度输出已执行 ${process}%, 总共${totalRecord}个记录${leftTime}\""), str);
        Ensure.isTrue(matcher.group(4).equals("100000"), str);
    }

    @Test
    public void test111213() {
        String str = "declare global test progress use out print \"测试进度输出已执行 ${process}%, 总共${totalRecord}个记录${leftTime}\" total 100000 times";
        Pattern compile = Pattern.compile(DeclareProgressCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("global test "), str);
        Ensure.isTrue(matcher.group(2).equals("out"), str);
        Ensure.isTrue(matcher.group(3).equals("\"测试进度输出已执行 ${process}%, 总共${totalRecord}个记录${leftTime}\""), str);
        Ensure.isTrue(matcher.group(4).equals("100000"), str);
    }

}
