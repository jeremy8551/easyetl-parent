package icu.etl.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import icu.etl.cn.ChineseRandom;
import icu.etl.concurrent.ThreadSource;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.printer.Progress;
import icu.etl.printer.StandardPrinter;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.Settings;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextTableFileCounterTest {

    /**
     * 使用指定用户名创建一个文件
     */
    public File createfile() throws IOException {
        File dir = FileUtils.getTempDir("test" + TextTableFileCounter.class.getSimpleName());
        File file = new File(dir, TextTableFileCounter.class.getSimpleName() + StringUtils.toRandomUUID() + ".csv");
        FileUtils.createFile(file);
        System.out.println(TextTableFileCounter.class.getSimpleName() + " testfile: " + file.getAbsolutePath());
        return file;
    }

    @Test
    public void testCalcTextFileLinesFile() throws Exception {
        EasyBeanContext context = new EasyBeanContext();
        ThreadSource threadSource = context.getBean(ThreadSource.class);
        File file = this.createfile();

        FileUtils.write(file, StringUtils.CHARSET, false, (CharSequence) null);
        assertEquals(0, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "");
        assertEquals(0, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, " ");
        assertEquals(1, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n");
        assertEquals(1, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n1");
        assertEquals(2, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n1\n");
        assertEquals(2, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\r\n1\r\n");
        assertEquals(2, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n1\r\n");
        assertEquals(2, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n2\r\n3\r");
        assertEquals(new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET), 3);
    }

    @Test
    public void testCountTextFileLinesFile() throws Exception {
        EasyBeanContext context = new EasyBeanContext();
        ThreadSource threadSource = context.getBean(ThreadSource.class);
        File file = createfile();

        FileUtils.write(file, StringUtils.CHARSET, false, (CharSequence) null);
        assertEquals(0, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "");
        assertEquals(0, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, " ");
        assertEquals(1, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n");
        assertEquals(1, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n1");
        assertEquals(2, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n1\n");
        assertEquals(2, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\r\n2\r\n");
        assertEquals(2, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n2\r\n");
        assertEquals(2, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n2\r\n3\r");
        assertEquals(3, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n2\r\n3\r4");
        assertEquals(4, new TextTableFileCounter(threadSource, 2).execute(file, StringUtils.CHARSET));
    }

    @Test
    public void test110() throws Exception {
        EasyBeanContext context = new EasyBeanContext();
        ThreadSource threadSource = context.getBean(ThreadSource.class);
        File file = new File(Settings.getUserHome(), "TEST_FILE_BIG.txt");
        String charsetName = StringUtils.CHARSET;
        long rows = this.createBigFile(file, charsetName);
        assertEquals(rows, new TextTableFileCounter(threadSource, 2).execute(file, charsetName));
        System.out.println("并行统计完毕, 共计 " + rows + " 行!");
    }

    /**
     * 生成一个2G表格文件 : <br>
     * 3445201 行数据, 共计 2147720745 字节, 文件 2.00 GB <br>
     * 文本最后一行没有换行符
     *
     * @param file        文件
     * @param charsetName 字符集
     * @return 返回行数
     * @throws IOException 生成文件发生错误
     */
    public long createBigFile(File file, String charsetName) throws IOException {
        int cache = 400; // 缓冲行数
        String str = "2G";
        long maxlength = 2L * 1024 * 1024 * 1024; // 文件最大值 2G

        // 已存在
        if (file.exists() && file.isFile() && file.length() >= maxlength) {
            if (new Date(file.lastModified()).compareTo(Dates.parse(Dates.format10(new Date()))) < 0) {
                file.delete(); // 如果文件不是当前生成的，则删除重建
            } else {
                long rows = this.countTextFileLines(file, charsetName);
                System.out.println("串行统计完毕, 共计 " + rows + " 行!");
                return rows;
            }
        }
        System.out.println("数据文件最大值 " + str + ", 最大字节数: " + maxlength);

        // 配置进度输出接口
        StandardPrinter printer = new StandardPrinter();
        Progress process = new Progress(printer, "正在准备 " + str + " 文本文件 ${process}%, ${leftTime}", maxlength);

        Date start = Dates.parse("2000-01-01");
        Date end = new Date();

        // 生成随机信息
        boolean lb = false;
        boolean over = false;
        ChineseRandom random = new ChineseRandom();
        Random r = random.get();
        BufferedLineWriter out = new BufferedLineWriter(file, charsetName, cache);
        try {
            long rows = 0;
            StringBuilder buf = new StringBuilder();
            while (true) { // 执行循环体
                if (lb) {
                    long currentLength = file.length();
                    process.print(currentLength, true);
                    if (currentLength >= maxlength) {
                        // 文件大小够了后需要中断
                        over = true;
                    }
                }

                buf.setLength(0);
                buf.append(++rows).append(','); // 行号
                buf.append(StringUtils.toRandomUUID()).append(','); // 唯一编号
                buf.append(random.nextName()).append(','); // 姓名
                buf.append(random.nextIdCard()).append(','); // 身份证号
                buf.append(random.nextMobile()).append(','); // 手机号
                buf.append("999").append(',');

                // 添加状态信息
                int value = r.nextInt(4);
                switch (value) {
                    case 0:
                        buf.append("\"normal\"").append(',');
                        break;
                    case 1:
                        buf.append("\"overdue\"").append(',');
                        break;
                    case 2:
                        buf.append("\"finish\"").append(',');
                        break;
                    case 3:
                        buf.append("\"turnoff\"").append(',');
                        break;
                    default:
                        throw new IllegalArgumentException(String.valueOf(value));
                }

                // 添加随机账号
                buf.append("\"P");
                buf.append(Dates.format08(Dates.random(start, end)));
                buf.append("27922650                       \",");

                buf.append(Dates.format08(Dates.random(start, end))).append(',');
                buf.append(Dates.format08(Dates.random(start, end))).append(',');
                buf.append(Dates.format08(Dates.random(start, end))).append(',');

                buf.append('+').append(r.nextDouble() * 100000).append(',');
                buf.append('+').append(r.nextDouble() * 100000).append(',');
                buf.append('+').append(r.nextDouble() * 100000).append(',');

                buf.append(Dates.format08(Dates.random(start, end))).append(",,");
                buf.append("+000000000000000000.00,+000000000000000000.00,");
                buf.append('+').append(r.nextDouble() * 100000).append(',');
                buf.append("\"3\",20210413,,,,,\"04501               \",\"GD10000765215                           \",\"BA2019053000000195                      \",\"37010006                      \",\"鏉庡崥                                                        \",20210413,\"13070050            \",,,,+000000000000250000.00,+000000000000000099.68,+000000000000000000.00,");

                String lineSeparator = String.valueOf(FileUtils.lineSeparator);
                switch (r.nextInt(3)) {
                    case 0:
                        lineSeparator = "\n";
                        break;
                    case 1:
                        lineSeparator = "\r\n";
                        break;
                    case 2:
                        lineSeparator = "\r";
                        break;

                    default:
                        throw new IllegalArgumentException();
                }

                if (over) {
                    out.write(buf.toString());
                    break;
                } else {
                    // 将缓存写入文件
                    lb = out.writeLine(buf.toString(), lineSeparator);
                }
            }
            out.flush();

            System.out.println(file.getAbsolutePath() + " 共写入 " + rows + " 行数据, 共计 " + file.length() + " 字节!");
            assertEquals(rows, this.countTextFileLines(file, charsetName));
            System.out.println("串行统计完毕 ..");
            return rows;
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
            return 0;
        } finally {
            out.close();
        }
    }

    /**
     * 统计文本文件行数 <br>
     * 使用逐行读取的方式计算文本文件行数
     *
     * @param file        文件
     * @param charsetName 文件字符集, 为空时取操作系统默认值
     * @return 文本行数
     * @throws IOException 读取文件发生错误
     */
    public long countTextFileLines(File file, String charsetName) throws IOException {
        BufferedReader in = IO.getBufferedReader(file, charsetName, IO.READER_BUFFER_SIZE);
        try {
            long rows = 0;
            while (in.readLine() != null) {
                rows++;
            }
            return rows;
        } finally {
            in.close();
        }
    }

}
