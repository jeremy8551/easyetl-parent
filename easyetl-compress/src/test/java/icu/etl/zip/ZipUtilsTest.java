package icu.etl.zip;

import java.io.File;
import java.io.IOException;

import icu.etl.apache.ant.zip.ZipFile;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class ZipUtilsTest {

    @Test
    public void testCompress() throws IOException {
        EasyBeanContext context = new EasyBeanContext();

        File dir = FileUtils.createTempDirectory(null);
        File f0 = new File(dir, "t1.txt");
        File f1 = new File(dir, "t2.txt");
        File f2 = new File(dir, "t3.txt");
        File f3 = new File(dir, "t4.txt");

        FileUtils.write(f0, StringUtils.CHARSET, false, "中文字符chinese charactors");
        FileUtils.write(f1, StringUtils.CHARSET, false, "中文字符chinese charactors");
        FileUtils.write(f2, StringUtils.CHARSET, false, "中文字符chinese charactors");
        FileUtils.write(f3, StringUtils.CHARSET, false, "中文字符chinese charactors");

        File zipfile = FileUtils.createTempFile("com.zip");
        FileUtils.createFile(zipfile);
        ZipUtils.compress(context, dir, zipfile, StringUtils.CHARSET, false);

        Assert.assertTrue(f0.exists() && f1.exists() && f2.exists() && f3.exists() && dir.exists() && dir.isDirectory());

        Assert.assertTrue(FileUtils.delete(zipfile));
        Assert.assertTrue(FileUtils.createFile(zipfile));
        ZipUtils.compress(context, dir, zipfile, StringUtils.CHARSET, true);
        Assert.assertTrue(zipfile.exists() && !dir.exists());
    }

    @Test
    public void testUncompress() throws IOException {
        EasyBeanContext context = new EasyBeanContext();

        File zipfile = FileUtils.createTempFile("com.zip");
        FileUtils.createFile(zipfile);

        File dir = FileUtils.createTempDirectory(null);
        File f0 = new File(dir, "t1.txt");
        File f1 = new File(dir, "t2.txt");
        File f2 = new File(dir, "t3.txt");
        File f3 = new File(dir, "t4.txt");

        FileUtils.write(f0, StringUtils.CHARSET, false, "中文字符chinese charactors");
        FileUtils.write(f1, StringUtils.CHARSET, false, "中文字符chinese charactors");
        FileUtils.write(f2, StringUtils.CHARSET, false, "中文字符chinese charactors");
        FileUtils.write(f3, StringUtils.CHARSET, false, "中文字符chinese charactors");

        ZipUtils.compress(context, dir, zipfile, StringUtils.CHARSET, true);
        Assert.assertTrue(!f0.exists() && !f1.exists() && !f2.exists() && !f3.exists() && !dir.exists());

        ZipUtils.uncompress(context, zipfile, dir.getParentFile(), StringUtils.CHARSET, false);
        Assert.assertTrue(f0.exists() && f1.exists() && f2.exists() && f3.exists() && dir.exists() && dir.isDirectory());
    }

    @Test
    public void testCloseQuietlyZipFile() throws IOException {
        File file1 = FileUtils.createTempFile(null);
        File file = new File(FileUtils.changeFilenameExt(file1.getAbsolutePath(), "zip"));
        FileUtils.delete(file);
        FileUtils.createFile(file);

        File f1 = FileUtils.createTempFile(null);
        FileUtils.createFile(f1);

        File f2 = FileUtils.createTempFile(null);
        FileUtils.createFile(f2);

        EasyBeanContext context = new EasyBeanContext();
        Compress c = context.getBean(Compress.class, FileUtils.getFilenameSuffix(file.getName()));
        c.setFile(file);
        c.archiveFile(f1, null);
        c.archiveFile(f2, null);
        c.close();

        ZipFile zipfile = new ZipFile(file, StringUtils.CHARSET) {
            public void close() throws IOException {
                throw new IOException();
            }
        };

        IO.closeQuietly(zipfile);
    }

    @Test
    public void test3() throws IOException {
        File dir = FileUtils.getTempDir("test", ZipUtilsTest.class.getSimpleName(), StringUtils.toRandomUUID()); // 文件所在目录
        Assert.assertTrue(FileUtils.createDirectory(dir));

        File f0 = new File(dir, "t1.txt");
        FileUtils.assertCreateFile(f0);
        FileUtils.write(f0, "utf-8", false, "f0");

        File f1 = new File(dir, "t2.txt");
        FileUtils.assertCreateFile(f1);

        File f2 = new File(dir, "t3.txt");
        FileUtils.assertCreateFile(f2);

        File f3 = new File(dir, "t4.txt");
        FileUtils.assertCreateFile(f3);

        System.out.println("压缩目录中的文件： " + dir.getAbsolutePath());

        File dir1 = FileUtils.getTempDir("test", ZipUtilsTest.class.getSimpleName(), StringUtils.toRandomUUID()); // 文件压缩后存储的目录
        FileUtils.createDirectory(dir1);
        System.out.println("压缩文件存储的目录: " + dir1.getAbsolutePath());

        GzipCompress c = new GzipCompress();
        c.gzipDir(dir, dir1, true, true);
        c.close();

        c.gunzipDir(dir1, dir1, true, true);
        c.close();

        File[] files = FileUtils.array(dir1.listFiles());
        for (File f : files) {
            System.out.println(f.getAbsolutePath());
        }
    }

    @Test
    public void test12() throws IOException {
        File parent = FileUtils.getTempDir("test", ZipUtilsTest.class.getSimpleName());
        File file = new File(parent, StringUtils.toRandomUUID() + ".tar"); // 文件所在目录
        FileUtils.createFile(file);

        File dir = FileUtils.getTempDir("test", ZipUtilsTest.class.getSimpleName(), StringUtils.toRandomUUID()); // 文件所在目录
        FileUtils.createDirectory(dir);

        File f0 = new File(dir, "t1.txt");
        FileUtils.assertCreateFile(f0);
        FileUtils.write(f0, "utf-8", false, "f0");

        File f1 = new File(dir, "t2.txt");
        FileUtils.assertCreateFile(f1);

        TarCompress c = new TarCompress();
        c.setFile(file);
        c.archiveFile(f0, null, "utf-8");
        c.archiveFile(f1, null, "utf-8");
        c.close();
        System.out.println("压缩文件: " + file.getAbsolutePath());

        File untardir = FileUtils.getTempDir("test", ZipUtilsTest.class.getSimpleName(), StringUtils.toRandomUUID()); // 文件所在目录
        Assert.assertTrue(FileUtils.createDirectory(untardir));
        System.out.println("解压目录: " + untardir.getAbsolutePath());
        c.extract(untardir.getAbsolutePath(), "UTF-8");
        c.close();

        File[] filelist = FileUtils.array(untardir.listFiles());
        for (File f : filelist) {
            System.out.println("解压后文件: " + f.getAbsolutePath());
            System.out.println(FileUtils.readline(f, "utf-8", 0));
        }

    }

}
