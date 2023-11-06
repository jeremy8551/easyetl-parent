package icu.etl.os.ftp;

import java.util.List;

import icu.etl.os.OSFile;
import icu.etl.util.StringUtils;

/**
 * FTP文件
 */
public class ApacheFtpFile {

    /** 文件路径 */
    private String path;

    /** 如果当前文件是一个目录，则这个属性表示目录中的子文件信息 */
    private List<OSFile> list;

    /** true表示文件是一个目录 */
    private boolean isDir;

    /**
     * FTP文件
     *
     * @param path  文件路径
     * @param isDir 如果当前文件是一个目录，则这个属性表示目录中的子文件信息
     * @param list  true表示文件是一个目录
     */
    public ApacheFtpFile(String path, boolean isDir, List<OSFile> list) {
        this.path = path;
        this.list = list;
        this.isDir = isDir;
    }

    public List<OSFile> listFiles() {
        return list;
    }

    public boolean isDirectory() {
        return this.isDir;
    }

    public String getPath() {
        return path;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(path);
        buf.append(" ");
        buf.append(StringUtils.toString(list));
        buf.append("}");
        return super.toString();
    }
}
