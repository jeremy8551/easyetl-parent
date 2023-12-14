package icu.etl.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import icu.etl.concurrent.ThreadSource;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class CsvFileTest {

    /**
     * 使用指定用户名创建一个文件
     *
     * @return 返回临时文件
     */
    private File createfile() throws IOException {
        File dir = FileUtils.getTempDir("test", CsvFileTest.class.getSimpleName());
        File file = new File(dir, "csvTestFile" + StringUtils.toRandomUUID() + ".csv");
        FileUtils.createFile(file);
        System.out.println(CsvFile.class.getSimpleName() + " testfile: " + file.getAbsolutePath());
        return file;
    }

    @Test
    public void testSplitCsvFileLineString() {
        Assert.assertEquals("String[]", StringUtils.toString(CsvFile.splitCsvFileLine("")));
        Assert.assertEquals("String[1]", StringUtils.toString(CsvFile.splitCsvFileLine("1")));
        Assert.assertEquals("String[12]", StringUtils.toString(CsvFile.splitCsvFileLine("12")));
        Assert.assertEquals("String[12, ]", StringUtils.toString(CsvFile.splitCsvFileLine("12,")));
        Assert.assertEquals("String[12, 3, ]", StringUtils.toString(CsvFile.splitCsvFileLine("12,3,")));
        Assert.assertEquals("String[12, 3, 4]", StringUtils.toString(CsvFile.splitCsvFileLine("12,3,4")));
        Assert.assertEquals("String[12, 3, 4, 测试 哈, sdf , 测试\" 哈,  sdf, ]", StringUtils.toString(CsvFile.splitCsvFileLine("12,3,4,\"测试 哈\",sdf ,\"测试\"\" 哈\", sdf,")));
        Assert.assertEquals("String[1996, Jep, grand chaeds , must sell\nair sdf sdf, loader, , \", 47900.00]", StringUtils.toString(CsvFile.splitCsvFileLine("1996,Jep,grand chaeds ,\"must sell\nair sdf sdf, loader\",\"\",\"\"\"\",47900.00")));
    }

    @Test
    public void testSplitCsvFileLineStringListOfString() {
        ArrayList<String> list = new ArrayList<String>();
        CsvFile.splitCsvFileLine("", list);
        Assert.assertEquals("ArrayList[]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("1", list);
        Assert.assertEquals("ArrayList[1]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("12", list);
        Assert.assertEquals("ArrayList[12]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("12,", list);
        Assert.assertEquals("ArrayList[12, ]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("12,3,", list);
        Assert.assertEquals("ArrayList[12, 3, ]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("12,3,4", list);
        Assert.assertEquals("ArrayList[12, 3, 4]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("12,3,4,\"测试 哈\",sdf ,\"测试\"\" 哈\", sdf,", list);
        Assert.assertEquals("ArrayList[12, 3, 4, 测试 哈, sdf , 测试\" 哈,  sdf, ]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("1996,Jep,grand chaeds ,\"must sell\nair sdf sdf, loader\",\"\",\"\"\"\",47900.00", list);
        Assert.assertEquals("ArrayList[1996, Jep, grand chaeds , must sell\nair sdf sdf, loader, , \", 47900.00]", StringUtils.toString(list));
    }

    @Test
    public void testJoinCsvFields() {
        Assert.assertEquals(",, ,\"this is ,  good dear '' ; \"", CsvFile.joinFields(null, "", " ", "this is ,  good dear '' ; "));
    }

    @Test
    public void testreplaceFieldValue() {
        Assert.assertEquals("1995,Jep1,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",100", CsvFile.replaceFieldValue("1996,Jep1,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",100", 1, "1995"));
        Assert.assertEquals("1995,ceshi,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",100", CsvFile.replaceFieldValue("1995,Jep1,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",100", 2, "ceshi"));
        Assert.assertEquals("1995,Jep1,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",800", CsvFile.replaceFieldValue("1995,Jep1,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",100", 7, "800"));
        Assert.assertEquals("1995,Jep1,grand chaeds ,ceshi,\"\",\"\"\"\",800", CsvFile.replaceFieldValue("1995,Jep1,grand chaeds ,\"ceshi\",\"\",\"\"\"\",800", 4, "ceshi"));
        Assert.assertNull(CsvFile.replaceFieldValue(null, 4, "ceshi"));

        try {
            Assert.assertEquals("1995,Jep1,grand chaeds ,ceshi,\"\",\"\"\"\",800", CsvFile.replaceFieldValue("1995,Jep1,grand chaeds ,\"ceshi\",\"\",\"\"\"\",800", 4, null));
            Assert.fail();
        } catch (NullPointerException e) {
            Assert.assertTrue(true);
        }

        try {
            Assert.assertEquals("1995,Jep1,grand chaeds ,ceshi,\"\",\"\"\"\",800", CsvFile.replaceFieldValue("1995,Jep1,grand chaeds ,\"ceshi\",\"\",\"\"\"\",800", 0, ""));
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("0", e.getMessage());
        }
    }

    @Test
    public void test1() throws IOException {
        String charsetName = "UTF-8";
        File file = this.createfile();

        System.out.println("文件内容:");
        String c = "";
        c += "1996,Jep1,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",100\n";
        c += "1997,Jep2,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",200\n";
        c += "1998,Jep3,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",300\n";
        c += "1999,Jep4,grand chaeds ,\"must sell\nair sdf sdf, loader\",\"\",\"\"\"\",400\n";
        FileUtils.write(file, charsetName, false, c);
        System.out.println(FileUtils.readline(file, charsetName, 0));

        CsvFile csvfile = new CsvFile(file);
        csvfile.setCharsetName(charsetName);

        csvfile = csvfile.clone();
        TextTableFileReader in = csvfile.getReader(100000);
        Assert.assertEquals(7, csvfile.getColumn());

        int row = 0;
        TextTableLine line;
        while ((line = in.readLine()) != null) {
            row++;
            Assert.assertEquals(1995 + row, Integer.parseInt(line.getColumn(1)));
            Assert.assertEquals("Jep" + row, line.getColumn(2));
            Assert.assertEquals(row * 100, Integer.parseInt(line.getColumn(7)));
        }
        in.close();
    }

    @Test
    public void test2() throws IOException {
        String charsetName = "UTF-8";
        File file = this.createfile();

        System.out.println("文件内容:");
        String c = "";
        c += "1996,Jep1,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",100\n";
        c += "1997,Jep2,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",200\n";
        c += "1998,Jep3,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",300\n";
        c += "1999,Jep4,grand chaeds ,\"must sell\nair sdf sdf, loader\",\"\",\"\"\"\",400\n";
        FileUtils.write(file, charsetName, false, c);
        System.out.println(FileUtils.readline(file, charsetName, 0));

        CsvFile csvfile = new CsvFile();
        csvfile.setAbsolutePath(file.getAbsolutePath());
        csvfile.setCharsetName(charsetName);
        TextTableFileReader in = csvfile.getReader(100);
        Assert.assertEquals(7, csvfile.getColumn());

        int row = 0;
        TextTableLine line;
        while ((line = in.readLine()) != null) {
            row++;
            Assert.assertEquals(1995 + row, Integer.parseInt(line.getColumn(1)));
            Assert.assertEquals("Jep" + row, line.getColumn(2));
            Assert.assertEquals(row * 100, Integer.parseInt(line.getColumn(7)));
        }
        in.close();
    }

    @Test
    public void test3() throws Exception {
        EasyBeanContext context = new EasyBeanContext();
        String charsetName = "UTF-8";
        File file = this.createfile();

        System.out.println("文件内容:");
        String c = "";
        c += "1996,Jep1,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",100\n";
        c += "1997,Jep2,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",200\n";
        c += "1998,Jep3,grand chaeds ,\"must sellair sdf sdf, loader\",\"\",\"\"\"\",300\n";
        c += "1999,Jep4,grand chaeds ,\"must sell\nair sdf sdf, loader\",\"\",\"\"\"\",400\n";
        FileUtils.write(file, charsetName, false, c);
        System.out.println(FileUtils.readline(file, charsetName, 0));

        CsvFile csvfile = new CsvFile(file);
        csvfile.setCharsetName(charsetName);
        TextTableFileWriter out = csvfile.getWriter(true, 100);

        final List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("\"6\"");
        list.add("7");

        TextTableLine line = new TextTableLine() {
            public String getContent() {
                return null;
            }

            public void setContext(String line) {
            }

            public boolean isColumnBlank(int position) {
                return false;
            }

            public String getColumn(int position) {
                return list.get(position - 1);
            }

            public void setColumn(int position, String value) {
            }

            public int getColumn() {
                return 7;
            }

            public String getLineSeparator() {
                return null;
            }

            public long getLineNumber() {
                return 0;
            }
        };
        out.addLine(line);
        out.flush();
        out.close();

        System.out.println("增加行之后: ");
        System.out.println(FileUtils.readline(file, charsetName, 0));

        long rows = new TextTableFileCounter(context.getBean(ThreadSource.class), 2).execute(file, charsetName);
        Assert.assertEquals("1,2,3,4,5,\"\"\"6\"\"\"7", FileUtils.readline(file, charsetName, rows));
    }

}
