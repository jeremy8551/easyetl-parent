package icu.etl.database.export.inernal;

import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import icu.etl.annotation.EasyBean;
import icu.etl.database.export.ExtractMessage;
import icu.etl.database.export.ExtractWriter;
import icu.etl.io.TableLine;
import icu.etl.io.TableWriter;
import icu.etl.io.TextTable;
import icu.etl.util.Ensure;
import icu.etl.util.NetUtils;

@EasyBean(name = "http", description = "卸载数据到用户浏览器")
public class HttpRequestWriter implements ExtractWriter {

    /** HTTP 响应信息输出接口 */
    private TableWriter writer;

    /** 下载后的文件名 */
    private String filename;

    /** 文件行数 */
    private long lineNumber;

    private ExtractMessage message;

    /**
     * 初始化
     *
     * @param request  HttpServletRequest 请求
     * @param response HttpServletResponse 响应
     * @param filename 下载后的文件名
     * @param table    文本表格
     * @param message  消息信息
     * @throws IOException 卸载数据发生错误
     */
    public HttpRequestWriter(Object request, Object response, String filename, TextTable table, ExtractMessage message) throws IOException {
        Ensure.notNull(request);
        Ensure.notNull(response);

        this.filename = filename;
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        res.reset();
        res.setContentType("APPLICATION/OCTET-STREAM");
        res.setHeader("Content-Disposition", "attachment; filename=\"" + NetUtils.encodeFilename(req, filename) + "\"");

        ServletOutputStream out = res.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, table.getCharsetName());
        this.writer = table.getWriter(writer, 0);
        this.message = message;
        this.message.setTarget("http://download/" + this.filename);
    }

    public void write(TableLine line) throws IOException {
        this.lineNumber++;
        this.writer.addLine(line);
    }

    public boolean rewrite() throws IOException {
        return false;
    }

    public void flush() throws IOException {
        if (this.writer != null) {
            this.writer.flush();
        }
    }

    public void close() throws IOException {
        if (this.writer != null) {
            this.writer.close();
            this.writer = null;

            this.message.setRows(this.lineNumber);
            this.message.setBytes(0);
        }

    }

}
