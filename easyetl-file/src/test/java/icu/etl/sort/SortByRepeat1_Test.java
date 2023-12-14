package icu.etl.sort;

import java.io.File;
import java.io.IOException;
import java.util.Random;

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

/**
 * 文件中有重复数据，且重复是连续在一起的（间隔小）
 */
public class SortByRepeat1_Test {
    private final static Log log = LogFactory.getLog(SortByRepeat1_Test.class);

    @Test
    public void test() throws Exception {
        TimeWatch watch = new TimeWatch();
        EasyBeanContext ioc = new EasyBeanContext("debug:sout+");
        TextTableFile txt = ioc.getBean(TextTableFile.class, "txt");

        File tmpfile = FileUtils.createTempFile(".txt");
        FileUtils.delete(tmpfile);
        FileUtils.createFile(tmpfile);
        txt.setAbsolutePath(tmpfile.getAbsolutePath());

        Random random = new Random();
        int line = random.nextInt(500);
        int next = line + random.nextInt(100);
        String copy = null;

        log.info("复制文件 {} 中第 {} 行到第 {} 行", tmpfile.getAbsoluteFile(), line, next);

        TextTableFileWriter out = txt.getWriter(false, IO.FILE_BYTES_BUFFER_SIZE);
        for (int i = 1; i <= 50000; i++) {
            // 在指定行写入重复行
            if (i == next) {
                out.addLine(copy);
                continue;
            }

            StringBuilder buf = new StringBuilder();
            for (int j = 0; j < 20; j++) {
                buf.append(StringUtils.right(i + j, 8, ' '));
                buf.append(txt.getDelimiter());
            }
            out.addLine(buf.toString());

            // 复制指定行内容
            if (i == line) {
                copy = buf.toString();
            }

            if (i % 200 == 0) {
                out.flush();
            }
        }
        out.flush();
        out.close();

        log.info("");
        log.info("第 {} 行: {}", line, FileUtils.readline(tmpfile, StringUtils.CHARSET, line));
        log.info("第 {} 行: {}", next, FileUtils.readline(tmpfile, StringUtils.CHARSET, next));

        log.info("");
        log.info("创建临时文件 {} 用时: {}", tmpfile.getAbsolutePath(), watch.useTime());

        // ---------------------------------------------------------------------------------------------------------------------------------------------------------
        //
        // 对文件进行排序
        //
        // ---------------------------------------------------------------------------------------------------------------------------------------------------------

        File txtfile = new File(txt.getAbsolutePath());
        File bakfile = new File(txtfile.getParentFile(), FileUtils.changeFilenameExt(txtfile.getName(), "bak"));
        FileUtils.deleteFile(bakfile);
        Assert.assertTrue(FileUtils.copy(txtfile, bakfile));

        TableFileSortContext context = new TableFileSortContext();
        context.setThreadSource(ioc.getBean(ThreadSource.class));
        context.setFileCount(2);
        context.setThreadNumber(2);
        context.setDeleteFile(true);
        context.setKeepSource(true);
        context.setReaderBuffer(1024 * 1024 * 100);
        context.setWriterBuffer(1024 * 1024 * 100);
        context.setMaxRows(1000);
        context.setWriterBuffer(800);
        context.setRemoveLastField(false);

        TableFileDeduplicateSorter sorter = new TableFileDeduplicateSorter(context);
        try {
            File sort = sorter.execute(ioc, txt, "int(1) desc");
            Assert.fail();
        } catch (IOException e) {
            String message = e.getLocalizedMessage();
            log.info(message);
            String[] array = StringUtils.splitByBlank(message);
            Assert.assertTrue(StringUtils.inArrayIgnoreCase(String.valueOf(line), array));
            Assert.assertTrue(StringUtils.inArrayIgnoreCase(String.valueOf(next), array));
        }

    }

}
