package icu.etl.script;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import icu.etl.ioc.AnnotationEasyetlContext;
import icu.etl.ioc.BeanConfig;
import icu.etl.log.STD;
import icu.etl.script.internal.CommandScanner;
import icu.etl.script.method.VariableMethodScanner;
import icu.etl.util.ClassUtils;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;

public class 打印所有命令 {

    public static void main(String[] args) throws IOException {
        AnnotationEasyetlContext context = new AnnotationEasyetlContext("sout:info");

        String[] list = ClassUtils.getJavaClassPath();
        for (String classpath : list) {
            STD.out.info("classpath: " + classpath);
        }

        STD.out.info("");
        List<BeanConfig> commands = new ArrayList<BeanConfig>(context.getImplements(UniversalCommandCompiler.class));
        Collections.sort(commands, new Comparator<BeanConfig>() {
            public int compare(BeanConfig o1, BeanConfig o2) {
                return o1.getBeanClass().getName().compareTo(o2.getBeanClass().getName());
            }
        });

        StringBuilder buf = new StringBuilder();
        STD.out.info("");
        Ensure.isTrue(ClassUtils.containsMethod(CommandScanner.class, "loadCommandBuilder"));
        Ensure.isTrue(ClassUtils.containsMethod(CommandScanner.class, "loadScriptCommand", Class.class));
        for (BeanConfig cls : commands) {
            STD.out.info(cls.getBeanClass().getName());
            buf.append("\t\t").append("this.loadScriptCommand(" + cls.getBeanClass().getName() + ".class);").append(FileUtils.lineSeparator);
        }
//		Std.out.info("共找到 " + commands.size() + " 个脚本命令类, 将以上内容替换到 " + CommandScanner.class.getName() + ".loadCommandBuilder() 方法中");

        STD.out.info("");
        List<BeanConfig> methods = new ArrayList<BeanConfig>(context.getImplements(UniversalScriptVariableMethod.class));
        Collections.sort(methods, new Comparator<BeanConfig>() {
            public int compare(BeanConfig o1, BeanConfig o2) {
                return o1.getBeanClass().getName().compareTo(o2.getBeanClass().getName());
            }
        });

        STD.out.info("");
        buf.setLength(0);
        Ensure.isTrue(ClassUtils.containsMethod(VariableMethodScanner.class, "loadVariableMethodBuilder"));
        Ensure.isTrue(ClassUtils.containsMethod(VariableMethodScanner.class, "loadVariableMethod", Class.class));
        for (BeanConfig cls : methods) {
            STD.out.info(cls.getBeanClass().getName());
            buf.append("\t\t").append("this.loadVariableMethod(" + cls.getBeanClass().getName() + ".class);").append(FileUtils.lineSeparator);
        }
//		Std.out.info("共找到 " + methods.size() + " 个变量方法类, 将以上内容替换到 " + VariableMethodScanner.class.getName() + ".loadVariableMethodBuilder() 方法中");
    }
}
