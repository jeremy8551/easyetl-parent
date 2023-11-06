package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class DeclareCatalogCommandTest {

    @Test
    public void test() {
        String str = "declare name catalog configuration use driver com.ibm.db2.jcc.DB2Driver url jdbc:db2://130.1.16.26:50000/username username db2inst1 password db2inst1;";
        Pattern compile = Pattern.compile(DeclareCatalogCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertTrue(matcher.group(1).equals("declare"));
        Assert.assertTrue(matcher.group(2).equals(""));
        Assert.assertTrue(matcher.group(3).equals("name"));
        Assert.assertTrue(matcher.group(4).equals("driver com.ibm.db2.jcc.DB2Driver url jdbc:db2://130.1.16.26:50000/username username db2inst1 password db2inst1;"));
    }

    @Test
    public void test1() {
        String str = "declare name catalog configuration use /home/user/config.properties ; ";
        Pattern compile = Pattern.compile(DeclareCatalogCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertTrue(matcher.group(1).equals("declare"));
        Assert.assertTrue(matcher.group(2).equals(""));
        Assert.assertTrue(matcher.group(3).equals("name"));
        Assert.assertTrue(matcher.group(4).equals("/home/user/config.properties ; "));
    }

    @Test
    public void test12() {
        String str = "declare global name catalog configuration use /home/user/config.properties ; ";
        Pattern compile = Pattern.compile(DeclareCatalogCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertTrue(matcher.group(1).equals("declare"));
        Assert.assertTrue(matcher.group(2).equals("global "));
        Assert.assertTrue(matcher.group(3).equals("name"));
        Assert.assertTrue(matcher.group(4).equals("/home/user/config.properties ; "));
    }

    @Test
    public void test121() {
        String str = "declare global uddb11 catalog configuration use driver ${databaseDriverName} url '${databaseUrl}' username ${username} password ${password}";
        Pattern compile = Pattern.compile(DeclareCatalogCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertTrue(matcher.group(1).equals("declare"));
        Assert.assertTrue(matcher.group(2).equals("global "));
        Assert.assertTrue(matcher.group(3).equals("uddb11"));
        Assert.assertTrue(matcher.group(4).equals("driver ${databaseDriverName} url '${databaseUrl}' username ${username} password ${password}"));
    }

    @Test
    public void test1212() {
        String str = "declare global uddb11 catalog configuration use driver com.ibm.db2.jcc.DB2Driver url 'jdbc:db2://130.1.16.26:50000/bhcdb' username db2inst1 password db2inst1";
        Pattern compile = Pattern.compile(DeclareCatalogCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Assert.assertTrue(matcher.find());

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Assert.assertTrue(matcher.group(1).equals("declare"));
        Assert.assertTrue(matcher.group(2).equals("global "));
        Assert.assertTrue(matcher.group(3).equals("uddb11"));
        Assert.assertTrue(matcher.group(4).equals("driver com.ibm.db2.jcc.DB2Driver url 'jdbc:db2://130.1.16.26:50000/bhcdb' username db2inst1 password db2inst1"));
    }

}
