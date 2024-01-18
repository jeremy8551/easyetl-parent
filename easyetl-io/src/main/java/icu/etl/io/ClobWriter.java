package icu.etl.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 读取 Clob 对象写入到文件或输出流
 *
 * @author jeremy8551@qq.com
 * @createtime 2017-12-14
 */
public class ClobWriter {

    /**
     * Clob
     */
    protected Clob clob;

    /**
     * 初始化
     */
    public ClobWriter() {
    }

    /**
     * 初始化
     */
    public ClobWriter(Clob clob) {
        this.clob = clob;
    }

    /**
     * 返回 Clob
     *
     * @return Clob对象
     */
    public Clob getClob() {
        return clob;
    }

    /**
     * 设置Clob
     *
     * @param clob Clob对象
     */
    public void setClob(Clob clob) {
        this.clob = clob;
    }

    /**
     * 将 Clob 写入文件
     *
     * @param file        文件
     * @param charsetName 文件的字符集编码
     * @param append      true表示追加方式写入文件
     * @param buffer      缓存大小
     * @param endStr      结尾处写入信息 (可以是null或空字符串)
     * @throws IOException  输入输出流错误
     * @throws SQLException 数据库错误
     */
    public void toFile(File file, String charsetName, boolean append, int buffer, String endStr) throws Exception {
        FileUtils.assertCreateFile(file);
        OutputStreamWriter out = IO.getFileWriter(file, charsetName, append);
        try {
            this.toWriter(this.clob, out, buffer);

            if (StringUtils.isNotBlank(endStr)) {
                out.write(endStr);
            }
            out.flush();
        } finally {
            IO.close(out);
        }
    }

    public String toString() {
        if (this.clob == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        Reader in = null;
        try {
            in = this.clob.getCharacterStream();
            if (in != null) {
                char[] cache = new char[128];
                for (int len = 0; (len = in.read(cache)) != -1; ) {
                    buf.append(cache, 0, len);
                }
            }
            return buf.toString();
        } catch (Throwable e) {
            throw new RuntimeException(ResourcesUtils.getMessage("io.standard.output.msg003"), e);
        } finally {
            IO.close(in);
        }
    }

    /**
     * 从 Clob 中读取字符写入到输出流
     *
     * @param clob Clob对象
     * @param out  输出流
     * @param size 缓存大小
     * @throws IOException  输入输出流错误
     * @throws SQLException 数据库错误
     */
    public void toWriter(Clob clob, Writer out, int size) throws Exception {
        if (clob != null) {
            Reader in = clob.getCharacterStream();
            if (in != null) {
                try {
                    this.write(in, out, size);
                } finally {
                    in.close();
                }
            }
        }
    }

    /**
     * 从输出流中读取字符并写入到输出流中
     *
     * @param in   输入流
     * @param out  输出流
     * @param size 缓存大小
     * @throws IOException 输入输出流错误
     */
    protected void write(Reader in, Writer out, int size) throws IOException {
        char[] buffer = new char[size];
        for (int len; (len = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, len);
            out.flush();
        }
        out.flush();
    }

}