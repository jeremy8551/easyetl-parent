package icu.etl.sort;

import java.io.File;
import java.io.IOException;

import icu.etl.concurrent.ThreadSource;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileWriter;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;
import org.junit.Assert;
import org.junit.Test;

public class TableFileDeduplicateSorterTest {
    private final static Log log = LogFactory.getLog(TableFileDeduplicateSorterTest.class);

    protected void createTestFile(TextTableFile file) throws IOException {
        TimeWatch watch = new TimeWatch();
        File tmpfile = FileUtils.createTempfile(TableFileDeduplicateSorterTest.class, "txt");
        FileUtils.delete(tmpfile);
        FileUtils.createFile(tmpfile);
        file.setAbsolutePath(tmpfile.getAbsolutePath());

        TextTableFileWriter out = file.getWriter(false, IO.FILE_BYTES_BUFFER_SIZE);
        for (int i = 50000; i > 0; i--) {
            StringBuilder buf = new StringBuilder();
            for (int j = 0; j < 20; j++) {
                buf.append(StringUtils.right(i + j, 8, ' '));
                buf.append(file.getDelimiter());
            }
            out.addLine(buf.toString());

            if (i % 20 == 0) {
                out.flush();
            }
        }
        out.flush();
        out.close();

        log.info(tmpfile.getAbsolutePath() + ", 用时: " + watch.useTime());
    }

    @Test
    public void test() throws IOException {
        EasyBeanContext ioc = new EasyBeanContext("debug:sout+");

        TextTableFile file = ioc.getBean(TextTableFile.class, "txt");
        this.createTestFile(file);

        File txtfile = new File(file.getAbsolutePath());
        File bakfile = new File(txtfile.getParentFile(), FileUtils.changeFilenameExt(txtfile.getName(), "bak"));
        FileUtils.deleteFile(bakfile);
        Assert.assertTrue(FileUtils.copy(txtfile, bakfile));

        TableFileSortContext context = new TableFileSortContext();
        context.setThreadSource(ioc.getBean(ThreadSource.class));
        context.setFileCount(4);
        context.setThreadNumber(3);
        context.setDeleteFile(false);
        context.setKeepSource(true);
        context.setReaderBuffer(1024 * 1024 * 100);
        context.setWriterBuffer(1024 * 1024 * 100);
        context.setMaxRows(10000);
        context.setWriterBuffer(800);
        context.setRemoveLastField(true);

        log.info("排序前文件: " + file.getAbsolutePath() + ", 列数: " + file.getColumn());
        TableFileDeduplicateSorter sorter = new TableFileDeduplicateSorter(context);
        File sort = sorter.execute(ioc, file, "1 asc");
        log.info("排序后文件: " + sort.getAbsolutePath());

        // 再次排序
        TextTableFile sortfile = file.clone();
        sortfile.setAbsolutePath(sort.getAbsolutePath());
        sortfile.countColumn(); // 需要重新统计列数

        context.setDeleteFile(true);
        context.setKeepSource(true);
        context.setMaxRows((int) file.getFile().length());
        context.setRemoveLastField(true);

        File oldfile = sorter.execute(ioc, sortfile, "1 desc");
        log.info("再次排序后文件: " + oldfile.getAbsolutePath());
        log.info("");

        log.info(txtfile.getAbsolutePath());
        log.info(oldfile.getAbsolutePath());

        log.info(FileUtils.readline(txtfile, file.getCharsetName(), 1));
        log.info(FileUtils.readline(oldfile, file.getCharsetName(), 1));

        long l = FileUtils.equalsIgnoreLineSeperator(oldfile, file.getCharsetName(), txtfile, file.getCharsetName(), context.getReaderBuffer());
        Assert.assertEquals(0L, l);

    }

}
