package icu.etl.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import icu.apache.ant.tar.TarEntry;
import icu.etl.annotation.EasyBean;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

/**
 * 只对文件或目录下的所有子文件进行压缩: <br>
 * 1) 压缩后文件扩展名 gz, 如 JavaConfig.java == JavaConfig.gz <br>
 * 2) 成功压缩文件后自动删除原文件（目录不会删除）<br>
 * 3) 如果压缩目录, 自动遍历目录下的所有文件（包含子目录下的文件） <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2017-11-30
 */
@EasyBean(name = "gz")
public class GzipCompress implements Compress {
    private final static Log log = LogFactory.getLog(GzipCompress.class);

    /** true 表示已终止压缩或解压 */
    private volatile boolean terminate = false;

    /** 压缩文件 */
    protected File gzipFile;

    /** 缓冲区 */
    protected byte[] buffer;

    /**
     * 构造函数
     */
    public GzipCompress() {
    }

    /**
     * 初始化操作
     */
    protected void initBuffer() {
        if (this.buffer == null) {
            this.buffer = new byte[512];
        }
    }

    public void archiveFile(File file, String dir) throws IOException {
        if (file.isFile()) {
            this.gzipFile(file, this.gzipFile, true);
            return;
        }

        if (file.isDirectory()) {
            this.gzipDir(file, this.gzipFile, true, true);
            return;
        }
    }

    public void archiveFile(File file, String dir, String charsetName) throws IOException {
        if (file.isFile()) {
            this.gzipFile(file, this.gzipFile, true);
            return;
        }

        if (file.isDirectory()) {
            this.gzipDir(file, this.gzipFile, true, true);
            return;
        }
    }

    /**
     * 遍历压缩目录中的所有文件
     *
     * @param dir     目录（压缩目录中的文件，压缩文件还在这个目录）
     * @param gzipDir 文件压缩后存储的目录，如果为null表示存储在文件所在的目录
     * @param delete  true表示压缩文件后删除原文件
     * @param loop    true表示循环遍历压缩目录下的所有文件 false表示只压缩目录下一级文件
     * @throws IOException 访问文件错误
     */
    public void gzipDir(File dir, File gzipDir, boolean delete, boolean loop) throws IOException {
        FileUtils.assertDirectory(dir);

        if (gzipDir == null) {
            gzipDir = dir;
        } else {
            FileUtils.assertCreateDirectory(gzipDir);
        }

        File[] files = FileUtils.array(dir.listFiles());
        for (File file : files) {
            if (this.terminate) {
                break;
            }

            if (file.isFile() && file.canRead()) {
                File gz = new File(gzipDir, file.getName() + ".gz");
                FileUtils.assertCreateFile(gz);
                this.gzipFile(file, gz, delete);
                continue;
            }

            if (loop && file.isDirectory()) {
                File cDir = new File(gzipDir, file.getName());
                this.gzipDir(file, cDir, delete, loop);
            }
        }
    }

    /**
     * 压缩文件
     *
     * @param file     待压缩文件
     * @param gzipFile 压缩后 gz 文件; 为null默认为 file文件同级目录
     * @param delete   true表示压缩文件后删除原文件
     * @throws IOException 访问文件错误
     */
    public void gzipFile(File file, File gzipFile, boolean delete) throws IOException {
        FileUtils.assertCreateFile(gzipFile);

        if (gzipFile == null) {
            gzipFile = new File(file.getParentFile(), file.getName() + ".gz");
        }

        if (log.isDebugEnabled()) {
            if (file.getParentFile().equals(gzipFile.getParentFile())) {
                log.debug("gzip " + file.getAbsolutePath() + " " + gzipFile.getName() + " ..");
            } else {
                log.debug("gzip " + file.getAbsolutePath() + " " + gzipFile.getAbsolutePath() + " ..");
            }
        }

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            BufferedOutputStream out = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(gzipFile)));
            try {
                this.initBuffer();
                int len;
                while (!this.terminate && (len = in.read(this.buffer)) != -1) {
                    out.write(this.buffer, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }

        if (delete) {
            FileUtils.delete(file);
        }
    }

    public void extract(String outputDir, String charsetName) throws IOException {
        this.gunzipFile(this.gzipFile, new File(outputDir), null, true);
    }

    public void extract(String outputDir, String charsetName, String entryName) throws IOException {
        this.gunzipFile(this.gzipFile, new File(outputDir), null, true);
    }

    // 解压目录中的gz文件
    public void gunzipDir(File gzipDir, File dir, boolean delete, boolean loop) throws IOException {
        FileUtils.assertDirectory(gzipDir);

        if (dir == null) {
            dir = gzipDir;
        } else {
            FileUtils.assertCreateDirectory(dir);
        }

        File[] files = FileUtils.array(gzipDir.listFiles());
        for (File gzip : files) {
            if (this.terminate) {
                break;
            }

            if (gzip.isFile() && gzip.canRead() && gzip.getName().toLowerCase().endsWith(".gz")) {
                this.gunzipFile(gzip, dir, FileUtils.getFilenameNoExt(gzip.getName()), delete);
                continue;
            }

            if (loop && gzip.isDirectory()) {
                File cDir = new File(dir, gzip.getName());
                this.gunzipDir(gzip, cDir, delete, loop);
            }
        }
    }

    /**
     * 解压 gz 文件
     *
     * @param gzipFile gz文件
     * @param dir      解压后的目录
     * @param filename 解压后文件名（null表示使用默认的文件名）
     * @param delete   true表示解压后删除gz文件
     * @throws IOException 访问文件错误
     */
    public void gunzipFile(File gzipFile, File dir, String filename, boolean delete) throws IOException {
        if (dir == null) {
            dir = gzipFile.getParentFile();
        }
        if (StringUtils.isBlank(filename)) {
            filename = FileUtils.getFilenameNoExt(gzipFile.getName());
        }

        FileUtils.assertCreateDirectory(dir);
        File file = new File(dir, filename);
        if (log.isDebugEnabled()) {
            if (gzipFile.getParentFile().equals(dir)) {
                log.debug("gunzip file: " + gzipFile.getAbsolutePath() + " ..");
            } else {
                log.debug("gunzip file: " + gzipFile.getAbsolutePath() + " " + file.getAbsolutePath() + " ..");
            }
        }

        GZIPInputStream in = new GZIPInputStream(new FileInputStream(gzipFile));
        try {
            this.gunzip(in, file);
        } finally {
            in.close();
        }

        if (delete) {
            FileUtils.deleteFile(gzipFile);
        }
    }

    public void gunzip(GZIPInputStream in, File file) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        try {
            this.initBuffer();
            for (int len; (len = in.read(this.buffer)) != -1; ) {
                out.write(this.buffer, 0, len);
            }
            out.flush();
        } finally {
            out.close();
        }
    }

    public List<TarEntry> getEntrys(String charsetName, String regex, boolean ignoreCase) {
        throw new UnsupportedOperationException();
    }

    public boolean removeEntry(String charsetName, String... entryName) {
        throw new UnsupportedOperationException();
    }

    public void setFile(File file) {
        this.gzipFile = file;
    }

    public void close() {
        this.buffer = null;
    }

    public void terminate() {
        this.terminate = true;
    }

    public boolean isTerminate() {
        return this.terminate;
    }

}