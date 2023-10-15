package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icu.etl.util.Ensure;
import org.junit.Test;

public class DeclareSSHClientCommandTest {

    @Test
    public void test() {
        String str = "declare name SSH client for connect to name@host:port?password=str";
        Pattern compile = Pattern.compile(DeclareSSHClientCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("name"), str);
        Ensure.isTrue(matcher.group(2).equals("name@host:port?password=str"), str);
    }

    @Test
    public void test1() {
        String str = "declare name SSH client for connect to name@host:port?password=str;";
        Pattern compile = Pattern.compile(DeclareSSHClientCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("name"), str);
        Ensure.isTrue(matcher.group(2).equals("name@host:port?password=str"), str);
    }

    @Test
    public void test2() {
        String str = "declare name SSH client for connect to name@host:port?password=str ; ";
        Pattern compile = Pattern.compile(DeclareSSHClientCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("name"), str);
        Ensure.isTrue(matcher.group(2).equals("name@host:port?password=str"), str);
    }

}
