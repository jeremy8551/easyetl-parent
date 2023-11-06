package icu.etl.printer;

import java.io.File;
import java.io.IOException;
import java.text.Format;

import icu.etl.util.IO;

/**
 * 信息输出到文件的接口实现类 <br>
 * 使用 {@linkplain StandardPrinter#print(boolean)} 等方法将数据写入到文件中
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-12-24
 */
public class StandardFilePrinter extends StandardPrinter implements Printer {

    /** 日志文件绝对路径 */
    protected File file;

    /** 字符集 */
    protected String charsetName;

    /** true表示追加方式写入日志文件 */
    protected boolean append;

    /**
     * 初始化
     *
     * @param file        文件
     * @param charsetName 文件字符集
     * @param append      true表示将数据写入到文件末尾位置 false表示将数据写入文件起始位置（会覆盖原文件内容）
     * @param converter   类型转换器(用于将 Object 对象转为字符串, 为 null 时默认使用 {@linkplain Object#toString()})
     * @throws IOException
     */
    public StandardFilePrinter(File file, String charsetName, boolean append, Format converter) throws IOException {
        super();
        this.file = file;
        this.charsetName = charsetName;
        this.append = append;
        this.converter = converter;
        this.open();
    }

    /**
     * 初始化
     *
     * @param file        文件
     * @param charsetName 文件字符集
     * @param append      true表示将数据写入到文件末尾位置 false表示将数据写入文件起始位置（会覆盖原文件内容）
     * @throws IOException
     */
    public StandardFilePrinter(File file, String charsetName, boolean append) throws IOException {
        this(file, charsetName, append, null);
    }

    /**
     * 打开文件输出流
     *
     * @throws IOException
     */
    protected void open() throws IOException {
        this.setWriter(IO.getFileWriter(this.file, this.charsetName, this.append));
    }

}
