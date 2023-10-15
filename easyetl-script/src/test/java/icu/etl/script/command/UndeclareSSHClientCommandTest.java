package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icu.etl.util.Ensure;
import org.junit.Test;

public class UndeclareSSHClientCommandTest {

    @Test
    public void test() {
        String str = "undeclare name ssh client";
        Pattern compile = Pattern.compile(UndeclareSSHCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

    @Test
    public void test1() {
        String str = "undeclare name ssh tunnel";
        Pattern compile = Pattern.compile(UndeclareSSHCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

    @Test
    public void test11() {
        String str = "undeclare name ssh tunnel;";
        Pattern compile = Pattern.compile(UndeclareSSHCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

    @Test
    public void test1111() {
        String str = "undeclare name ssh tunnel ; ";
        Pattern compile = Pattern.compile(UndeclareSSHCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

    @Test
    public void test11111() {
        String str = "undeclare   name   ssh   tunnel ;";
        Pattern compile = Pattern.compile(UndeclareSSHCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

}
