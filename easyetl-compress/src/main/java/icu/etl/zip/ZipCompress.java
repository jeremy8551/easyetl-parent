package icu.etl.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import icu.apache.ant.zip.ZipEntry;
import icu.apache.ant.zip.ZipFile;
import icu.apache.ant.zip.ZipOutputStream;
import icu.etl.annotation.EasyBean;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * ZIP压缩接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-07-23 16:16:03
 */
@EasyBean(name = "zip")
public class ZipCompress implements Compress {
    private final static Log log = LogFactory.getLog(ZipCompress.class);

    private volatile boolean terminate = false;
    private File zipFile;
    private ZipOutputStream zos;

    public ZipCompress() {
    }

    public void setFile(File file) {
        this.zipFile = file;
    }

    public void archiveFile(File file, String dir) throws IOException {
        this.addFile(file, dir, null, 0);
    }

    public void archiveFile(File file, String dir, String charsetName) throws IOException {
        this.addFile(file, dir, charsetName, 0);
    }

    protected void addFile(File file, String dir, String charsetName, int level) throws IOException {
        Ensure.notNull(this.zipFile);
        FileUtils.assertExists(file);
        charsetName = StringUtils.charset(charsetName);

        if (this.zos == null) {
            this.zos = new ZipOutputStream(new FileOutputStream(this.zipFile));
        }

        this.zos.setEncoding(charsetName);

        // 处理目录
        dir = (dir == null) ? "" : dir.trim();
        if (dir.equals("/")) {
            dir = "";
        }

        int length = dir.length();

        // 去掉最前面的斜线
        if (length > 1 && dir.charAt(0) == '/') {
            dir = dir.substring(1);
        }

        // 去掉最后面的斜线
        if (length > 1 && dir.charAt(length - 1) != '/') {
            dir = dir + "/";
        }

        if (file.isDirectory()) {
            String d = "";
            File root = new File(file.getAbsolutePath());
            for (int i = 0; i < level; i++) {
                if (this.terminate) {
                    break;
                } else {
                    root = new File(root.getParent());
                    d = root.getName() + "/" + d;
                }
            }
            d = d + file.getName() + "/";

            if (log.isDebugEnabled()) {
                log.debug("zip file, create dir: {} ..", d);
            }

            ZipEntry entry = new ZipEntry(d);
            this.zos.putNextEntry(entry);

            // 遍历目录下的所有文件并压入压缩包中的目录下
            File[] files = FileUtils.array(file.listFiles());
            for (int i = 0; i < files.length; i++) {
                if (this.terminate) {
                    break;
                } else {
                    this.addFile(files[i], d, charsetName, level + 1);
                }
            }
        } else {
            if (dir.length() > 1 && !dir.equals("//")) { // 创建父目录
                String d = dir.charAt(0) == '/' ? dir.substring(1) : dir;
                if (d.length() > 1) {
                    ZipEntry entry = new ZipEntry(d);
                    this.zos.putNextEntry(entry);
                }
            }

            String zipfile = dir + file.getName();
            InputStream in = new FileInputStream(file);
            try {
                if (log.isDebugEnabled()) {
                    if (StringUtils.isBlank(dir)) {
                        log.debug("zip {} {} ..", file.getAbsolutePath(), this.zipFile.getAbsolutePath());
                    } else {
                        log.debug("zip {} {} -> {} ..", file.getAbsolutePath(), this.zipFile.getAbsolutePath(), dir);
                    }
                }

                ZipEntry entry = new ZipEntry(zipfile);
                this.zos.putNextEntry(entry);
                byte[] buffer = new byte[1024];
                for (int len; (len = in.read(buffer)) != -1; ) {
                    if (this.terminate) {
                        break;
                    } else {
                        this.zos.write(buffer, 0, len);
                    }
                }
            } finally {
                in.close();
            }
        }
    }

    public void extract(String outputDir, String charsetName) throws IOException {
        FileUtils.assertFile(this.zipFile);
        FileUtils.assertCreateDirectory(outputDir);
        charsetName = StringUtils.charset(charsetName);

        ZipFile file = new ZipFile(zipFile, charsetName);
        try {
            if (log.isDebugEnabled()) {
                log.debug("unzip " + zipFile + " " + outputDir + " ..");
            }

            byte[] buffer = new byte[128];
            for (Enumeration<ZipEntry> it = file.getEntries(); it.hasMoreElements(); ) {
                if (this.terminate) {
                    break;
                }

                ZipEntry entry = it.nextElement();
                String filePath = FileUtils.joinPath(outputDir, entry.getName());

                if (log.isDebugEnabled()) {
                    log.debug("unzip " + entry.getName() + " " + outputDir + " ..");
                }

                if (entry.isDirectory()) {
                    FileUtils.assertCreateDirectory(filePath);
                } else {
                    this.tofile(file, entry, filePath, buffer);
                }
            }
        } finally {
            file.close();
        }
    }

    public void extract(String outputDir, String charsetName, String entryName) throws IOException {
        FileUtils.assertFile(this.zipFile);
        Ensure.notBlank(entryName);
        FileUtils.assertCreateDirectory(outputDir);
        charsetName = StringUtils.charset(charsetName);

        ZipFile file = new ZipFile(this.zipFile, charsetName);
        try {
            if (log.isDebugEnabled()) {
                log.debug("unzip " + this.zipFile + " -> " + entryName + " " + outputDir + " ..");
            }

            byte[] buffer = new byte[128];
            Iterable<ZipEntry> itr = file.getEntries(entryName);
            for (Iterator<ZipEntry> it = itr.iterator(); it.hasNext(); ) {
                if (this.terminate) {
                    break;
                }

                ZipEntry entry = it.next();
                String filePath = FileUtils.joinPath(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    FileUtils.assertCreateDirectory(filePath);
                } else {
                    this.tofile(file, entry, filePath, buffer);
                }
            }
        } finally {
            file.close();
        }
    }

    public List<ZipEntry> getEntrys(String charsetName, String filename, boolean ignoreCase) throws IOException {
        FileUtils.assertFile(this.zipFile);
        Ensure.notBlank(filename);
        charsetName = StringUtils.charset(charsetName);

        ZipFile file = new ZipFile(this.zipFile, charsetName);
        try {
            List<ZipEntry> list = new ArrayList<ZipEntry>();
            for (Enumeration<ZipEntry> it = file.getEntries(); it.hasMoreElements(); ) {
                if (this.terminate) {
                    break;
                }

                ZipEntry entry = it.nextElement();
                String name = FileUtils.getFilename(entry.getName());
                if (ignoreCase) {
                    if (name.equalsIgnoreCase(filename)) {
                        list.add(entry);
                    }
                } else {
                    if (name.equals(filename)) {
                        list.add(entry);
                    }
                }
            }
            return list;
        } finally {
            file.close();
        }
    }

    public boolean removeEntry(String charsetName, String... entryNames) throws IOException {
        File tmpDir = FileUtils.createTempDirectory(ZipCompress.class.getSimpleName());
        try {
            ZipFile file = new ZipFile(this.zipFile, charsetName); // 解压缩文件
            try {
                if (log.isDebugEnabled()) {
                    log.debug("delete zipfile " + this.zipFile + "'s entry: " + StringUtils.join(entryNames, ", ") + " ..");
                }

                byte[] buffer = new byte[128];
                for (Enumeration<ZipEntry> it = file.getEntries(); it.hasMoreElements(); ) {
                    if (this.terminate) {
                        break;
                    }

                    ZipEntry entry = it.nextElement();
                    if (this.find(entryNames, entry)) {
                        continue;
                    }

                    String filepath = FileUtils.joinPath(tmpDir.getAbsolutePath(), entry.getName());
                    if (entry.isDirectory()) {
                        FileUtils.assertCreateDirectory(filepath);
                    } else {
                        this.tofile(file, entry, filepath, buffer);
                    }
                }
            } finally {
                file.close();
            }

            // 重新压缩
            File newzipFile = new File(tmpDir.getAbsolutePath(), this.zipFile.getName());
            ZipCompress c = new ZipCompress();
            try {
                c.setFile(newzipFile);
                File[] list = FileUtils.array(tmpDir.listFiles());
                for (File child : list) {
                    if (this.terminate) {
                        break;
                    } else {
                        c.addFile(child, null, charsetName, 0);
                    }
                }
            } finally {
                c.close();
            }

            return this.zipFile.delete() && FileUtils.rename(newzipFile, this.zipFile, null);
        } finally {
            FileUtils.delete(tmpDir);
        }
    }

    private boolean find(String[] array, ZipEntry entry) {
        boolean success = false;
        for (String name : array) {
            if (entry.getName().indexOf(name) == 0) {
                success = true;
                break;
            }
        }
        return success;
    }

    public void close() {
        IO.close(this.zos);
    }

    /**
     * 把压缩包中的 ZipEntry 转换为 File
     */
    protected void tofile(ZipFile file, ZipEntry entry, String filepath, byte[] buffer) throws IOException {
        FileUtils.assertCreateFile(filepath);
        InputStream in = file.getInputStream(entry);
        try {
            FileOutputStream out = new FileOutputStream(filepath);
            try {
                for (int len = in.read(buffer); len != -1; len = in.read(buffer)) {
                    if (this.terminate) {
                        break;
                    } else {
                        out.write(buffer, 0, len);
                    }
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public void terminate() {
        this.terminate = true;
    }

    public boolean isTerminate() {
        return this.terminate;
    }
}
