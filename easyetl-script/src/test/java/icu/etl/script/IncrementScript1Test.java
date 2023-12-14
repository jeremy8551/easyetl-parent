package icu.etl.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.IOException;
import java.util.Random;

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
 * 增量剥离测试：第一个与最后一个字段作为联合唯一索引, 测试有重复数据时测试是否能正确抛出异常
 */
public class IncrementScript1Test {
    private final static Log log = LogFactory.getLog(IncrementScript1Test.class);

    @Test
    public void test() throws IOException {
        TimeWatch watch = new TimeWatch();
        EasyBeanContext ioc = new EasyBeanContext("info:sout+");
        TextTableFile txt = ioc.getBean(TextTableFile.class, "txt");

        File parent = FileUtils.getTempDir("test", IncrementScript1Test.class.getSimpleName());
        FileUtils.assertClearDirectory(parent);
        File tmpfile = FileUtils.createNewFile(parent, ".txt");
        FileUtils.delete(tmpfile);
        FileUtils.createFile(tmpfile);
        txt.setAbsolutePath(tmpfile.getAbsolutePath());

        Random random = new Random();
        int line = random.nextInt(500);
        int next = 40000 + random.nextInt(9000);
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

        int column = txt.countColumn(); // 总列数

        File newfile = FileUtils.allocate(tmpfile.getParentFile(), "NEWFILE.txt");
        File oldfile = FileUtils.allocate(tmpfile.getParentFile(), "OLDFILE.txt");

        Assert.assertTrue(FileUtils.copy(tmpfile, newfile));
        Assert.assertTrue(FileUtils.copy(tmpfile, oldfile));
        FileUtils.assertDelete(tmpfile);

        File logfile = new File(newfile.getParentFile(), FileUtils.changeFilenameExt(newfile.getName(), "log"));
        File incfile = new File(newfile.getParentFile(), "INC_" + newfile.getName());

        log.info("");
        log.info(FileUtils.readline(newfile, StringUtils.CHARSET, 1));
        log.info(FileUtils.readline(oldfile, StringUtils.CHARSET, 1));
        log.info("");
        log.info("新文件: " + newfile);
        log.info("旧文件: " + oldfile);
        log.info("增量文件: " + incfile.getAbsolutePath());
        log.info("日志文件: " + logfile.getAbsolutePath());
        log.info("");

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("etl");
        try {
            // 设置命令中使用的文件路径与索引字段位置信息
            engine.eval("set newfile='" + newfile.getAbsolutePath() + "'");
            engine.eval("set oldfile='" + oldfile.getAbsolutePath() + "'");
            engine.eval("set incfile='" + incfile.getAbsolutePath() + "'");
            engine.eval("set index='1," + column + "'");
            engine.eval("set compare=");

            // 增量剥离命令
            String inccmd = "";
            inccmd += "extract increment compare ";
            inccmd += " $newfile of txt modified by index=$index ";
            inccmd += " and";
            inccmd += " $oldfile of txt modified by index=$index ";
            inccmd += " write new and upd and del into $incfile ";
            inccmd += " write log into " + logfile.getAbsolutePath();
            engine.eval(inccmd);
            Assert.fail();
        } catch (Throwable e) {
            String message = StringUtils.toString(e);
//            System.out.println(message);
            String[] array = StringUtils.splitByBlank(message);
            Assert.assertTrue(StringUtils.inArrayIgnoreCase(String.valueOf(line), array));
            Assert.assertTrue(StringUtils.inArrayIgnoreCase(String.valueOf(next), array));
//            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
