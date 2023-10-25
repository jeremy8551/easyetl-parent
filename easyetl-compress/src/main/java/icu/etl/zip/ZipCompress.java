package icu.etl.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipException;

import icu.apache.ant.zip.ZipEntry;
import icu.apache.ant.zip.ZipFile;
import icu.apache.ant.zip.ZipOutputStream;
import icu.etl.annotation.EasyBean;
import icu.etl.log.STD;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * ZIP压缩接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-07-23 16:16:03
 */
@EasyBean(kind = "zip", mode = "", major = "", minor = "")
public class ZipCompress implements Compress {

    private volatile boolean terminate = false;
    private File zipFile;
    private ZipOutputStream zos;

    public ZipCompress() {
    }

    public void setFile(File file) {
        this.zipFile = file;
    }

    public void archiveFile(File file, String dir) {
        addFile(file, dir, null, 0);
    }

    public void archiveFile(File file, String dir, String charset) {
        addFile(file, dir, charset, 0);
    }

    protected void addFile(File file, String dir, String charset, int level) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException();
        }

        if (this.zipFile == null) {
            throw new IllegalArgumentException("zipFile is null!");
        }

        try {
            if (this.zos == null) {
                this.zos = new ZipOutputStream(new FileOutputStream(this.zipFile));
            }

            if (this.zos != null) {
                this.zos.setEncoding(StringUtils.defaultString(charset, StringUtils.CHARSET));
            }

            // 处理目录
            dir = (dir == null) ? "" : dir.trim();
            if (dir.equals("/")) {
                dir = "";
            }

            int len = dir.length();

            // 去掉最前面的斜线
            if (len > 1 && dir.charAt(0) == '/') {
                dir = dir.substring(1);
            }

            // 去掉最后面的斜线
            if (len > 1 && dir.charAt(len - 1) != '/') {
                dir = dir + "/";
            }

            if (file.isDirectory()) {
                String d = "";
                File root = new File(file.getAbsolutePath());
                for (int i = 0; i < level; i++) {
                    if (this.terminate) {
                        break;
                    }

                    root = new File(root.getParent());
                    d = root.getName() + "/" + d;
                }
                d = d + file.getName() + "/";

                if (STD.out.isDebugEnabled()) {
                    STD.out.debug("zip file, create dir: " + d + " ..");
                }
                ZipEntry entry = new ZipEntry(d);
                this.zos.putNextEntry(entry);

                // 遍历目录下的所有文件并压入压缩包中的目录下
                File[] fs = FileUtils.array(file.listFiles());
                for (int i = 0; i < fs.length; i++) {
                    if (this.terminate) {
                        break;
                    }

                    this.addFile(fs[i], d, charset, level + 1);
                }
            } else {
                if (dir.length() > 1 && !dir.equals("//")) { // 创建父目录
                    String d = dir.charAt(0) == '/' ? dir.substring(1) : dir;
                    if (d.length() > 1) {
                        ZipEntry entry = new ZipEntry(d);
                        this.zos.putNextEntry(entry);
                    }
                }

                String zipFile = dir + file.getName();
                InputStream is = new FileInputStream(file);
                if (STD.out.isDebugEnabled()) {
                    if (StringUtils.isBlank(dir)) {
                        STD.out.debug("zip " + file.getAbsolutePath() + " " + this.zipFile.getAbsolutePath() + " ..");
                    } else {
                        STD.out.debug("zip " + file.getAbsolutePath() + " " + this.zipFile.getAbsolutePath() + " -> " + dir + " ..");
                    }
                }

                try {
                    ZipEntry entry = new ZipEntry(zipFile);
                    this.zos.putNextEntry(entry);
                    byte tmp[] = new byte[1024];
                    int i = 0;
                    while ((i = is.read(tmp)) != -1) {
                        if (this.terminate) {
                            break;
                        }

                        this.zos.write(tmp, 0, i);
                    }
                } finally {
                    IO.closeQuietly(is);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("zip " + file.getAbsolutePath() + " fail!", e);
        }
    }

    public void extract(String outputDir, String charsetName) {
        if (zipFile == null) {
            throw new IllegalArgumentException("unzip " + zipFile.getPath() + " fail: zipFile is null!");
        }
        if (!zipFile.exists() || !zipFile.isFile()) {
            throw new IllegalArgumentException("unzip " + zipFile.getPath() + " fail: zipfile " + zipFile.getPath() + " invalid!");
        }

        if (!FileUtils.exists(outputDir)) {
            FileUtils.createDirectory(new File(outputDir));
        } else if (!FileUtils.isDirectory(outputDir)) {
            throw new IllegalArgumentException("unzip " + zipFile.getPath() + " fail: dir " + outputDir + " invalid!");
        }

        if (StringUtils.isBlank(charsetName)) {
            charsetName = StringUtils.CHARSET;
        }

        ZipFile file = null;
        try {
            if (STD.out.isDebugEnabled()) {
                STD.out.debug("unzip " + zipFile + " " + outputDir + " ..");
            }

            file = new ZipFile(zipFile, charsetName);
            byte[] buf = new byte[128];
            Enumeration<ZipEntry> it = file.getEntries();
            while (it.hasMoreElements()) {
                ZipEntry entry = it.nextElement();
                if (this.terminate) {
                    break;
                }

                String filePath = FileUtils.joinFilepath(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    if (STD.out.isDebugEnabled()) {
                        STD.out.debug("unzip " + entry.getName() + " " + outputDir + " ..");
                    }
                    FileUtils.createDirectory(new File(filePath));
                } else {
                    if (STD.out.isDebugEnabled()) {
                        STD.out.debug("unzip " + entry.getName() + " " + outputDir + " ..");
                    }
                    zipEntry2File(file, entry, filePath, buf);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("unzip " + zipFile.getPath() + " fail!", e);
        } finally {
            IO.close(file);
        }
    }

    public void extract(String outputDir, String charsetName, String entryName) {
        if (zipFile == null) {
            throw new IllegalArgumentException("unzip " + zipFile.getPath() + " fail: zipFile is null!");
        }
        if (!zipFile.exists() || !zipFile.isFile()) {
            throw new IllegalArgumentException("unzip " + zipFile.getPath() + " fail: zipfile " + zipFile.getPath() + " invalid!");
        }

        if (!FileUtils.exists(outputDir)) {
            FileUtils.createDirectory(new File(outputDir));
        } else if (!FileUtils.isDirectory(outputDir)) {
            throw new IllegalArgumentException("unzip " + zipFile.getPath() + " fail: directory " + outputDir + " invalid!");
        }

        if (StringUtils.isBlank(entryName)) {
            throw new IllegalArgumentException("entryName is blank!");
        }

        if (StringUtils.isBlank(charsetName)) {
            charsetName = StringUtils.CHARSET;
        }

        ZipFile file = null;
        try {
            if (STD.out.isDebugEnabled()) {
                STD.out.debug("unzip " + zipFile + " -> " + entryName + " " + outputDir + " ..");
            }

            file = new ZipFile(zipFile, charsetName);
            byte[] buf = new byte[128];
            Iterable<ZipEntry> entryList = file.getEntries(entryName);
            Iterator<ZipEntry> it = entryList.iterator();
            while (it.hasNext()) {
                if (this.terminate) {
                    break;
                }

                ZipEntry entry = it.next();
                String filePath = FileUtils.joinFilepath(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    FileUtils.createDirectory(new File(filePath));
                } else {
                    zipEntry2File(file, entry, filePath, buf);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("unzip " + zipFile.getPath() + " fail!", e);
        } finally {
            IO.close(file);
        }
    }

    public List<ZipEntry> getEntrys(String charsetName, String filename, boolean ignoreCase) {
        if (zipFile == null) {
            throw new IllegalArgumentException("unzip " + zipFile.getPath() + " fail: zipFile is null!");
        }
        if (!zipFile.exists() || !zipFile.isFile()) {
            throw new IllegalArgumentException("unzip " + zipFile.getPath() + " fail: zipfile " + zipFile.getPath() + " invalid!");
        }

        if (StringUtils.isBlank(filename)) {
            throw new IllegalArgumentException("filename is null!");
        }

        ZipFile file = null;
        try {
            file = new ZipFile(zipFile, charsetName);
            List<ZipEntry> result = new ArrayList<ZipEntry>();
            Enumeration<ZipEntry> it = file.getEntries();
            while (it.hasMoreElements()) {
                if (this.terminate) {
                    break;
                }

                ZipEntry entry = it.nextElement();
                String fn = FileUtils.getFilename(entry.getName());
                if (ignoreCase) {
                    if (fn.equalsIgnoreCase(filename)) {
                        result.add(entry);
                    }
                } else {
                    if (fn.equals(filename)) {
                        result.add(entry);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("unzip " + zipFile.getPath() + " fail!", e);
        } finally {
            IO.close(file);
        }
    }

    public boolean removeEntry(String charsetName, String... entryName) throws IOException {
        String dir = FileUtils.joinFilepath(this.zipFile.getParent(), FileUtils.getFilenameRandom("java_zip_del_", "_tmp"));
        File tmpDir = new File(dir);
        FileUtils.createDirectory(tmpDir);
        try {
            HashSet<String> set = new HashSet<String>();
            Collections.addAll(set, entryName);

            ZipFile file = null;
            try {
                if (STD.out.isDebugEnabled()) {
                    STD.out.debug("delete zipfile " + zipFile + "'s entry: " + StringUtils.join(entryName, ", ") + " ..");
                }

                /** 解压缩文件 */
                file = new ZipFile(zipFile, charsetName);
                byte[] buf = new byte[128];
                Enumeration<ZipEntry> it = file.getEntries();
                while (it.hasMoreElements()) {
                    ZipEntry entry = it.nextElement();
                    if (this.terminate) {
                        break;
                    }

                    boolean isFilterData = false;
                    for (String en : entryName) {
                        if (this.terminate) {
                            break;
                        }

                        if (entry.getName().indexOf(en) == 0) {
                            isFilterData = true;
                            break;
                        }
                    }
                    if (isFilterData) {
                        continue;
                    }

                    String filePath = FileUtils.joinFilepath(dir, entry.getName());
                    if (entry.isDirectory()) {
                        FileUtils.createDirectory(new File(filePath));
                    } else {
                        zipEntry2File(file, entry, filePath, buf);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("unzip " + zipFile.getPath() + " fail!", e);
            } finally {
                IO.close(file);
            }

            File[] childs = FileUtils.array(tmpDir.listFiles());

            /** 重新压缩 */

            File copy = new File(dir, this.zipFile.getName());
            ZipCompress zc = new ZipCompress();
            try {
                zc.setFile(copy);
                for (File child : childs) {
                    if (this.terminate) {
                        break;
                    }
                    zc.addFile(child, null, charsetName, 0);
                }
            } finally {
                zc.close();
            }

            if (!this.zipFile.delete()) {
                return false;
            }

            return FileUtils.moveFile(copy, this.zipFile.getParentFile());
        } finally {
            FileUtils.clearDirectory(tmpDir);
            tmpDir.delete();
        }
    }

    public void close() {
        IO.close(zos);
    }

    /**
     * 把压缩包中的 ZipEntry 转换为 File
     *
     * @param file
     * @param entry
     * @param filepath
     * @param buffer
     * @throws IOException
     * @throws ZipException
     * @throws FileNotFoundException
     */
    protected void zipEntry2File(ZipFile file, ZipEntry entry, String filepath, byte[] buffer) throws IOException, ZipException, FileNotFoundException {
        FileUtils.createFile(new File(filepath));

        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = file.getInputStream(entry);
            fos = new FileOutputStream(filepath);
            for (int s = is.read(buffer, 0, buffer.length); s != -1; s = is.read(buffer, 0, buffer.length)) {
                if (this.terminate) {
                    break;
                }

                fos.write(buffer, 0, s);
            }
        } finally {
            IO.closeQuietly(is);
            IO.closeQuietly(fos);
        }
    }

    public void terminate() {
        this.terminate = true;
    }

    public boolean isTerminate() {
        return this.terminate;
    }
}
