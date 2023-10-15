package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icu.etl.util.Ensure;
import icu.etl.util.StringUtils;
import org.junit.Test;

public class DeclareHandlerCommandTest {

    @Test
    public void test() {
        String str = "declare continue handler for exception begin .. end";
        Pattern compile = Pattern.compile(DeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

    @Test
    public void test1() {
        String str = " declare continue handler for exception begin .. end";
        Pattern compile = Pattern.compile(DeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

    @Test
    public void test11() {
        String str = "declare  continue   handler   for  exception   begin .   .   end  ";
        Pattern compile = Pattern.compile(DeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

    @Test
    public void test111() {
        String str = "declare continue handler for exception begin .. end";
        Pattern compile = Pattern.compile(DeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

    @Test
    public void test1111() {
        String str = "declare global continue handler for exitcode == 3 begin .. end";
        Pattern compile = Pattern.compile(DeclareHandlerCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

    @Test
    public void test11111() {
        String str = "declare global continue handler for exitcode == 3 begin .. end";
        Matcher matcher = StringUtils.compile(str, DeclareHandlerCommandCompiler.REGEX);
        Ensure.isTrue("global ".equals(matcher.group(1)), matcher.group(2));
        Ensure.isTrue("continue".equals(matcher.group(2)), matcher.group(1));
        Ensure.isTrue("exitcode == 3".equals(matcher.group(3)), matcher.group(3));
    }

    @Test
    public void test111111() {
        String str = "declare continue handler for exitcode == 3 begin .. end";
        Matcher matcher = StringUtils.compile(str, DeclareHandlerCommandCompiler.REGEX);
        Ensure.isTrue("".equals(matcher.group(1)), matcher.group(2));
        Ensure.isTrue("continue".equals(matcher.group(2)), matcher.group(1));
        Ensure.isTrue("exitcode == 3".equals(matcher.group(3)), matcher.group(3));
    }

    @Test
    public void test1111112() {
        String str = "declare continue handler for exception begin\necho deal exception ${exception}\nend";
        Matcher matcher = StringUtils.compile(str, DeclareHandlerCommandCompiler.REGEX);
        Ensure.isTrue("".equals(matcher.group(1)), matcher.group(2));
        Ensure.isTrue("continue".equals(matcher.group(2)), matcher.group(1));
        Ensure.isTrue("exception".equals(matcher.group(3)), matcher.group(3));
    }

}
