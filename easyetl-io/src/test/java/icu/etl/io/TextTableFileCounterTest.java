package icu.etl.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import icu.etl.cn.ChineseRandom;
import icu.etl.log.STD;
import icu.etl.printer.Progress;
import icu.etl.printer.StandardPrinter;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.Settings;
import icu.etl.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TextTableFileCounterTest {

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

        File dir = new File(FileUtils.getTempDir(FileUtils.class), "单元测试");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("创建目录 " + dir.getAbsolutePath() + " 失败!");
        } else {
            return new File(dir, name); // 返回一个临时文件信息
        }
    }

    /**
     * 生成一个2G表格文件 : <br>
     * 3445201 行数据, 共计 2147720745 字节, 文件 2.00 GB <br>
     * 文本最后一行没有换行符
     *
     * @param charsetName
     * @throws IOException
     */
    public void createBigFile(File file, String charsetName) throws IOException {
        int cache = 400; // 缓冲行数
        String str = "2G";
        long maxlength = 2 * 1024 * 1024 * 1024; // 文件最大值 2G

        // 已存在
        if (file.exists() && file.isFile() && file.length() >= maxlength) {
            return;
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
            long number = 0;
            StringBuilder buf = new StringBuilder();
            for (; ; ) { // 执行循环体
                if (lb) {
                    long currentLength = file.length();
                    process.print(currentLength, true);
                    if (currentLength >= maxlength) {
                        // 文件大小够了后需要中断
                        over = true;
                    }
                }

                buf.setLength(0);
                buf.append(++number).append(','); // 行号
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

            STD.out.info(file.getAbsolutePath() + " 共写入 " + number + " 行数据, 共计 " + file.length() + " 字节!");
            assertTrue(true);
        } catch (Throwable e) {
            e.printStackTrace();
            assertTrue(false);
        } finally {
            IO.close(out);
        }
    }

    /**
     * 统计文本文件行数 <br>
     * 使用逐行读取的方式计算文本文件行数
     *
     * @param file        文件
     * @param charsetName 文件字符集, 为空时取操作系统默认值
     * @return
     * @throws IOException
     */
    public static int countTextFileLines(File file, String charsetName) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException(StringUtils.toString(file));
        }
        if (StringUtils.isBlank(charsetName)) {
            charsetName = StringUtils.CHARSET;
        }

        BufferedReader in = IO.getBufferedReader(file, charsetName);
        try {
            int count = 0;
            while (in.readLine() != null) {
                count++;
            }
            return count;
        } catch (Throwable e) {
            throw new RuntimeException("countTextFileLines(" + file + ", " + charsetName + ")", e);
        } finally {
            IO.close(in);
        }
    }

    @Test
    public void testCalcTextFileLinesFile() throws IOException {
        File file = getFile();

        FileUtils.write(file, StringUtils.CHARSET, false, (CharSequence) null);
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 0);

        FileUtils.write(file, StringUtils.CHARSET, false, "");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 0);

        FileUtils.write(file, StringUtils.CHARSET, false, " ");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 1);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 1);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n1");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 2);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n1\n");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 2);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\r\n1\r\n");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 2);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n1\r\n");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 2);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n2\r\n3\r");
        assertEquals(new TextTableFileCounter().execute(file, StringUtils.CHARSET), 3);

        File f = new File(Settings.getUserHome(), "TEST_FILE_BIG.txt");
        String fc = StringUtils.CHARSET;
        this.createBigFile(f, fc);
        assertEquals(new TextTableFileCounter().execute(f, fc), 3445201);
        STD.out.info("分段统计完毕 ..");

        assertEquals(countTextFileLines(f, fc), 3445201);
        STD.out.info("串行统计完毕 ..");
    }

    @Test
    public void testCountTextFileLinesFile() throws IOException {
        File file = getFile();

        FileUtils.write(file, StringUtils.CHARSET, false, (CharSequence) null);
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 0);

        FileUtils.write(file, StringUtils.CHARSET, false, "");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 0);

        FileUtils.write(file, StringUtils.CHARSET, false, " ");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 1);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 1);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n1");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 2);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n1\n");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 2);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\r\n2\r\n");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 2);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n2\r\n");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 2);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n2\r\n3\r");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 3);

        FileUtils.write(file, StringUtils.CHARSET, false, "1\n2\r\n3\r4");
        assertTrue(new TextTableFileCounter().execute(file, StringUtils.CHARSET) == 4);
    }
}
