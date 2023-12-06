package icu.etl.script;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import icu.etl.ioc.EasyBeanContext;
import icu.etl.ioc.EasyBeanInfo;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ClassUtils;
import icu.etl.util.FileUtils;
import org.junit.Test;

/**
 * 打印脚本引擎中已注册的命令
 */
public class ScriptPringCommandInfoTest {
    private final static Log log = LogFactory.getLog(ScriptPringCommandInfoTest.class);

    @Test
    public void test() throws IOException {
        EasyBeanContext context = new EasyBeanContext("sout:info");

        String[] list = ClassUtils.getJavaClassPath();
        for (String classpath : list) {
            log.info("classpath: " + classpath);
        }

        log.info("");
        List<EasyBeanInfo> commands = context.getBeanInfoList(UniversalCommandCompiler.class);
        Collections.sort(commands, new Comparator<EasyBeanInfo>() {
            public int compare(EasyBeanInfo o1, EasyBeanInfo o2) {
                return o1.getType().getName().compareTo(o2.getType().getName());
            }
        });

        StringBuilder buf = new StringBuilder();
        log.info("");
        for (EasyBeanInfo cls : commands) {
            log.info(cls.getType().getName());
            buf.append("\t\t").append(cls.getType().getName()).append(FileUtils.lineSeparator);
        }
        log.info("共找到 " + commands.size() + " 个脚本命令类!");

        log.info("");
        List<EasyBeanInfo> methods = context.getBeanInfoList(UniversalScriptVariableMethod.class);
        Collections.sort(methods, new Comparator<EasyBeanInfo>() {
            public int compare(EasyBeanInfo o1, EasyBeanInfo o2) {
                return o1.getType().getName().compareTo(o2.getType().getName());
            }
        });

        log.info("");
        buf.setLength(0);
        for (EasyBeanInfo beanInfo : methods) {
            log.info(beanInfo.getType().getName());
            buf.append("\t\t").append("this.loadVariableMethod(" + beanInfo.getType().getName() + ".class);").append(FileUtils.lineSeparator);
        }
        log.info("共找到 " + methods.size() + " 个变量方法类!");
    }
}
