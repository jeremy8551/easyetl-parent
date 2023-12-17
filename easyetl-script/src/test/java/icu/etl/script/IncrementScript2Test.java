package icu.etl.script;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import icu.etl.ProjectPom;
import icu.etl.cn.ChineseRandom;
import icu.etl.io.BufferedLineWriter;
import icu.etl.util.ArrayUtils;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.Settings;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * 测试设置临时目录功能
 */
public class IncrementScript2Test {

    public synchronized File[] getTempFiles(int rows) throws IOException {
        File tempDir = FileUtils.getTempDir("test", IncrementScript3Test.class.getSimpleName(), StringUtils.toRandomUUID());

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
                buf.append("姓名").append(coldel);
                buf.append("Line").append(i).append(coldel);
                buf.append("身份证号").append(coldel);
                buf.append("手机号").append(coldel);
                buf.append(Dates.format19(start)).append(coldel);
                buf.append(Dates.format19(end)).append(coldel);
                for (int j = 1; j <= 20; j++) {
                    buf.append("Column").append(j).append(coldel);
                }
                oldOut.writeLine(buf.toString());
            }
        } finally {
            oldOut.close();
        }

        // 生成新文件
        BufferedLineWriter result = new BufferedLineWriter(resultfile, StringUtils.CHARSET);
        BufferedLineWriter newOut = new BufferedLineWriter(newfile, StringUtils.CHARSET);
        try {
            ChineseRandom cr = new ChineseRandom();
            StringBuilder buf = new StringBuilder(500);

            // 写入不变数据与变化数据，已删除数据
            for (int i = 1; i <= rows; i++) {
                boolean m = false;

                buf.setLength(0);
                buf.append("姓名").append(coldel);
                buf.append("Line").append(i).append(coldel);
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
            buf.append(cr.nextName()).append(coldel);
            buf.append("M1").append(coldel);
            buf.append(cr.nextIdCard()).append(coldel);
            buf.append(cr.nextMobile()).append(coldel);
            buf.append(Dates.format19(cr.nextBirthday(start, end))).append(coldel);
            buf.append(Dates.format19(cr.nextBirthday(start, end))).append(coldel);
            for (int j = 1; j <= 20; j++) {
                buf.append("Column").append(j).append(coldel);
            }
            newOut.writeLine(buf.toString());
            result.writeLine(buf.toString());
        } finally {
            newOut.close();
            result.close();
        }

        return new File[]{oldfile, newfile, resultfile};
    }

    @Test
    public void test() throws IOException, ScriptException {
        File[] files = this.getTempFiles(100000);
        File oldfile = files[0];
        File newfile = files[1];
        File resultfile = files[2];
        File logfile = new File(newfile.getParentFile(), FileUtils.changeFilenameExt(newfile.getName(), "log"));
        File incfile = new File(newfile.getParentFile(), "INC_" + newfile.getName());
        System.out.println(FileUtils.readline(newfile, StringUtils.CHARSET, 1));

        File dir = FileUtils.createDirectory(Settings.getUserHome(), "." + ProjectPom.getArtifactID(), "inc");
        FileUtils.assertClearDirectory(dir);

        // 当前目录
        System.out.println("新文件: " + newfile);
        System.out.println("旧文件: " + oldfile);
        System.out.println("增量文件: " + incfile.getAbsolutePath());
        System.out.println("日志文件: " + logfile.getAbsolutePath());
        System.out.println("正确文件: " + resultfile.getAbsolutePath());

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("etl");
        try {
            // 设置命令中使用的文件路径与索引字段位置信息
            engine.eval("set newfile='" + newfile.getAbsolutePath() + "'");
            engine.eval("set oldfile='" + oldfile.getAbsolutePath() + "'");
            engine.eval("set incfile='" + incfile.getAbsolutePath() + "'");
            engine.eval("set index='2'");
            engine.eval("set compare=");

            // 增量剥离命令
            String inccmd = "";
            inccmd += "extract increment compare ";
            inccmd += " $newfile of txt modified by index=$index temp=" + dir.getAbsolutePath();
            inccmd += " and";
            inccmd += " $oldfile of txt modified by index=$index temp=" + dir.getAbsolutePath();
            inccmd += " write new and upd and del into $incfile ";
            inccmd += " write log into " + logfile.getAbsolutePath();
            engine.eval(inccmd);
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            engine.eval("exit 0");
        }

        Assert.assertTrue(dir.exists());
        Assert.assertTrue(ArrayUtils.isEmpty(dir.listFiles()));

        File parent = newfile.getParentFile();
        Assert.assertEquals(5, FileUtils.array(parent.listFiles()).length);

        // 判断剥离增量结果文件与正确文件是否相等
        long n = FileUtils.equalsIgnoreLineSeperator(incfile, StringUtils.CHARSET, resultfile, StringUtils.CHARSET, 0);
        if (n != 0) {
            String msg = "第 " + n + " 行不同!" + FileUtils.lineSeparator;
            msg += FileUtils.readline(incfile, StringUtils.CHARSET, n) + FileUtils.lineSeparator; // 读取文件中的指定行内容
            msg += FileUtils.readline(resultfile, StringUtils.CHARSET, n); // 读取文件中的指定行内容
            System.err.println(msg);
            Assert.fail();
        }
    }

}
