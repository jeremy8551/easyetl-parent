package icu.etl.zip;

import java.io.File;
import java.io.IOException;

import icu.apache.ant.zip.ZipFile;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ZipUtilsTest {

    /**
     * 返回一个临时文件
     *
     * @return
     */
    public static File getFile() {
        return getFile(null);
    }

    /**
     * 使用指定用户名创建一个文件
     *
     * @param name
     * @return
     */
    public static File getFile(String name) {
        if (StringUtils.isBlank(name)) {
            name = FileUtils.getFilenameRandom("testfile", "_tmp") + ".txt";
        }

        File dir = new File(FileUtils.getTempDir(ZipUtilsTest.class), StringUtils.toRandomUUID());
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("创建目录 " + dir.getAbsolutePath() + " 失败!");
        } else {
            return new File(dir, name); // 返回一个临时文件信息
        }
    }

    @Test
    public void testCompress() throws IOException {
        EasyBeanContext context = new EasyBeanContext();
        File f = getFile();
        FileUtils.createDirectory(f);

        File zipfile = getFile("com.zip");
        FileUtils.createFile(zipfile);

        File f0 = new File(f, "t1.txt");
        File f1 = new File(f, "t2.txt");
        File f2 = new File(f, "t3.txt");
        File f3 = new File(f, "t4.txt");

        FileUtils.write(f0, StringUtils.CHARSET, false, "中文字符chinese charactors");
        FileUtils.write(f1, StringUtils.CHARSET, false, "中文字符chinese charactors");
        FileUtils.write(f2, StringUtils.CHARSET, false, "中文字符chinese charactors");
        FileUtils.write(f3, StringUtils.CHARSET, false, "中文字符chinese charactors");

        ZipUtils.compress(context, f, zipfile, StringUtils.CHARSET, false);

        assertTrue(f0.exists() && f1.exists() && f2.exists() && f3.exists() && f.exists() && f.isDirectory());

        FileUtils.delete(zipfile);
        FileUtils.createFile(zipfile);
        ZipUtils.compress(context, f, zipfile, StringUtils.CHARSET, true);
        assertTrue(zipfile.exists() && !f.exists());
    }

    @Test
    public void testUncompress() throws IOException {
        EasyBeanContext context = new EasyBeanContext();
        File f = getFile();
        FileUtils.createDirectory(f);

        File zipfile = getFile("com.zip");
        FileUtils.createFile(zipfile);

        File f0 = new File(f, "t1.txt");
        File f1 = new File(f, "t2.txt");
        File f2 = new File(f, "t3.txt");
        File f3 = new File(f, "t4.txt");

        FileUtils.write(f0, StringUtils.CHARSET, false, "中文字符chinese charactors");
        FileUtils.write(f1, StringUtils.CHARSET, false, "中文字符chinese charactors");
        FileUtils.write(f2, StringUtils.CHARSET, false, "中文字符chinese charactors");
        FileUtils.write(f3, StringUtils.CHARSET, false, "中文字符chinese charactors");

        ZipUtils.compress(context, f, zipfile, StringUtils.CHARSET, true);
        assertTrue(!f0.exists() && !f1.exists() && !f2.exists() && !f3.exists() && !f.exists());

        ZipUtils.uncompress(context, zipfile, f.getParentFile(), StringUtils.CHARSET, false);
        assertTrue(f0.exists() && f1.exists() && f2.exists() && f3.exists() && f.exists() && f.isDirectory());
    }

    @Test
    public void testCloseQuietlyZipFile() throws IOException {
        File file = new File(FileUtils.changeFilenameExt(getFile().getAbsolutePath(), "zip"));
        FileUtils.delete(file);
        FileUtils.createFile(file);

        File f1 = getFile();
        FileUtils.createFile(f1);

        File f2 = getFile();
        FileUtils.createFile(f2);

        EasyBeanContext context = new EasyBeanContext();
        Compress c = context.getBean(Compress.class, FileUtils.getFilenameSuffix(file.getName()));
        c.setFile(file);
        c.archiveFile(f1, null);
        c.archiveFile(f2, null);
        c.close();

        ZipFile zipfile = new ZipFile(file, StringUtils.CHARSET) {
            public void close() throws IOException {
                throw new RuntimeException();
            }
        };

        IO.closeQuietly((ZipFile) zipfile);
    }

    @Test
    public void test3() throws IOException {
        File dir = new File(FileUtils.getTempDir(ZipUtilsTest.class), StringUtils.toRandomUUID()); // 文件所在目录
        FileUtils.createDirectory(dir);

        File f0 = new File(dir, "t1.txt");
        f0.createNewFile();
        FileUtils.write(f0, "utf-8", false, "f0");

        File f1 = new File(dir, "t2.txt");
        f1.createNewFile();

        File f2 = new File(dir, "t3.txt");
        f2.createNewFile();

        File f3 = new File(dir, "t4.txt");
        f3.createNewFile();

        System.out.println("压缩目录中的文件： " + dir.getAbsolutePath());

        File dir1 = new File(FileUtils.getTempDir(ZipUtilsTest.class), StringUtils.toRandomUUID()); // 文件压缩后存储的目录
        FileUtils.createDirectory(dir1);
        System.out.println("压缩文件存储的目录: " + dir1.getAbsolutePath());

        GzipCompress c = new GzipCompress();
        c.gzipDir(dir, dir1, true, true);
        c.close();

        c.gunzipDir(dir1, dir1, true, true);
        c.close();

        File[] files = dir1.listFiles();
        for (File f : files) {
            System.out.println(f.getAbsolutePath());
        }
    }

    @Test
    public void test12() throws IOException {
        File file = new File(FileUtils.getTempDir(ZipUtilsTest.class), StringUtils.toRandomUUID() + ".tar"); // 文件所在目录
        FileUtils.createFile(file);

        File dir = new File(FileUtils.getTempDir(ZipUtilsTest.class), StringUtils.toRandomUUID()); // 文件所在目录
        FileUtils.createDirectory(dir);

        File f0 = new File(dir, "t1.txt");
        f0.createNewFile();
        FileUtils.write(f0, "utf-8", false, "f0");

        File f1 = new File(dir, "t2.txt");
        f1.createNewFile();

        TarCompress c = new TarCompress();
        c.setFile(file);
        c.archiveFile(f0, null, "utf-8");
        c.archiveFile(f1, null, "utf-8");
        c.close();
        System.out.println("压缩文件: " + file.getAbsolutePath());

        File untardir = new File(FileUtils.getTempDir(ZipUtilsTest.class), StringUtils.toRandomUUID()); // 文件所在目录
        FileUtils.createDirectory(untardir);
        System.out.println("解压目录: " + untardir.getAbsolutePath());
        c.extract(untardir.getAbsolutePath(), "UTF-8");
        c.close();

        for (File f : untardir.listFiles()) {
            System.out.println("解压后文件: " + f.getAbsolutePath());
            System.out.println(FileUtils.readline(f, "utf-8", 0));
        }

    }

}
