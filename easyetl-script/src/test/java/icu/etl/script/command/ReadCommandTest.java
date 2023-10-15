package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icu.etl.util.Ensure;
import org.junit.Test;

public class ReadCommandTest {

    @Test
    public void test() {
        String str = "while read line do ... done < filename";
        Pattern compile = Pattern.compile(ReadCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

    @Test
    public void test1() {
        String str = "while read line2 do ... done<filename";
        Pattern compile = Pattern.compile(ReadCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

    @Test
    public void test2() {
        String str = "while read line1 do ... done < filename;";
        Pattern compile = Pattern.compile(ReadCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

    @Test
    public void test3() {
        String str = "while read line do ... done < filename ;";
        Pattern compile = Pattern.compile(ReadCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);
    }

}
