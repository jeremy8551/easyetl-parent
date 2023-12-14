package icu.etl.sort;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import icu.etl.concurrent.ThreadSource;
import icu.etl.io.BufferedLineWriter;
import icu.etl.io.CommonTextTableFile;
import icu.etl.io.CommonTextTableFileReaderListener;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableLine;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.JVM)
public class TextTableFileTest {

    @Test
    public void testAll() throws IOException {
        CommonTextTableFile file = new CommonTextTableFile();
        file.setDelimiter(",");
        file.setCharsetName(StringUtils.CHARSET);

        File f = this.getTestFile(file);
        file.setAbsolutePath(f.getAbsolutePath());

        TextTableFileReader in = file.getReader(IO.FILE_BYTES_BUFFER_SIZE);
        try {
            assertTrue(file.getCharsetName().equals(StringUtils.CHARSET));
            assertTrue(file.getColumn() == 21);
            assertTrue(!file.existsEscape());
            assertTrue(file.getDelimiter().equals(","));

            int no = 0;
            TextTableLine line = null;
            while ((line = in.readLine()) != null) {
                assertTrue(++no == in.getLineNumber());
                assertTrue(line.getColumn() == file.getColumn());
                String firstfield = StringUtils.trimBlank(line.getColumn(1));
                assertTrue(Integer.parseInt(firstfield) >= 1 && Integer.parseInt(firstfield) <= 50000);
                assertTrue(StringUtils.inArray(in.getLineSeparator(), "\n", "\r", "\r\n"));
                assertTrue(StringUtils.inArray(in.getLineSeparator(), "\n", "\r", "\r\n"));
            }
            assertTrue(50000 == in.getLineNumber());

            // 读取第一行
//			assertTrue(file.canReadLine(1));
            assertTrue((line = in.readLine(1)) != null);
            assertTrue(StringUtils.trimBlank(line.getColumn(1)).equals("50000"));
            assertTrue(in.getLineNumber() == 1);

            // 读取最后一行
//			assertTrue(in.canReadLine(50000));
            assertTrue((line = in.readLine(50000)) != null);
            assertTrue(StringUtils.trimBlank(line.getColumn(1)).equals("1"));
            assertTrue(in.getLineNumber() == 50000);

            // 读取指定行
//			assertTrue(in.canReadLine(10000));
            assertTrue((line = in.readLine(10000)) != null);
            assertTrue(StringUtils.trimBlank(line.getColumn(1)).equals("40001"));
            assertTrue(in.getLineNumber() == 10000);

            // 回到首行之前即刚刚打开文件的状态
//			assertTrue(in.canReadLine(0));
            assertTrue(in.readLine(0) == null);
            assertTrue(in.getLineNumber() == 0);

        } finally {
            in.close();
        }
    }

    @Test
    public void testMergeLine() throws IOException {
        File parent = FileUtils.getTempDir("test", TextTableFileTest.class.getSimpleName());
        File dir = new File(parent, Dates.format08(new Date()));
        FileUtils.assertCreateDirectory(dir);

        File file = new File(dir, "SortTableFileTestMergeLine" + Dates.format17() + ".txt");
        FileWriter fw = new FileWriter(file);
        fw.write("1,11,12,13,14" + "\r\n");
        fw.write("2,2\r1,22,23,24" + "\r\n");
        fw.write("3,31,3\n2,33,34" + "\r\n");
        fw.write("4,4\r1,4\r\n2,4\n3,44" + "\r\n");
        fw.close();

        CommonTextTableFile tablefile = new CommonTextTableFile();
        tablefile.setAbsolutePath(file.getAbsolutePath());
        tablefile.setCharsetName("UTF-8");
        tablefile.setDelimiter(",");

        TextTableFileReader in = tablefile.getReader(IO.FILE_BYTES_BUFFER_SIZE);
        in.setListener(new CommonTextTableFileReaderListener());
        int i = 1;
        TextTableLine line = null;
        while ((line = in.readLine()) != null) {
            if (i == in.getLineNumber() && i != Integer.parseInt(StringUtils.trimBlank(line.getColumn(1)))) {
                assertTrue(i + ", " + in.getLineNumber() + ", " + StringUtils.trimBlank(line.getColumn(1)), false);
            }

            if (in.getLineNumber() == 2 && !line.getColumn(2).equals("21")) {
                assertTrue(i + ", " + in.getLineNumber() + ", " + StringUtils.trimBlank(line.getColumn(2)), false);
            }

            if (in.getLineNumber() == 3 && !line.getColumn(3).equals("32")) {
                assertTrue(i + ", " + in.getLineNumber() + ", " + StringUtils.trimBlank(line.getColumn(3)), false);
            }

            if (in.getLineNumber() == 4 && !line.getColumn(2).equals("41")) {
                assertTrue(i + ", " + in.getLineNumber() + ", " + StringUtils.trimBlank(line.getColumn(2)), false);
            }

            if (in.getLineNumber() == 4 && !line.getColumn(3).equals("42")) {
                assertTrue(i + ", " + in.getLineNumber() + ", " + StringUtils.trimBlank(line.getColumn(3)), false);
            }

            if (in.getLineNumber() == 4 && !line.getColumn(4).equals("43")) {
                assertTrue(i + ", " + in.getLineNumber() + ", " + StringUtils.trimBlank(line.getColumn(4)), false);
            }

            i++;
        }
        in.close();
    }

    @Test
    public void testMergeLine1() throws IOException {
        File parent = FileUtils.getTempDir("test", TextTableFileTest.class.getSimpleName());
        File dir = new File(parent, Dates.format08(new Date()));
        dir.mkdirs();
        File file = new File(dir, "SortTableFileTestMergeLine" + Dates.format17() + ".txt");
        FileWriter fw = new FileWriter(file);
        fw.write("1,11,12,13,14" + "\r\n");
        fw.write("2,2\r1,22,23,24" + "\r\n");
        fw.write("3,31,3\n2,33,34" + "\r\n");
        fw.write("4,4\r1,4\r\n2,4\n3,44" + "\r\n");
        fw.close();

        CommonTextTableFile tablefile = new CommonTextTableFile();
        tablefile.setAbsolutePath(file.getAbsolutePath());
        tablefile.setCharsetName("UTF-8");
        tablefile.setDelimiter(",");
//		tablefile.setIgnoreCRLF(false); // = false;
        TextTableFileReader in = tablefile.getReader(IO.FILE_BYTES_BUFFER_SIZE);
        in.setListener(new CommonTextTableFileReaderListener() {
            public void processLineSeparator(TextTableFile file, TextTableLine line, long lineNumber) throws IOException {
            }
        });

        int i = 1;
        TextTableLine line = null;
        while ((line = in.readLine()) != null) {
            if (i == in.getLineNumber() && i != Integer.parseInt(StringUtils.trimBlank(line.getColumn(1)))) {
                assertTrue(i + ", " + in.getLineNumber() + ", " + StringUtils.trimBlank(line.getColumn(1)), false);
            }

            if (in.getLineNumber() == 2 && !line.getContent().equals("2,2\r1,22,23,24")) {
                assertTrue("no: " + i + ", line: " + in.getLineNumber() + ", str: " + StringUtils.escapeLineSeparator(line.getContent()), false);
            }

            if (in.getLineNumber() == 3 && !line.getContent().equals("3,31,3\n2,33,34")) {
                assertTrue(i + ", " + in.getLineNumber() + ", " + StringUtils.trimBlank(line.getColumn(3)), false);
            }

            if (in.getLineNumber() == 4 && !line.getContent().equals("4,4\r1,4\r\n2,4\n3,44")) {
                assertTrue(i + ", " + in.getLineNumber() + ", " + StringUtils.trimBlank(line.getColumn(2)), false);
            }

            i++;
        }
        in.close();
    }

    @Test
    public void testSort() throws IOException {
        EasyBeanContext context = new EasyBeanContext();
        CommonTextTableFile file = new CommonTextTableFile();
        file.setDelimiter(",");
        file.setCharsetName(StringUtils.CHARSET);

        TableFileSortContext cxt = new TableFileSortContext();
        cxt.setWriterBuffer(50);
        cxt.setMaxRows(10000);
        cxt.setDeleteFile(true);
        cxt.setThreadNumber(1);
        cxt.setFileCount(2);
        cxt.setReaderBuffer(8192);
        cxt.setThreadSource(context.getBean(ThreadSource.class));

        TableFileSorter sorter = new TableFileSorter(cxt);

        File f0 = this.getTestFile(file);
        file.setAbsolutePath(f0.getAbsolutePath());
        try {
            cxt.setThreadNumber(1);
            cxt.setFileCount(2);
            File rs = sorter.sort(context, file, "1");
            CommonTextTableFile cf = file.clone();
            cf.setAbsolutePath(rs.getAbsolutePath());
            this.checkFile(cf);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        try {
            this.getTestFile(file);
            cxt.setThreadNumber(2);
            cxt.setFileCount(2);
            File rs = sorter.sort(context, file, "1");
            CommonTextTableFile cf = file.clone();
            cf.setAbsolutePath(rs.getAbsolutePath());
            this.checkFile(cf);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        try {
            this.getTestFile(file);
            cxt.setThreadNumber(5);
            cxt.setFileCount(2);
            File rs = sorter.sort(context, file, "1");
            CommonTextTableFile cf = file.clone();
            cf.setAbsolutePath(rs.getAbsolutePath());
            this.checkFile(cf);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        try {
            this.getTestFile(file);
            cxt.setThreadNumber(3);
            cxt.setFileCount(3);
            File rs = sorter.sort(context, file, "1");

            CommonTextTableFile cf = file.clone();
            cf.setAbsolutePath(rs.getAbsolutePath());
            this.checkFile(cf);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        try {
            this.getTestFile(file);
            File rs = sorter.sort(context, file, "1");
            CommonTextTableFile cf = file.clone();
            cf.setAbsolutePath(rs.getAbsolutePath());
            this.checkFile(cf);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * 测试倒序排序
     */
    @Test
    public void test1() throws Exception {
        EasyBeanContext context = new EasyBeanContext();
        CommonTextTableFile file = new CommonTextTableFile();
        file.setDelimiter(",");
        file.setCharsetName(StringUtils.CHARSET);

        File f = this.getTestFile(file);
        file.setAbsolutePath(f.getAbsolutePath());

        TableFileSortContext cxt = new TableFileSortContext();
        cxt.setWriterBuffer(50);
        cxt.setMaxRows(10000);
        cxt.setDeleteFile(true);
        cxt.setThreadNumber(3);
        cxt.setFileCount(3);
        cxt.setReaderBuffer(8192);
        cxt.setKeepSource(false);
        cxt.setThreadSource(context.getBean(ThreadSource.class));

        TableFileSorter s = new TableFileSorter(cxt);
        try {
            s.sort(context, file, "1 desc");
            int i = 50000;
            TextTableFileReader in = file.getReader(IO.FILE_BYTES_BUFFER_SIZE);
            TextTableLine line = null;
            while ((line = in.readLine()) != null) {
                if (i != Integer.parseInt(StringUtils.trimBlank(line.getColumn(1)))) {
                    fail(i + " != " + Integer.parseInt(StringUtils.trimBlank(line.getColumn(1))));
                }

                if (i + 19 != Integer.parseInt(StringUtils.trimBlank(line.getColumn(20)))) {
                    fail((i + 19) + " != " + Integer.parseInt(StringUtils.trimBlank(line.getColumn(20))));
                }

                i--;
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        s.sort(context, file, "1 asc");
        this.checkFile(file);

        s.sort(context, file, "int(1) asc");
        this.checkFile(file);
    }

    /**
     * 测试从指定位置开始读取文件
     */
    @Test
    public void test2() throws IOException {
        CommonTextTableFile file = new CommonTextTableFile();
        file.setDelimiter(",");
        file.setCharsetName(StringUtils.CHARSET);

        File f = this.getTestFile(file);
        file.setAbsolutePath(f.getAbsolutePath());
        long chars = 0;
        int count = 1000;

        // 继续向下写入数据
        int total = 40000;
        BufferedLineWriter out = new BufferedLineWriter(f, "UTF-8", 20);
        for (int i = total, z = 0; i > 0; i--) {
            StringBuilder buf = new StringBuilder();
            for (int j = 0; j < 20; j++) {
                buf.append(StringUtils.right(i + j, 8, ' '));
                buf.append(file.getDelimiter());
            }

            if (++z <= count) {
                chars += buf.length() + String.valueOf(FileUtils.lineSeparator).length();
            }

            if (out.writeLine(buf.toString(), String.valueOf(FileUtils.lineSeparator))) {
                out.flush();
            }
        }
        out.close();

        // 向下读取数据文件判断行数是否相等
        TextTableFileReader in = file.getReader(IO.FILE_BYTES_BUFFER_SIZE);
        assertTrue("越过文件 " + f.getAbsolutePath() + " 失败! rows: " + count + ", chars: " + chars, in.skip(chars, count));
        try {
            int c = 0;
            while (in.readLine() != null) {
                c++;
            }
            assertEquals(c + count, total);
        } finally {
            in.close();
        }
    }

    @Test
    public void test3() throws IOException {
        CommonTextTableFile file = new CommonTextTableFile();
        file.setDelimiter(",");
        file.setCharsetName(StringUtils.CHARSET);
        File f = this.getTestFile(file);
        file.setAbsolutePath(f.getAbsolutePath());

        int stat = 181;
        int max = 819;
        TextTableFileReader in = file.getReader(stat, max, 100);
        try {
            System.out.println("起始起始位置: " + in.getStartPointer());
            int count = 0;
            TextTableLine line = null;
            while ((line = in.readLine()) != null) {
                int lineSize = StringUtils.length(line.getContent(), file.getCharsetName()) + StringUtils.length(line.getLineSeparator(), file.getCharsetName());
                System.out.println("line: [" + line.getContent() + "] 实际 " + lineSize + " 个字节长度!");
                count += lineSize;
                if (in.getLineNumber() >= 200) {
                    break;
                }
            }
            assertEquals(181, in.getStartPointer());
            assertEquals(count, 905);
            System.out.println("最多能读取 " + max + " 个字节! 实际读取个 " + count + " 字节!");
        } finally {
            in.close();
        }
    }

    protected void checkFile(CommonTextTableFile file) throws NumberFormatException, IOException {
        int i = 0;
        TextTableFileReader in = file.getReader(IO.FILE_BYTES_BUFFER_SIZE);
        TextTableLine line = null;
        while ((line = in.readLine()) != null) {
            if (++i != Integer.parseInt(StringUtils.trimBlank(line.getColumn(1)))) {
                assertTrue(i + " != " + Integer.parseInt(StringUtils.trimBlank(line.getColumn(1))), false);
            }

            if (i + 19 != Integer.parseInt(StringUtils.trimBlank(line.getColumn(20)))) {
                assertTrue((i + 19) + " != " + StringUtils.trimBlank(line.getColumn(20)), false);
            }
        }
        in.close();
    }

    protected File getTestFile(TextTableFile file) throws IOException {
        File parent = FileUtils.getTempDir("test", TextTableFileTest.class.getSimpleName());
        File dir = new File(parent, Dates.format08(new Date()));
        FileUtils.assertCreateDirectory(dir);
        File f0 = new File(dir, "SortTableFile" + Dates.format17() + StringUtils.toRandomUUID() + ".txt");

        FileUtils.delete(f0);
        FileUtils.createFile(f0);

        FileWriter out = new FileWriter(f0);
        for (int i = 50000; i > 0; i--) {
            StringBuilder buf = new StringBuilder();
            for (int j = 0; j < 20; j++) {
                buf.append(StringUtils.right(i + j, 8, ' '));
                buf.append(file.getDelimiter());
            }
            out.write(buf + String.valueOf(FileUtils.lineSeparator));

            if (i % 20 == 0) {
                out.flush();
            }
        }
        out.flush();
        out.close();
        System.out.println(f0.getAbsolutePath());
        return f0;
    }

    @Test
    public void testGetTextTableFileColumn() throws IOException {
        CommonTextTableFile txt = new CommonTextTableFile();
        File file = FileUtils.createTempFile("testfile.txt");

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n2");
        txt.setAbsolutePath(file.getAbsolutePath());
        txt.setDelimiter(",");
        assertEquals(txt.countColumn() + " == " + 1, txt.countColumn(), 1);

        FileUtils.write(file, StringUtils.CHARSET, false, "");
        txt.setAbsolutePath(file.getAbsolutePath());
        txt.setDelimiter(",");
        assertEquals(txt.countColumn() + " == " + 0, txt.countColumn(), 0);

        FileUtils.write(file, StringUtils.CHARSET, false, "1,2,3");
        txt.setAbsolutePath(file.getAbsolutePath());
        txt.setDelimiter(",");
        int c = txt.countColumn();
        assertEquals(c + " == " + 3, c, 3);
    }

    @Test
    public void testCloneTableFile() {
        CommonTextTableFile c = new CommonTextTableFile();
        c.setAbsolutePath("/home/user/shell/test.del");
        c.setCharsetName(StringUtils.CHARSET);
        c.setColumn(20);
        c.setDelimiter(",");

        TextTableFile t = c.clone();
        Ensure.isTrue(t.getAbsolutePath().equals(c.getAbsolutePath()) //
                && t.getCharsetName().equals(c.getCharsetName()) //
                && t.getColumn() == c.getColumn() //
                && t.getDelimiter().equals(c.getDelimiter()) //
        );
    }

    @Test
    public void testDeleteFileTableFile() throws IOException {
        File file = FileUtils.createTempFile("testfile.txt");
        FileUtils.write(file, StringUtils.CHARSET, false, "1,2,3\na,b,c"); // 写入表格行数据文件内容

        TextTableFile tf = new CommonTextTableFile();
        String path = file.getAbsolutePath();
        tf.setAbsolutePath(path);
        tf.setDelimiter(",");
        tf.setCharsetName(StringUtils.CHARSET);
        tf.delete();

        Assert.assertFalse(file.exists());
    }

}
