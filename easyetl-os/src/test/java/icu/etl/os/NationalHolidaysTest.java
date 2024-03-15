package icu.etl.os;

import java.io.File;
import java.io.IOException;

import icu.etl.cn.NationalHoliday;
import icu.etl.collection.ByteBuffer;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.ioc.impl.EasyBeanInfoImpl;
import icu.etl.util.ClassUtils;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class NationalHolidaysTest {

    public WithSSHRule rule = new WithSSHRule();

    @Test
    public void test() throws IOException {
        StringBuilder buf = new StringBuilder();
        String[] array = ClassUtils.getJavaClassPath();
        for (String path : array) {
            System.out.println(path);
            buf.append('\"').append(path).append('\"').append(System.getProperty("path.separator"));
        }

        EasyBeanContext context = rule.getContext();
        File parent = FileUtils.getTempDir("test", NationalHolidaysTest.class.getSimpleName());

        String charsetName = StringUtils.CHARSET;
        String className = "USHolidays";
        String uri = "/icu/etl/bean/" + className + ".txt";
        System.out.println(uri);
        String source = new ByteBuffer().append(ClassUtils.getResourceAsStream(uri)).toString();

        File javafile = new File(parent, className + ".java");
        System.out.println("javafile: " + javafile.getAbsolutePath());
        Assert.assertTrue(FileUtils.deleteFile(javafile));
        Assert.assertTrue(FileUtils.write(javafile, StringUtils.CHARSET, false, source));

        System.out.println("class dir: " + buf);

        String testClassesDir = ClassUtils.getClasspath(NationalHolidaysTest.class);
        System.out.println("Test class dir: " + testClassesDir);

        File classfile = new File(FileUtils.joinPath(testClassesDir, FileUtils.changeFilenameExt(uri, "class")));
        FileUtils.delete(classfile);
        System.out.println("classes path: " + classfile.getAbsolutePath());

        NationalHoliday bean = context.getBean(NationalHoliday.class, "zh_CN");
        Assert.assertNotNull(bean);
        Assert.assertFalse(bean.getRestDays().contains(Dates.parse("2021-12-24")));
        Assert.assertFalse(bean.getWorkDays().contains(Dates.parse("2021-12-24")));

        // 编译 java 源文件
        OS os = context.getBean(OS.class);
        try {
            Assert.assertTrue(os.enableOSCommand());
            OSCommand cmd = os.getOSCommand();
            cmd.execute("java -version ");
            System.out.println(cmd.getStdout(charsetName));

            if (cmd.execute("javac -d " + testClassesDir + " -cp " + buf + " " + javafile.getAbsolutePath()) != 0) {
                System.out.println(cmd.getStdout(charsetName));
                System.out.println(cmd.getStderr(charsetName));
                Assert.fail();
            }
        } finally {
            os.close();
            classfile.deleteOnExit();
        }

        String className1 = StringUtils.trim(FileUtils.changeFilenameExt(uri, ""), '/', '.').replace('/', '.');
        System.out.println("load class " + className1);
        Class<? extends NationalHoliday> cls = ClassUtils.loadClass(className1);

        Assert.assertTrue(context.addBean(new EasyBeanInfoImpl(cls)));
        Assert.assertTrue(bean.getWorkDays().contains(Dates.parse("2021-12-24")));
        Assert.assertFalse(bean.getRestDays().contains(Dates.parse("2021-12-24")));
    }
}
