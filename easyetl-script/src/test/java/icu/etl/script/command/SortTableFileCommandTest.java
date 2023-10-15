package icu.etl.script.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import icu.etl.io.CommonTextTableFile;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableLine;
import icu.etl.sort.TableFileSortContext;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.JVM)
public class SortTableFileCommandTest {

    /**
     * 测试倒序排序
     *
     * @throws IOException
     * @throws ScriptException
     */
    @Test
    public void test1() throws IOException, ScriptException {
        CommonTextTableFile file = new CommonTextTableFile();
        file.setDelimiter(",");
        file.setCharsetName(StringUtils.CHARSET);

        File f = this.getTestFile(file);
        file.setAbsolutePath(f.getAbsolutePath());

        TableFileSortContext obj = new TableFileSortContext();
        obj.setWriterBuffer(50);
        obj.setMaxRows(10000);
        obj.setDeleteFile(true);
        obj.setThreadNumber(3);
        obj.setFileCount(3);
        obj.setReaderBuffer(8192);
        obj.setKeepSource(false);

        ScriptEngineManager m = new ScriptEngineManager();
        ScriptEngine e = m.getEngineByExtension("etl");
        try {
            e.eval("sort table file " + f.getAbsolutePath() + " of txt modified by thread=3 maxrow=10000 maxfile=3 covsrc order by int(1) asc,2 desc");
            this.checkFile(file);
        } catch (Exception e1) {
            e1.printStackTrace();
            assertTrue(false);
        } finally {
            e.eval("exit 0");
        }

//		s.sort(file, "1 asc");
//		this.checkFile(file);
//		
//		s.sort(file, "int(1) asc");
//		this.checkFile(file);
    }

    protected void checkFile(CommonTextTableFile file) throws NumberFormatException, IOException {
        int i = 0;
        TextTableFileReader in = file.getReader(IO.FILE_BYTES_BUFFER_SIZE);
        TextTableLine line = null;
        while ((line = in.readLine()) != null) {
            if (++i != Integer.parseInt(StringUtils.trimBlank(line.getColumn(1)))) {
                assertTrue(i + " != " + Integer.parseInt(StringUtils.trimBlank(line.getColumn(1))), false);
            }

            if (i + 19 != Integer.parseInt(StringUtils.trimBlank(line.getColumn(20)))) {
                assertTrue((i + 19) + " != " + StringUtils.trimBlank(line.getColumn(20)), false);
            }
        }
        in.close();
    }

    protected File getTestFile(TextTableFile file) throws IOException {
        File parent = FileUtils.getTempDir(TextTableFile.class);
        FileUtils.createDirectory(parent);

        File dir = new File(parent, Dates.format08(new Date()));
        FileUtils.createDirectory(dir);
        File f0 = new File(dir, "SortTableFile" + Dates.format17(new Date()) + ".txt");

        FileUtils.delete(f0);
        FileUtils.createFile(f0);

        FileWriter out = new FileWriter(f0);
        for (int i = 50000; i > 0; i--) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 20; j++) {
                sb.append(StringUtils.right(i + j, 8, ' '));
                sb.append(file.getDelimiter());
            }
            out.write(sb + String.valueOf(FileUtils.lineSeparator));

            if (i % 20 == 0) {
                out.flush();
            }
        }
        out.flush();
        out.close();
        System.out.println(f0.getAbsolutePath());
        return f0;
    }

}
