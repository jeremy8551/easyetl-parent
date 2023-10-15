package icu.etl.os;

import java.io.File;
import java.io.IOException;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.collection.ByteBuffer;
import icu.etl.ioc.BeanConfig;
import icu.etl.ioc.BeanContext;
import icu.etl.ioc.BeanFactory;
import icu.etl.ioc.NationalHoliday;
import icu.etl.util.ClassUtils;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class NationalHolidaysTest {

    @Test
    public void test() throws IOException {
        File javadir = FileUtils.getTempDir(NationalHolidaysTest.class);
        Ensure.isTrue(FileUtils.createDirectory(javadir));

        String charsetName = "utf-8";
        String className = "USHolidays";
        String uri = "/" + NationalHolidaysTest.class.getPackage().getName().replace('.', '/') + "/" + className + ".txt";
        System.out.println(uri);
        String source = new ByteBuffer().append(ClassUtils.getResourceAsStream(uri)).toString();

        File javafile = new File(javadir, className + ".java");
        System.out.println("javafile: " + javafile.getAbsolutePath());
        javafile.delete();
        Ensure.isTrue(FileUtils.write(javafile, StringUtils.CHARSET, false, source), source);

        String classesDir = ClassUtils.getClasspath(BeanContext.class);
        System.out.println("class dir: " + classesDir);

        String testClassesDir = ClassUtils.getClasspath(NationalHolidaysTest.class);
        System.out.println("Test class dir: " + testClassesDir);

        File classfile = new File(FileUtils.joinFilepath(testClassesDir, FileUtils.changeFilenameExt(uri, "class")));
        FileUtils.delete(classfile);
        System.out.println("classes path: " + classfile.getAbsolutePath());

        NationalHoliday bean = BeanFactory.get(NationalHoliday.class);
        Ensure.isTrue(!bean.getRestDays().contains(Dates.parse("2021-12-24")));
        Ensure.isTrue(!bean.getWorkDays().contains(Dates.parse("2021-12-24")));

        // 编译 java 源文件
        OS os = BeanFactory.get(OS.class);
        try {
            assertTrue(os.enableOSCommand());
            OSCommand cmd = os.getOSCommand();
            cmd.execute("java -version ");
            System.out.println(cmd.getStdout(charsetName));

            if (cmd.execute("javac -d " + testClassesDir + " -cp " + classesDir + " " + javafile.getAbsolutePath()) != 0) {
                System.out.println(cmd.getStdout(charsetName));
                System.out.println(cmd.getStderr(charsetName));
                assertTrue(false);
            }
        } finally {
            os.close();
            classfile.deleteOnExit();
        }

        String fullName = NationalHolidaysTest.class.getPackage().getName() + "." + className;
        Class<? extends NationalHoliday> cls = ClassUtils.loadClass(fullName);

        EasyBeanClass anno = cls.getAnnotation(EasyBeanClass.class);
        BeanFactory.getContext().add(new BeanConfig(anno.type(), cls, anno), null);
        Ensure.isTrue(bean.getWorkDays().contains(Dates.parse("2021-12-24")));
        Ensure.isTrue(!bean.getRestDays().contains(Dates.parse("2021-12-24")));
    }
}
