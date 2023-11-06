package icu.etl.sort;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import javax.script.ScriptException;

import icu.etl.io.BufferedLineReader;
import icu.etl.io.CommonTextTableFile;
import icu.etl.io.TextTableFile;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;
import org.junit.Assert;
import org.junit.Test;

public class TableFileDeduplicateSorterTest {

    protected File getTestFile(TextTableFile file) throws IOException {
        File parent = new File("/Users/user/Desktop");
//        File parent = Files.getTempDir(TextTableFile.class);
        FileUtils.createDirectory(parent);

        File dir = new File(parent, "test");
        FileUtils.clearDirectory(dir);
        FileUtils.createDirectory(dir);
        File f = new File(dir, "SortTableFile" + Dates.format17(new Date()) + "_" + StringUtils.toRandomUUID() + ".txt");
        FileUtils.delete(f);
        FileUtils.createFile(f);

        OutputStreamWriter out = IO.getFileWriter(f, "utf-8", false);
        for (int i = 50000; i > 0; i--) {
            StringBuilder buf = new StringBuilder();
            for (int j = 0; j < 20; j++) {
                buf.append(StringUtils.right(i + j, 8, ' '));
                buf.append(file.getDelimiter());
            }
            out.write(buf + String.valueOf(FileUtils.lineSeparator));

//            System.out.println(buf.toString());
            if (i % 20 == 0) {
                out.flush();
            }
        }
        out.flush();
        out.close();
        System.out.println(f.getAbsolutePath());
        return f;
    }

    @Test
    public void test() throws IOException, ScriptException {
        EasyBeanContext context = new EasyBeanContext();
//        File dir = new File("/Users/user/Desktop/test");
        CommonTextTableFile file = new CommonTextTableFile();
        file.setDelimiter(",");
        file.setCharsetName(StringUtils.CHARSET);
        String filepath = this.getTestFile(file).getAbsolutePath();
        file.setAbsolutePath(filepath);

        TimeWatch watch = new TimeWatch();
        System.out.println("初始化: " + watch.useTime());

        TableFileDeduplicateSorter sorter = new TableFileDeduplicateSorter();
        TableFileSortContext cxt = sorter.getContext();
        cxt.setFileCount(4);
        cxt.setThreadNumber(3);
        cxt.setDeleteFile(false);
        cxt.setKeepSource(true);
        cxt.setReaderBuffer(1024 * 1024 * 100);
        cxt.setWriterBuffer(1024 * 1024 * 100);
        cxt.setMaxRows(10000);
        cxt.setWriterBuffer(800);

        System.out.println("合并前文件: " + file.getAbsolutePath() + ", 列数: " + file.getColumn());
        watch.start();
        File sort = sorter.sort(context, file, "1 asc");
        System.out.println("合并后文件: " + sort.getAbsolutePath() + ", 用时: " + watch.useTime());

        // 只加载一次数据文件
        CommonTextTableFile mergefile = file.clone();
        mergefile.setAbsolutePath(sort.getAbsolutePath());
        cxt.setDeleteFile(true);
        cxt.setKeepSource(true);
        cxt.setMaxRows((int) file.getFile().length());
        watch.start();
        File file1 = sorter.sort(context, mergefile, "1 desc");
        System.out.println("合并后文件: " + file1.getAbsolutePath() + ", 用时: " + watch.useTime());

        BufferedLineReader in2 = new BufferedLineReader(new File(filepath), mergefile.getCharsetName()); //
        BufferedLineReader in1 = new BufferedLineReader(file1, mergefile.getCharsetName());
        try {
            String line1, line2;
            while ((line1 = in1.readLine()) != null && (line2 = in2.readLine()) != null) {
                int i = line1.lastIndexOf(mergefile.getDelimiter());
                if (i != -1) {
                    int j = StringUtils.lastIndexOfStr(line1, mergefile.getDelimiter(), 0, i - 1, false);
                    if (j == -1) {
                        Assert.fail();
                    } else {
                        Assert.assertEquals(line1.substring(0, j), line2);
                    }
                } else {
                    Assert.fail();
                }
            }

            Assert.assertNull(in1.readLine());
            Assert.assertNull(in2.readLine());
        } finally {
            in1.close();
            in2.close();
        }
    }

}
