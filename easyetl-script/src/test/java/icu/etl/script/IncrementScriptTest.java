package icu.etl.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import icu.etl.cn.ChineseRandom;
import icu.etl.io.BufferedLineWriter;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class IncrementScriptTest {

    public synchronized File[] getTempFiles(int rows) throws IOException {
        File tempDir = FileUtils.getTempDir("test", IncrementScriptTest.class.getSimpleName());

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

        // 当前目录
        System.out.println("新文件: " + newfile);
        System.out.println("旧文件: " + oldfile);
        System.out.println("增量文件: " + incfile.getAbsolutePath());
        System.out.println("日志文件: " + logfile.getAbsolutePath());
        System.out.println("正确文件: " + resultfile.getAbsolutePath());

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("etl");
        try {
            engine.eval("echo 脚本引擎初始化完毕，执行增量剥离任务!");

            // 设置命令中使用的文件路径与索引字段位置信息
            engine.eval("set newfile='" + newfile.getAbsolutePath() + "'");
            engine.eval("set oldfile='" + oldfile.getAbsolutePath() + "'");
            engine.eval("set incfile='" + incfile.getAbsolutePath() + "'");
            engine.eval("set index='2'");
            engine.eval("set compare=");

            // 增量剥离命令
            String inccmd = "";
            inccmd += "extract increment compare ";
            inccmd += " $newfile of txt modified by index=$index keeptemp";
            inccmd += " and";
            inccmd += " $oldfile of txt modified by index=$index keeptemp";
            inccmd += " write new and upd and del into $incfile ";
            inccmd += " write log into " + logfile.getAbsolutePath();
            engine.eval(inccmd);
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            engine.eval("exit 0");
        }

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

    /**
     * 测试并发执行剥离增量任务
     */
    @Test
    public void testContainer() throws IOException, ScriptException {
        List<File> resultList = new ArrayList<File>();
        List<File> incList = new ArrayList<File>();

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("etl");
        try {
            engine.eval("echo 脚本引擎初始化完毕，执行增量剥离任务!");

            // 设置命令中使用的文件路径与索引字段位置信息
            engine.eval("set index='2'");
            engine.eval("set compare=");

            String cmd = "container to execute tasks in parallel using thread=3 begin " + FileUtils.lineSeparator;
            for (int i = 1; i <= 10; i++) {
                File[] files = this.getTempFiles(100000);
                File oldfile = files[0];
                FileUtils.createFile(oldfile);

                File newfile = files[1];
                FileUtils.createFile(newfile);

                File resultfile = files[2];
                FileUtils.createFile(resultfile);

                File logfile = new File(newfile.getParentFile(), FileUtils.changeFilenameExt(newfile.getName(), "log"));
                FileUtils.createFile(logfile);

                File incfile = new File(newfile.getParentFile(), "INC_" + newfile.getName());
                FileUtils.createFile(incfile);

                resultList.add(resultfile);
                incList.add(incfile);

                // 剥离增量命令
                cmd += "extract increment compare";
                cmd += " " + newfile.getAbsolutePath() + " of txt modified by index=$index ";
                cmd += " and";
                cmd += " " + oldfile.getAbsolutePath() + " of txt modified by index=$index ";
                cmd += " write new and upd and del into " + incfile.getAbsolutePath();
                cmd += " write log into " + logfile.getAbsolutePath();
                cmd += ";";
                cmd += FileUtils.lineSeparator;
            }
            cmd += "end" + FileUtils.lineSeparator;
            System.out.println(cmd);

            engine.eval(cmd);
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            engine.eval("exit 0");
        }

        for (int i = 0; i < resultList.size(); i++) {
            File resultfile = resultList.get(i);
            File incfile = incList.get(i);

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

}
