package icu.etl.zip;

import java.io.File;
import java.io.IOException;
import java.util.List;

import icu.apache.ant.zip.ZipEntry;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class CompressTest {

    /**
     * 使用指定用户名创建一个文件
     *
     * @return 返回临时文件
     */
    private File createfile(String suffix) throws IOException {
        File file = FileUtils.createTempFile("CompressTestfile." + suffix);
        System.out.println(file.getAbsolutePath());
        return file;
    }

    @Test
    public void test() throws IOException {
        File file = this.createfile("txt.tar.gz");
        EasyBeanContext context = new EasyBeanContext();
        Compress compress = context.getBean(Compress.class, file);
        Assert.assertEquals(GzipCompress.class, compress.getClass());
    }

    @Test
    public void test1() throws IOException {
        File file = this.createfile("tar.rar");
        EasyBeanContext context = new EasyBeanContext();
        Compress compress = context.getBean(Compress.class, file);
        Assert.assertEquals(RarCompress.class, compress.getClass());
    }

    @Test
    public void test2() throws IOException {
        File file = this.createfile("tar");
        EasyBeanContext context = new EasyBeanContext();
        Compress compress = context.getBean(Compress.class, file);
        Assert.assertEquals(TarCompress.class, compress.getClass());
    }

    @Test
    public void test3() throws IOException {
        File file = this.createfile("zip");
        EasyBeanContext context = new EasyBeanContext();
        Compress compress = context.getBean(Compress.class, file);
        Assert.assertEquals(ZipCompress.class, compress.getClass());
    }

    /**
     * 测试 tar.gz
     *
     * @throws IOException
     */
    @Test
    public void test4() throws IOException {
        String ext = "gz";

        EasyBeanContext context = new EasyBeanContext();
        File compressfile = this.createfile(ext); // 压缩文件

        File f1 = new File(compressfile.getParentFile(), "t1.txt");
        File f2 = new File(compressfile.getParentFile(), "t2.txt");
        File f3 = new File(compressfile.getParentFile(), "t3.txt");
        File f4 = new File(compressfile.getParentFile(), "t4中文.txt");

        // 先删除
        f1.delete();
        f2.delete();
        f3.delete();
        f4.delete();

        FileUtils.write(f1, "utf-8", false, ext + " " + f1.getAbsolutePath());
        FileUtils.write(f2, "utf-8", false, ext + " " + f2.getAbsolutePath());
        FileUtils.write(f3, "utf-8", false, ext + " " + f3.getAbsolutePath());
        FileUtils.write(f4, "utf-8", false, ext + " 测试中文 " + f4.getAbsolutePath());

        Compress c = context.getBean(Compress.class, compressfile);
        c.setFile(compressfile);
        c.archiveFile(f1, null, "utf-8");
        c.archiveFile(f2, "", "utf-8");
        c.archiveFile(f3, "dir", "utf-8");
        c.archiveFile(f4, "dir/cdir", "utf-8");
        c.close();
        System.out.println("压缩完毕! " + compressfile.getAbsolutePath());

        File parentdir = new File(f4.getParentFile(), "dir/cdir");
        f1.delete();
        f2.delete();
        f3.delete();
        f4.delete();
        Assert.assertTrue(FileUtils.deleteDirectory(parentdir));

        Assert.assertFalse(f1.exists());
        Assert.assertFalse(f2.exists());
        Assert.assertFalse(f3.exists());
        Assert.assertFalse(f4.exists());

        // 解压缩
        c.setFile(compressfile);
        c.extract(compressfile.getParentFile().getAbsolutePath(), "UTF-8");
        c.close();
        System.out.println("文件 " + compressfile.getAbsolutePath() + " 解压完毕!");
        System.out.println("目录: " + compressfile.getParentFile().getAbsolutePath());
    }

    /**
     * 测试 zip 格式的子目录压缩与删除压缩包中的文件
     *
     * @throws IOException
     */
    @Test
    public void test6() throws IOException {
        String ext = "zip";

        EasyBeanContext context = new EasyBeanContext();
        File compressfile = this.createfile(ext); // 压缩文件

        File f1 = new File(compressfile.getParentFile(), "t1.txt");
        File f2 = new File(compressfile.getParentFile(), "t2.txt");
        File f3 = new File(compressfile.getParentFile(), "t3.txt");
        File f4 = new File(compressfile.getParentFile(), "t4中文.txt");

        // 先删除
        f1.delete();
        f2.delete();
        f3.delete();
        f4.delete();

        FileUtils.write(f1, "utf-8", false, ext + " " + f1.getAbsolutePath());
        FileUtils.write(f2, "utf-8", false, ext + " " + f2.getAbsolutePath());
        FileUtils.write(f3, "utf-8", false, ext + " " + f3.getAbsolutePath());
        FileUtils.write(f4, "utf-8", false, ext + " 测试中文 " + f4.getAbsolutePath());

        Compress c = context.getBean(Compress.class, compressfile);
        c.setFile(compressfile);
        c.archiveFile(f1, null, "utf-8");
        c.archiveFile(f2, "", "utf-8");
        c.archiveFile(f3, "dir", "utf-8");
        c.archiveFile(f4, "dir/cdir", "utf-8");
        c.close();
        System.out.println("压缩完毕 ..");

        File parentfile = new File(f4.getParentFile(), "dir/cdir/");
        Assert.assertTrue(f1.delete());
        Assert.assertTrue(f2.delete());
        Assert.assertTrue(f3.delete());
        Assert.assertTrue(f4.delete());
        Assert.assertTrue(FileUtils.deleteDirectory(parentfile));

        Assert.assertFalse(f1.exists());
        Assert.assertFalse(f2.exists());
        Assert.assertFalse(f3.exists());
        Assert.assertFalse(f4.exists());
        Assert.assertFalse(parentfile.exists());

        c = context.getBean(Compress.class, compressfile);
        c.setFile(compressfile);
        c.removeEntry("utf-8", "dir/cdir");
        c.close();

        // 解压缩
        c.setFile(compressfile);
        c.extract(compressfile.getParentFile().getAbsolutePath(), "UTF-8");
        c.close();
        System.out.println("文件 " + compressfile.getAbsolutePath() + " 解压完毕!");
        System.out.println("目录: " + compressfile.getParentFile().getAbsolutePath());

        Assert.assertTrue(f1.exists());
        Assert.assertTrue(f2.exists());
        Assert.assertTrue(new File(f3.getParentFile(), "dir/" + f3.getName()).exists());
        Assert.assertFalse(parentfile.exists());
    }

    @Test
    public void test5() throws IOException {
        testcompress("zip");
    }

    private void testcompress(String ext) throws IOException {
        EasyBeanContext context = new EasyBeanContext();

        File compressfile = this.createfile(ext); // 压缩文件

        File f1 = new File(compressfile.getParentFile(), "t1.txt");
        File f2 = new File(compressfile.getParentFile(), "t2.txt");
        File f3 = new File(compressfile.getParentFile(), "t3.txt");
        File f4 = new File(compressfile.getParentFile(), "t4中文.txt");

        // 先删除
        f1.delete();
        f2.delete();
        f3.delete();
        f4.delete();

        FileUtils.write(f1, "utf-8", false, ext + " " + f1.getAbsolutePath());
        FileUtils.write(f2, "utf-8", false, ext + " " + f2.getAbsolutePath());
        FileUtils.write(f3, "utf-8", false, ext + " " + f3.getAbsolutePath());
        FileUtils.write(f4, "utf-8", false, ext + " 测试中文 " + f4.getAbsolutePath());

        Compress c = context.getBean(Compress.class, compressfile);
        c.setFile(compressfile);
        c.archiveFile(f1, null, "utf-8");
        c.archiveFile(f2, "", "utf-8");
        c.archiveFile(f3, "dir", "utf-8");
        c.archiveFile(f4, "dir/cdir", "utf-8");
        c.close();
        System.out.println("压缩完毕 ..");

        File parentdir = new File(f4.getParentFile(), "dir/cdir");
        Assert.assertTrue(f1.delete());
        Assert.assertTrue(f2.delete());
        Assert.assertTrue(f3.delete());
        Assert.assertTrue(f4.delete());
        Assert.assertTrue(FileUtils.deleteDirectory(parentdir));

        Assert.assertFalse(f1.exists());
        Assert.assertFalse(f2.exists());
        Assert.assertFalse(f3.exists());
        Assert.assertFalse(f4.exists());

        // 解压缩
        c.setFile(compressfile);
        c.extract(compressfile.getParentFile().getAbsolutePath(), "UTF-8");
        c.close();
        System.out.println("文件 " + compressfile.getAbsolutePath() + " 解压完毕!");
        System.out.println("目录: " + compressfile.getParentFile().getAbsolutePath());

        Assert.assertTrue(f1.exists());
        Assert.assertTrue(f2.exists());
        Assert.assertTrue(new File(f3.getParentFile(), "dir/" + f3.getName()).exists());

        File uncomf4 = new File(parentdir, f4.getName());
        Assert.assertTrue(uncomf4.exists());
    }

    public static void main3(String[] args) throws IOException {
        String path = "C:\\Users\\etl\\Desktop\\cshi\\cshi.zip";
        ZipCompress jc = new ZipCompress();
        jc.setFile(new File(path));
        System.out.println(jc.removeEntry(null, "cshi/底1层"));
        jc.close();
    }

    public static void main1(String[] args) throws IOException {
        String zip = "C:\\Users\\etl\\Desktop\\test\\cshi.zip";
        File zipFile = new File(zip);

        ZipCompress jc = new ZipCompress();
        jc.setFile(zipFile);
        List<ZipEntry> list = jc.getEntrys("UTF-8", "SEC.sql", true);
        System.out.println(StringUtils.toString(list));
        for (ZipEntry obj : list) {
            jc.extract(zipFile.getParent(), "UTF-8", obj.getName());
        }
        jc.close();
    }

}
