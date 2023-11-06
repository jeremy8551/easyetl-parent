package icu.etl.script;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import icu.etl.ioc.BeanInfo;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.log.STD;
import icu.etl.util.ClassUtils;
import icu.etl.util.FileUtils;
import org.junit.Test;

/**
 * 打印脚本引擎中已注册的命令
 */
public class ScriptPringCommandInfoTest {

    @Test
    public void test() throws IOException {
        EasyBeanContext context = new EasyBeanContext("sout:info");

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
        for (BeanInfo cls : commands) {
            STD.out.info(cls.getType().getName());
            buf.append("\t\t").append(cls.getType().getName()).append(FileUtils.lineSeparator);
        }
        STD.out.info("共找到 " + commands.size() + " 个脚本命令类!");

        STD.out.info("");
        List<BeanInfo> methods = context.getBeanInfoList(UniversalScriptVariableMethod.class);
        Collections.sort(methods, new Comparator<BeanInfo>() {
            public int compare(BeanInfo o1, BeanInfo o2) {
                return o1.getType().getName().compareTo(o2.getType().getName());
            }
        });

        STD.out.info("");
        buf.setLength(0);
        for (BeanInfo beanInfo : methods) {
            STD.out.info(beanInfo.getType().getName());
            buf.append("\t\t").append("this.loadVariableMethod(" + beanInfo.getType().getName() + ".class);").append(FileUtils.lineSeparator);
        }
        STD.out.info("共找到 " + methods.size() + " 个变量方法类!");
    }
}
