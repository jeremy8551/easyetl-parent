package icu.etl.script;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableLine;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.ioc.EasyContext;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.JVM) // 控制JUnit单元测试方法执行顺序
public class SortTableFileCommandTest {

    /**
     * 测试倒序排序
     */
    @Test
    public void test1() throws IOException, ScriptException {
        EasyContext context = new EasyBeanContext();
        TextTableFile file = context.getBean(TextTableFile.class, "txt");
        file.setDelimiter(",");
        File txtfile = this.createfile(file);
        file.setAbsolutePath(txtfile.getAbsolutePath());

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("etl");
        try {
            engine.eval("sort table file " + txtfile.getAbsolutePath() + " of txt modified by thread=3 maxrow=10000 maxfile=3 covsrc order by int(1) asc,2 desc");
            this.checkFile(file);
        } catch (Exception e1) {
            e1.printStackTrace();
            Assert.fail();
        } finally {
            engine.eval("exit 0");
        }

//		s.sort(file, "1 asc");
//		this.checkFile(file);
//		
//		s.sort(file, "int(1) asc");
//		this.checkFile(file);
    }

    protected void checkFile(TextTableFile file) throws NumberFormatException, IOException {
        int i = 0;
        TextTableFileReader in = file.getReader(IO.FILE_BYTES_BUFFER_SIZE);
        TextTableLine line;
        while ((line = in.readLine()) != null) {
            if (++i != Integer.parseInt(StringUtils.trimBlank(line.getColumn(1)))) {
                Assert.fail(i + " != " + Integer.parseInt(StringUtils.trimBlank(line.getColumn(1))));
            }

            if (i + 19 != Integer.parseInt(StringUtils.trimBlank(line.getColumn(20)))) {
                Assert.fail((i + 19) + " != " + StringUtils.trimBlank(line.getColumn(20)));
            }
        }
        in.close();
    }

    protected File createfile(TextTableFile tableFile) throws IOException {
        File file = FileUtils.createTempFile("SortTableFile.txt");
        FileUtils.clearDirectory(file.getParentFile());
        FileUtils.createFile(file);

        FileWriter out = new FileWriter(file);
        StringBuilder buf = new StringBuilder();
        for (int i = 50000; i > 0; i--) {
            for (int j = 0; j < 20; j++) {
                buf.append(StringUtils.right(i + j, 8, ' '));
                buf.append(tableFile.getDelimiter());
            }
            buf.append(tableFile.getLineSeparator());
            out.write(buf.toString());
            buf.setLength(0);

            if (i % 20 == 0) {
                out.flush();
            }
        }
        out.flush();
        out.close();

        System.out.println("sort file: " + file.getAbsolutePath());
        return file;
    }

}
