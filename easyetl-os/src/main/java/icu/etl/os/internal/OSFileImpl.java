package icu.etl.os.internal;

import java.io.File;
import java.util.Date;

import icu.etl.jdk.JavaDialectFactory;
import icu.etl.os.OSFile;
import icu.etl.os.linux.Linuxs;
import icu.etl.util.Dates;

/**
 * 操作系统上文件的接口实现
 */
public class OSFileImpl implements OSFile {

    private String name;
    private String parent;
    private String absolutePath;
    private long size;
    private Date createTime;
    private Date modifyTime;
    private boolean isDir;
    private boolean isLink;
    private boolean isFile;
    private boolean isBlk;
    private boolean isPipe;
    private boolean isSock;
    private boolean isChr;
    private String link;
    private String longname;
    private boolean canRead;
    private boolean canWrite;
    private boolean canExecute;

    public OSFileImpl() {
    }

    public OSFileImpl(File file) {
        this.setName(file.getName());
        this.setParent(file.getParent());
        this.setModifyTime(new Date(file.lastModified()));
        this.setLength(file.length());
        this.setFile(file.isFile());
        this.setDirectory(file.isDirectory());
        String link = JavaDialectFactory.getDialect().getLink(file);
        if (link != null) {
            this.setLink(true);
            this.setLink(link);
        }
        this.setCreateTime(JavaDialectFactory.getDialect().getCreateTime(this.getAbsolutePath()));
        this.setLongname(Linuxs.toLongname(file).toString());
        this.setCanRead(file.canRead());
        this.setCanWrite(file.canWrite());
        this.setCanExecute(JavaDialectFactory.getDialect().canExecute(file));
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
        this.absolutePath = null;
    }

    public String getLongname() {
        return longname;
    }

    public void setLongname(String line) {
        this.longname = line;
    }

    public String getName() {
        return name;
    }

    public void setName(String filename) {
        this.name = filename;
        this.absolutePath = null;
    }

    public long length() {
        return size;
    }

    public void setLength(long size) {
        this.size = size;
    }

    public Date getCreateDate() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyDate() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public boolean isDirectory() {
        return isDir;
    }

    public void setDirectory(boolean isDir) {
        this.isDir = isDir;
    }

    public boolean isLink() {
        return isLink;
    }

    public void setLink(boolean isLink) {
        this.isLink = isLink;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean isFile) {
        this.isFile = isFile;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isBlockDevice() {
        return isBlk;
    }

    public void setBlockDevice(boolean isBlk) {
        this.isBlk = isBlk;
    }

    public boolean isPipe() {
        return isPipe;
    }

    public void setPipe(boolean isPipe) {
        this.isPipe = isPipe;
    }

    public boolean isSock() {
        return isSock;
    }

    public void setSock(boolean isSock) {
        this.isSock = isSock;
    }

    public boolean isCharDevice() {
        return isChr;
    }

    public void setCharDevice(boolean isChr) {
        this.isChr = isChr;
    }

    public String getAbsolutePath() {
        if (this.absolutePath == null) {
            StringBuilder buf = new StringBuilder();
            buf.append(this.parent);
            if (this.parent.endsWith("/") || this.parent.endsWith("\\")) {
                if (this.name.startsWith("/") || this.name.startsWith("\\")) {
                    buf.append(this.name.substring(1));
                } else {
                    buf.append(this.name);
                }
            } else {
                if (this.name.startsWith("/") || this.name.startsWith("\\")) {
                    buf.append(this.name);
                } else {
                    int up = this.parent.indexOf('/');
                    if (up == -1) {
                        int wp = this.parent.indexOf('\\');
                        buf.append(wp == -1 ? '/' : '\\');
                    } else {
                        buf.append('/');
                    }
                    buf.append(this.name);
                }
            }
            this.absolutePath = buf.toString();
        }
        return this.absolutePath;
    }

    public boolean canRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean canWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public boolean canExecute() {
        return canExecute;
    }

    public void setCanExecute(boolean canExecute) {
        this.canExecute = canExecute;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[name=" + name).append(", ");
        buf.append("parent=" + this.parent).append(", ");
        buf.append("size=" + size).append(", ");
        buf.append("createTime=" + Dates.format21(this.createTime)).append(", ");
        buf.append("modifyTime=" + Dates.format21(this.modifyTime)).append(", ");
        buf.append("isFile=" + this.isFile).append(", ");
        buf.append("isDir=" + this.isDir).append(", ");
        buf.append("isBlk=" + this.isBlk).append(", ");
        buf.append("isChr=" + this.isChr).append(", ");
        buf.append("isLink=" + this.isLink).append(", ");
        buf.append("isPipe=" + this.isPipe).append(", ");
        buf.append("isSock=" + this.isSock).append(", ");
        buf.append("link=" + this.link).append(", ");
        buf.append("canRead=" + this.canRead).append(", ");
        buf.append("canWrite=" + this.canWrite).append(", ");
        buf.append("canExecute=" + this.canExecute).append(", ");
        buf.append("longname=" + this.longname).append("]");
        return buf.toString();
    }
}
