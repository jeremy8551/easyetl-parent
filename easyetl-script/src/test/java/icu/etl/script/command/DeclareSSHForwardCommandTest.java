package icu.etl.script.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icu.etl.util.Ensure;
import org.junit.Test;

public class DeclareSSHForwardCommandTest {

    @Test
    public void test() {
        String str = "declare name ssh tunnel use proxy name@proxyHost:proxySSHPort?password=proxyPassword connect to remoteHost:remoteSSHPort";
        Pattern compile = Pattern.compile(DeclareSSHTunnelCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("name"), str);
        Ensure.isTrue(matcher.group(2).equals("name@proxyHost:proxySSHPort?password=proxyPassword"), str);
        Ensure.isTrue(matcher.group(3).equals("remoteHost:remoteSSHPort"), str);
    }

    @Test
    public void test1() {
        String str = "declare name ssh tunnel use proxy name@proxyHost:proxySSHPort?password=proxyPassword connect to remoteHost:remoteSSHPort;";
        Pattern compile = Pattern.compile(DeclareSSHTunnelCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("name"), str);
        Ensure.isTrue(matcher.group(2).equals("name@proxyHost:proxySSHPort?password=proxyPassword"), str);
        Ensure.isTrue(matcher.group(3).equals("remoteHost:remoteSSHPort"), str);
    }

    @Test
    public void test11() {
        String str = "declare name ssh tunnel use proxy name@proxyHost:proxySSHPort?password=proxyPassword connect to remoteHost:remoteSSHPort ; ";
        Pattern compile = Pattern.compile(DeclareSSHTunnelCommandCompiler.REGEX);
        Matcher matcher = compile.matcher(str);
        Ensure.isTrue(matcher.find(), str);

        int size = matcher.groupCount();
        for (int i = 1; i <= size; i++) {
            System.out.println("第 " + i + " 个参数: " + matcher.group(i));
        }

        Ensure.isTrue(matcher.group(1).equals("name"), str);
        Ensure.isTrue(matcher.group(2).equals("name@proxyHost:proxySSHPort?password=proxyPassword"), str);
        Ensure.isTrue(matcher.group(3).equals("remoteHost:remoteSSHPort"), str);
    }

}
