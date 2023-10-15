package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icu.etl.util.Ensure;
import org.junit.Test;

public class UndeclareStatementCommandTest {

    @Test
    public void test() {
        String str = "undeclare name Statement;";
        Pattern compile = Pattern.compile(UndeclareStatementCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("name"), str);
    }

    @Test
    public void test2() {
        String str = "undeclare name Statement  ;";
        Pattern compile = Pattern.compile(UndeclareStatementCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("name"), str);
    }

    @Test
    public void test1() {
        String str = "undeclare name Statement;";
        Pattern compile = Pattern.compile(UndeclareStatementCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("name"), str);
    }

    @Test
    public void test11() {
        String str = "undeclare name Statement ; ";
        Pattern compile = Pattern.compile(UndeclareStatementCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("name"), str);
    }

    @Test
    public void test111() {
        String str = "undeclare nonamecur Statement 1>/var/folders/px/cmxpxt69321_r578dgv5lg2r0000gn/T/testerrlog.log 2> /var/folders/px/cmxpxt69321_r578dgv5lg2r0000gn/T/testerrlog.err";
        Pattern compile = Pattern.compile(UndeclareStatementCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("nonamecur"), str);
    }

}
