package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icu.etl.util.Ensure;
import org.junit.Test;

public class UndeclareCatalogCommandTest {

    @Test
    public void test11() {
        String str = "undeclare name catalog configuration  ";
        Pattern compile = Pattern.compile(UndeclareCatalogCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals(""), str);
        Ensure.isTrue(matcher.group(2).equals("name"), str);
    }

    @Test
    public void test111() {
        String str = "undeclare name catalog configuration";
        Pattern compile = Pattern.compile(UndeclareCatalogCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals(""), str);
        Ensure.isTrue(matcher.group(2).equals("name"), str);
    }

    @Test
    public void test2() {
        String str = "undeclare name catalog configuration;";
        Pattern compile = Pattern.compile(UndeclareCatalogCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals(""), str);
        Ensure.isTrue(matcher.group(2).equals("name"), str);
    }

    @Test
    public void test21() {
        String str = "undeclare name catalog configuration ;";
        Pattern compile = Pattern.compile(UndeclareCatalogCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals(""), str);
        Ensure.isTrue(matcher.group(2).equals("name"), str);
    }

    @Test
    public void test213() {
        String str = "undeclare name catalog configuration  ;   ";
        Pattern compile = Pattern.compile(UndeclareCatalogCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals(""), str);
        Ensure.isTrue(matcher.group(2).equals("name"), str);
    }

    @Test
    public void test212() {
        String str = "undeclare name catalog configuration;  ";
        Pattern compile = Pattern.compile(UndeclareCatalogCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals(""), str);
        Ensure.isTrue(matcher.group(2).equals("name"), str);
    }

}
