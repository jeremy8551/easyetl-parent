package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class UndeclareHandleCommandTest {

    @Test
    public void test() {
        String str = "undeclare handler for exitcode != 0";
        Pattern compile = Pattern.compile(UndeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void test1() {
        String str = "undeclare global handler for exitcode != 0";
        Pattern compile = Pattern.compile(UndeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void test2() {
        String str = "undeclare global handler for exception";
        Pattern compile = Pattern.compile(UndeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

}
