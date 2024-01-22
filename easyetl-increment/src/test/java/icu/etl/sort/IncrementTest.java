package icu.etl.sort;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import icu.etl.concurrent.ThreadSourceImpl;
import icu.etl.increment.IncrementJob;
import icu.etl.increment.IncrementContext;
import icu.etl.increment.IncrementListenerImpl;
import icu.etl.increment.IncrementPositionImpl;
import icu.etl.io.BufferedLineWriter;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileWriter;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.printer.StandardFilePrinter;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.Numbers;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * 增量剥离测试：第一个与最后一个字段作为联合唯一索引，进行排序
 */
public class IncrementTest {
    private final static Log log = LogFactory.getLog(IncrementTest.class);

    public synchronized File[] getTempFiles(int rows) throws IOException {
        File tempDir = FileUtils.getTempDir("test", IncrementTest.class.getSimpleName());

        File newfile = FileUtils.allocate(tempDir, "NEWFILE.txt");
        FileUtils.createFile(newfile);

        File oldfile = FileUtils.allocate(tempDir, "OLDFILE.txt");
        FileUtils.createFile(oldfile);

        File resultfile = FileUtils.allocate(tempDir, "RESULT.txt");
        FileUtils.createFile(resultfile);

        Date start = Dates.parse("1960-01-01");
        Date end = new Date();
        String coldel = ",";

        // 写入旧文件
        BufferedLineWriter oldOut = new BufferedLineWriter(oldfile, StringUtils.CHARSET);
        try {
            StringBuilder buf = new StringBuilder(500);
            for (int i = 1; i <= rows; i++) {
                buf.setLength(0);
                buf.append("Line").append(i).append(coldel);
                buf.append("姓名").append(coldel);
                buf.append("身份证号").append(coldel);
                buf.append("手机号").append(coldel);
                buf.append(Dates.format19(start)).append(coldel);
                buf.append(Dates.format19(end)).append(coldel);
                for (int j = 1; j <= 20; j++) {
                    buf.append("Column").append(j).append(coldel);
                }
                buf.append("INDEX").append(i);
                oldOut.writeLine(buf.toString());
            }
        } finally {
            oldOut.close();
        }

        // 生成新文件
        BufferedLineWriter result = new BufferedLineWriter(resultfile, StringUtils.CHARSET);
        BufferedLineWriter newOut = new BufferedLineWriter(newfile, StringUtils.CHARSET);
        try {
            StringBuilder buf = new StringBuilder(500);

            // 写入不变数据与变化数据，已删除数据
            for (int i = 1; i <= rows; i++) {
                boolean m = false;

                buf.setLength(0);
                buf.append("Line").append(i).append(coldel);
                buf.append("姓名").append(coldel);
                buf.append("身份证号").append(coldel);
                buf.append("手机号").append(coldel);

                if (i == 1000 || i == 2002 || i == 4003) {
                    buf.append(coldel);
                    m = true;
                } else {
                    buf.append(Dates.format19(start)).append(coldel);
                }
                buf.append(Dates.format19(end)).append(coldel);

                for (int j = 1; j <= 20; j++) {
                    buf.append("Column").append(j).append(coldel);
                }

                buf.append("INDEX").append(i);

                // 删除 2001 行与 4000 行内容
                if (i == 2001 || i == 4000) {
                    result.writeLine(buf.toString());
                    continue;
                }

                // 写入变化记录
                if (m) {
                    result.writeLine(buf.toString());
                }

                newOut.writeLine(buf.toString());
            }

            // 写入一行新增数据
            buf.setLength(0);
            buf.append("M1").append(coldel);
            buf.append("姓名").append(coldel);
            buf.append("身份证号").append(coldel);
            buf.append("手机号").append(coldel);
            buf.append(Dates.format19(new Date())).append(coldel);
            buf.append(Dates.format19(new Date())).append(coldel);
            for (int j = 1; j <= 20; j++) {
                buf.append("Column").append(j).append(coldel);
            }
            buf.append("M1");
            newOut.writeLine(buf.toString());
            result.writeLine(buf.toString());
        } finally {
            newOut.close();
            result.close();
        }

        return new File[]{oldfile, newfile, resultfile};
    }

    @Test
    public void test() throws Exception {
        EasyBeanContext ioc = new EasyBeanContext("debug:sout+");
        File[] files = this.getTempFiles(100000);
        File oldfile = files[0];
        File newfile = files[1];
        File resultfile = files[2];
        File logfile = new File(newfile.getParentFile(), FileUtils.changeFilenameExt(newfile.getName(), "log"));
        File incfile = new File(newfile.getParentFile(), "INC_" + newfile.getName());

        String newFirstLine = FileUtils.readline(newfile, StringUtils.CHARSET, 1);
        String oldFirstLine = FileUtils.readline(oldfile, StringUtils.CHARSET, 1);

        Assert.assertEquals(newFirstLine, oldFirstLine);
        log.info("");
        log.info(newFirstLine);
        log.info(oldFirstLine);
        log.info("");

        // 当前目录
        log.info("新文件: " + newfile);
        log.info("旧文件: " + oldfile);
        log.info("增量文件: " + incfile.getAbsolutePath());
        log.info("日志文件: " + logfile.getAbsolutePath());
        log.info("正确文件: " + resultfile.getAbsolutePath());
        log.info("");

        // 读取文件类型
        TextTableFile file = ioc.getBean(TextTableFile.class, "txt");
        TextTableFile newtxtfile = file.clone();
        TextTableFile oldtxtfile = file.clone();
        TextTableFile inctxtfile = file.clone();

        // 设置文件
        newtxtfile.setAbsolutePath(newfile.getAbsolutePath());
        oldtxtfile.setAbsolutePath(oldfile.getAbsolutePath());
        inctxtfile.setAbsolutePath(incfile.getAbsolutePath());

        // 输出流
        TextTableFileWriter writer = inctxtfile.getWriter(false, IO.FILE_BYTES_BUFFER_SIZE);
        TextTableFileWriter newout = writer;
        TextTableFileWriter updout = writer;
        TextTableFileWriter delout = writer;

        // 总列数
        int column = newtxtfile.countColumn();
//        log.info("总列数: " + newtxtfile.countColumn());
//        log.info("总列数: " + oldtxtfile.countColumn());

        int[] newIndexPosition = new int[]{1, column};
        int[] oldIndexPosition = new int[]{1, column};
        int[] newComparePosition = this.toComparePositions(column, newIndexPosition);
        int[] oldComparePosition = this.toComparePositions(column, oldIndexPosition);
        IncrementPositionImpl position = new IncrementPositionImpl(newIndexPosition, oldIndexPosition, newComparePosition, oldComparePosition);

        IncrementContext context = new IncrementContext();
        context.setThreadSource(new ThreadSourceImpl());
        context.setName(newfile.getAbsolutePath());
        context.setNewFile(newtxtfile);
        context.setOldFile(oldtxtfile);
        context.setPosition(position);
        context.setNewWriter(newout);
        context.setUpdWriter(updout);
        context.setDelWriter(delout);
        context.setSortNewfile(true);
        context.setSortOldfile(true);
        context.setSortNewContext(new TableFileSortContext(ioc, null));
        context.setSortOldContext(new TableFileSortContext(ioc, null));
        context.setReplaceList(null);

        // 输出剥离增量日志
        StandardFilePrinter out = new StandardFilePrinter(logfile, StringUtils.CHARSET, false);
        context.setLogger(new IncrementListenerImpl(out, position, newtxtfile, oldtxtfile));

        IncrementJob inc = new IncrementJob(context);
        Assert.assertEquals(0, inc.execute());

        // 判断剥离增量结果文件与正确文件是否相等
        long n = FileUtils.equalsIgnoreLineSeperator(incfile, StringUtils.CHARSET, resultfile, StringUtils.CHARSET, 0);
        if (n != 0) {
            String msg = "第 " + n + " 行不同!" + FileUtils.lineSeparator;
            msg += FileUtils.readline(incfile, StringUtils.CHARSET, n) + FileUtils.lineSeparator; // 读取文件中的指定行内容
            msg += FileUtils.readline(resultfile, StringUtils.CHARSET, n); // 读取文件中的指定行内容
            System.err.println(msg);
            Assert.fail();
        }

        // 输出增量日志
        log.info("");
        log.info("增量日志内容如下: ");
        log.info(FileUtils.readline(logfile, StringUtils.CHARSET, 0));
    }

    /**
     * 转为对比位置信息
     *
     * @param column 总列数
     * @param indexs 主键位置数组
     * @return 数组
     */
    private int[] toComparePositions(int column, int[] indexs) {
        int[] array = new int[column - indexs.length]; // 排除主键字段
        for (int i = 0, j = 0; i < column; i++) {
            int position = i + 1;
            if (!Numbers.inArray(position, indexs)) {
                array[j++] = position;
            }
        }
        return array;
    }

}
