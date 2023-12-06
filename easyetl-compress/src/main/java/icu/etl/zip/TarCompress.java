package icu.etl.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import icu.apache.ant.tar.TarEntry;
import icu.apache.ant.tar.TarInputStream;
import icu.apache.ant.tar.TarOutputStream;
import icu.etl.annotation.EasyBean;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.Numbers;
import icu.etl.util.StringUtils;

@EasyBean(name = "tar")
public class TarCompress implements Compress {
    private final static Log log = LogFactory.getLog(TarCompress.class);

    private volatile boolean terminate = false;
    private File tarFile;
    private TarOutputStream outputStream;
    private TarInputStream inputStream;

    /**
     * 缓冲区
     */
    protected byte[] buffer;

    /**
     * true表示tar文件使用gzip格式压缩
     */
    protected boolean isGzipCompress = false;

    /**
     * 构造函数
     */
    public TarCompress() {
        this.buffer = new byte[512];
    }

    /**
     * 构造函数
     *
     * @param file           tar文件
     * @param bufferSize     缓冲区大小
     * @param isGzipCompress true表示tar文件使用gzip格式压缩
     */
    public TarCompress(File file, int bufferSize, boolean isGzipCompress) {
        this.setFile(file);
        this.buffer = new byte[bufferSize];
        this.setGzipCompress(isGzipCompress);
    }

    /**
     * tar文件是否使用gzip格式压缩
     *
     * @return
     */
    public boolean isGzipCompress() {
        return isGzipCompress;
    }

    /**
     * true表示tar文件使用gzip格式压缩
     *
     * @param useGzipFormate
     */
    public void setGzipCompress(boolean useGzipFormate) {
        this.isGzipCompress = useGzipFormate;
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

        if (this.tarFile == null) {
            throw new IllegalArgumentException("tarfile is null!");
        }

        if (StringUtils.isBlank(charset)) {
            charset = StringUtils.CHARSET;
        }

        try {
            initTarOutputStream(charset);

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
                    root = new File(root.getParent());
                    d = root.getName() + "/" + d;
                }
                d = d + file.getName() + "/";

                if (log.isDebugEnabled()) {
                    log.debug("tar file, create dir: " + d + " ..");
                }

                TarEntry entry = new TarEntry(d);
                entry.setSize(0);
                this.outputStream.putNextEntry(entry);
                this.outputStream.closeEntry();

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
                        TarEntry entry = new TarEntry(d);
                        entry.setSize(0);
                        this.outputStream.putNextEntry(entry);
                        this.outputStream.closeEntry();
                    }
                }

                String tarFile = dir + file.getName();
                InputStream is = new FileInputStream(file);
                if (log.isDebugEnabled()) {
                    if (StringUtils.isBlank(dir)) {
                        log.debug("tar " + file.getAbsolutePath() + " " + this.tarFile.getAbsolutePath() + " ..");
                    } else {
                        log.debug("tar " + file.getAbsolutePath() + " " + this.tarFile.getAbsolutePath() + " -> " + dir + " ..");
                    }
                }

                try {
                    TarEntry entry = new TarEntry(tarFile);
                    entry.setSize(file.length());
                    this.outputStream.putNextEntry(entry);
                    byte tmp[] = new byte[1024];
                    int i = 0;
                    while ((i = is.read(tmp)) != -1) {
                        if (this.terminate) {
                            break;
                        }
                        this.outputStream.write(tmp, 0, i);
                    }
                    this.outputStream.closeEntry();
                } finally {
                    IO.close(is);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("tar " + file.getAbsolutePath() + " fail!", e);
        }
    }

    public void extract(String outputDir, String charsetName) {
        FileUtils.createDirectory(new File(outputDir));
        boolean hasError = false;

        try {
            this.initTarInputStream(charsetName);
            TarEntry entry = null;
            while ((entry = inputStream.getNextEntry()) != null) {
                if (this.terminate) {
                    break;
                }

                if (!this.untar(inputStream, outputDir, entry)) {
                    hasError = true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("untar " + this.tarFile.getAbsolutePath() + " fail!", e);
        } finally {
            this.closeTarInputStream();
        }

        if (hasError) {
            throw new RuntimeException("untar " + this.tarFile.getAbsolutePath() + " fail!");
        }
    }

    public void extract(String outputDir, String charsetName, String... filerEntryNames) {
        FileUtils.createDirectory(new File(outputDir));
        boolean hasError = false;

        try {
            this.initTarInputStream(charsetName);
            TarEntry entry = null;
            while ((entry = inputStream.getNextEntry()) != null) {
                if (this.terminate) {
                    break;
                }

                if (!StringUtils.inArray(entry.getName(), filerEntryNames)) {
                    if (!this.untar(inputStream, outputDir, entry)) {
                        hasError = true;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("untar " + this.tarFile.getAbsolutePath() + " fail!", e);
        } finally {
            this.closeTarInputStream();
        }

        if (hasError) {
            throw new RuntimeException("untar " + this.tarFile.getAbsolutePath() + " fail!");
        }
    }

    public void extract(String outputDir, String charsetName, String entryName) {
        FileUtils.createDirectory(new File(outputDir));
        boolean hasError = false;

        try {
            this.initTarInputStream(charsetName);
            TarEntry entry = null;
            while ((entry = inputStream.getNextEntry()) != null) {
                if (this.terminate) {
                    break;
                }

                if (entryName.equals(entry.getName())) {
                    if (!this.untar(inputStream, outputDir, entry)) {
                        hasError = true;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("untar " + this.tarFile.getAbsolutePath() + " fail!", e);
        } finally {
            this.closeTarInputStream();
        }

        if (hasError) {
            throw new RuntimeException("untar " + this.tarFile.getAbsolutePath() + " fail!");
        }
    }

    /**
     * 解压 TarEntry 对象
     *
     * @param inputStream io数据流
     * @param outputDir   解压后目录
     * @param entry       TarEntry实例
     * @throws FileNotFoundException
     * @throws IOException
     */
    public boolean untar(TarInputStream inputStream, String outputDir, TarEntry entry) throws FileNotFoundException, IOException {
        if (entry == null) {
            throw new NullPointerException();
        }

        boolean success = true;
        if (entry.isDirectory()) { // 如果是目录
            File dir = new File(outputDir, entry.getName());
            FileUtils.createDirectory(dir);
            TarEntry[] childEntrys = entry.getDirectoryEntries(); // 遍历目录下的文件
            for (int i = 0; i < childEntrys.length; i++) {
                if (this.terminate) {
                    break;
                }

                TarEntry childEntry = childEntrys[i];
                if (!this.untar(inputStream, dir.getAbsolutePath(), childEntry.getName())) {
                    success = false;
                }
            }
        } else {
            if (!this.untar(inputStream, outputDir, entry.getName())) {
                success = false;
            }
        }

        return success;
    }

    /**
     * 解压tar文件到指定目录
     *
     * @param inputStream io数据流
     * @param outputDir   解压后目录
     * @param filename    文件名
     * @throws FileNotFoundException
     * @throws IOException
     */
    public boolean untar(TarInputStream inputStream, String outputDir, String filename) throws FileNotFoundException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("untar " + FileUtils.replaceFolderSeparator(FileUtils.joinFilepath(outputDir, filename)) + " ..");
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(outputDir, filename), false);
            for (int s = inputStream.read(buffer, 0, buffer.length); s != -1; s = inputStream.read(buffer, 0, buffer.length)) {
                if (this.terminate) {
                    break;
                }
                fos.write(buffer, 0, s);
            }
            return true;
        } catch (Exception e) {
            log.error(StringUtils.toString(e));
            return false;
        } finally {
            IO.close(fos);
        }
    }

    public List<TarEntry> getEntrys(String charsetName, String regex, boolean ignoreCase) {
        List<TarEntry> list = new ArrayList<TarEntry>();
        try {
            this.initTarInputStream(StringUtils.defaultString(charsetName, StringUtils.CHARSET));
            TarEntry entry = null;
            while ((entry = this.inputStream.getNextEntry()) != null) {
                if (this.terminate) {
                    break;
                }

                getTarEntrys(entry, regex, ignoreCase, list);
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("loop " + this.tarFile.getAbsolutePath() + " tar file, search " + regex + " error!", e);
        } finally {
            this.closeTarInputStream();
        }
    }

    protected void getTarEntrys(TarEntry entry, String regex, boolean ignoreCase, List<TarEntry> list) {
        if (entry.isDirectory()) {
            String[] array = StringUtils.split(FileUtils.replaceFolderSeparator(entry.getName()), String.valueOf(File.separatorChar));
            int index = StringUtils.lastIndexOfNotBlank(array);
            String name = index >= 0 ? array[index] : null;
            if (name != null && this.match(name, regex, ignoreCase)) {
                list.add(entry);
            }

            TarEntry[] childEntrys = entry.getDirectoryEntries();
            for (TarEntry childEntry : childEntrys) {
                if (this.terminate) {
                    break;
                }

                this.getTarEntrys(childEntry, regex, ignoreCase, list);
            }
        } else {
            if (this.match(FileUtils.getFilename(entry.getName()), regex, ignoreCase)) {
                list.add(entry);
            }
        }
    }

    /**
     * 判断 entryName 与 regex 是否匹配
     *
     * @param entryName
     * @param regex
     * @param ignoreCase
     * @return
     */
    protected boolean match(String entryName, String regex, boolean ignoreCase) {
        if (ignoreCase) {
            if (entryName.equalsIgnoreCase(regex)) {
                return true;
            }
        } else {
            if (entryName.equals(regex) || entryName.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    public boolean removeEntry(String charsetName, String... entryName) throws IOException {
        String dirName = StringUtils.replaceAll(FileUtils.getFilenameNoExt(this.tarFile.getName()) + Numbers.getRandom() + Numbers.getRandom() + Numbers.getRandom() + Numbers.getRandom() + "tmp", ".", "");
        String dir = FileUtils.joinFilepath(this.tarFile.getParent(), dirName);
        File tmpDir = new File(dir);
        FileUtils.createDirectory(tmpDir);
        try {
            this.extract(tmpDir.getAbsolutePath(), charsetName, entryName);
            this.close();

            /** 重新压缩 */
            File copy = new File(dir, this.tarFile.getName());
            TarCompress c = new TarCompress(copy, this.buffer.length, this.isGzipCompress);
            try {
                File[] childs = FileUtils.array(tmpDir.listFiles());
                for (File child : childs) {
                    if (this.terminate) {
                        break;
                    }

                    c.archiveFile(child, null, charsetName);
                }
            } finally {
                c.close();
            }

            if (!this.tarFile.delete()) {
                return false;
            }

            return FileUtils.moveFile(copy, this.tarFile.getParentFile());
        } finally {
            FileUtils.clearDirectory(tmpDir);
            tmpDir.delete();
        }
    }

    protected TarOutputStream initTarOutputStream(String charsetName) throws IOException, FileNotFoundException {
        if (this.outputStream == null) {
            if (this.isGzipCompress()) {
                this.outputStream = new TarOutputStream(new GZIPOutputStream(new FileOutputStream(this.tarFile, true)), charsetName);
            } else {
                this.outputStream = new TarOutputStream(new FileOutputStream(this.tarFile, true), charsetName);
            }
        }
        return this.outputStream;
    }

    protected TarInputStream initTarInputStream(String charsetName) throws IOException, FileNotFoundException {
        if (this.inputStream == null) {
            if (this.isGzipCompress()) {
                this.inputStream = new TarInputStream(new GZIPInputStream(new FileInputStream(this.tarFile)), charsetName);
            } else {
                this.inputStream = new TarInputStream(new FileInputStream(this.tarFile), charsetName);
            }
        }
        return this.inputStream;
    }

    public void setFile(File file) {
        this.tarFile = file;
    }

    /**
     * 遍历输入流后需要关闭
     */
    private void closeTarInputStream() {
        IO.close(this.inputStream);
        this.inputStream = null;
    }

    public void close() {
        IO.close(this.inputStream, this.outputStream);
    }

    public void terminate() {
        this.terminate = true;
    }

    public boolean isTerminate() {
        return this.terminate;
    }

}