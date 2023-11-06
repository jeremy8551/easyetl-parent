package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class UndeclareSSHClientCommandTest {

    @Test
    public void test() {
        String str = "undeclare name ssh client";
        Pattern compile = Pattern.compile(UndeclareSSHCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void test1() {
        String str = "undeclare name ssh tunnel";
        Pattern compile = Pattern.compile(UndeclareSSHCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void test11() {
        String str = "undeclare name ssh tunnel;";
        Pattern compile = Pattern.compile(UndeclareSSHCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void test1111() {
        String str = "undeclare name ssh tunnel ; ";
        Pattern compile = Pattern.compile(UndeclareSSHCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void test11111() {
        String str = "undeclare   name   ssh   tunnel ;";
        Pattern compile = Pattern.compile(UndeclareSSHCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

}
