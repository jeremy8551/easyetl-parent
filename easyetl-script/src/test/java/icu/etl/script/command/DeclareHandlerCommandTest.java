package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class DeclareHandlerCommandTest {

    @Test
    public void test() {
        String str = "declare continue handler for exception begin .. end";
        Pattern compile = Pattern.compile(DeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void test1() {
        String str = " declare continue handler for exception begin .. end";
        Pattern compile = Pattern.compile(DeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void test11() {
        String str = "declare  continue   handler   for  exception   begin .   .   end  ";
        Pattern compile = Pattern.compile(DeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void test111() {
        String str = "declare continue handler for exception begin .. end";
        Pattern compile = Pattern.compile(DeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void test1111() {
        String str = "declare global continue handler for exitcode == 3 begin .. end";
        Pattern compile = Pattern.compile(DeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());
    }

    @Test
    public void test11111() {
        String str = "declare global continue handler for exitcode == 3 begin .. end";
        Matcher matcher = StringUtils.compile(str, DeclareHandlerCommandCompiler.REGEX);
        Assert.assertEquals("global ", matcher.group(1));
        Assert.assertEquals("continue", matcher.group(2));
        Assert.assertEquals("exitcode == 3", matcher.group(3));
    }

    @Test
    public void test111111() {
        String str = "declare continue handler for exitcode == 3 begin .. end";
        Matcher matcher = StringUtils.compile(str, DeclareHandlerCommandCompiler.REGEX);
        Assert.assertEquals("", matcher.group(1));
        Assert.assertEquals("continue", matcher.group(2));
        Assert.assertEquals("exitcode == 3", matcher.group(3));
    }

    @Test
    public void test1111112() {
        String str = "declare continue handler for exception begin\necho deal exception ${exception}\nend";
        Matcher matcher = StringUtils.compile(str, DeclareHandlerCommandCompiler.REGEX);
        Assert.assertEquals("", matcher.group(1));
        Assert.assertEquals("continue", matcher.group(2));
        Assert.assertEquals("exception", matcher.group(3));
    }

}
