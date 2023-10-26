package icu.etl.script;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import icu.etl.ioc.AnnotationEasyetlContext;
import icu.etl.ioc.BeanInfo;
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
        List<BeanInfo> commands = context.getBeanInfoList(UniversalCommandCompiler.class);
        Collections.sort(commands, new Comparator<BeanInfo>() {
            public int compare(BeanInfo o1, BeanInfo o2) {
                return o1.getType().getName().compareTo(o2.getType().getName());
            }
        });

        StringBuilder buf = new StringBuilder();
        STD.out.info("");
        Ensure.isTrue(ClassUtils.containsMethod(CommandScanner.class, "loadCommandBuilder"));
        Ensure.isTrue(ClassUtils.containsMethod(CommandScanner.class, "loadScriptCommand", Class.class));
        for (BeanInfo cls : commands) {
            STD.out.info(cls.getType().getName());
            buf.append("\t\t").append("this.loadScriptCommand(" + cls.getType().getName() + ".class);").append(FileUtils.lineSeparator);
        }
//		Std.out.info("共找到 " + commands.size() + " 个脚本命令类, 将以上内容替换到 " + CommandScanner.class.getName() + ".loadCommandBuilder() 方法中");

        STD.out.info("");
        List<BeanInfo> methods = context.getBeanInfoList(UniversalScriptVariableMethod.class);
        Collections.sort(methods, new Comparator<BeanInfo>() {
            public int compare(BeanInfo o1, BeanInfo o2) {
                return o1.getType().getName().compareTo(o2.getType().getName());
            }
        });

        STD.out.info("");
        buf.setLength(0);
        Ensure.isTrue(ClassUtils.containsMethod(VariableMethodScanner.class, "loadVariableMethodBuilder"));
        Ensure.isTrue(ClassUtils.containsMethod(VariableMethodScanner.class, "loadVariableMethod", Class.class));
        for (BeanInfo beanInfo : methods) {
            STD.out.info(beanInfo.getType().getName());
            buf.append("\t\t").append("this.loadVariableMethod(" + beanInfo.getType().getName() + ".class);").append(FileUtils.lineSeparator);
        }
//		Std.out.info("共找到 " + methods.size() + " 个变量方法类, 将以上内容替换到 " + VariableMethodScanner.class.getName() + ".loadVariableMethodBuilder() 方法中");
    }
}
