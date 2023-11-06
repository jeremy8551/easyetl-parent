package icu.etl.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import icu.apache.ant.unrar.Archive;
import icu.apache.ant.unrar.exception.RarException;
import icu.apache.ant.unrar.rarfile.FileHeader;
import icu.etl.annotation.EasyBean;
import icu.etl.log.STD;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

@EasyBean(name = "rar")
public class RarCompress implements Compress {

    private volatile boolean terminate = false;

    private Archive archive;

    private File rarFile;

    public RarCompress() {
    }

    public void archiveFile(File file, String dir) {
        throw new UnsupportedOperationException();
    }

    public void archiveFile(File file, String dir, String charset) {
        throw new UnsupportedOperationException();
    }

    public void close() {
        IO.close(this.archive);
    }

    /**
     * 解压rar文件
     *
     * @param outputDir   解压后根目录（null表示解压到当前目录）
     * @param charsetName 为null即可
     * @throws IOException
     */
    public void extract(String outputDir, String charsetName) throws IOException {
        if (StringUtils.isBlank(outputDir)) {
            outputDir = this.rarFile.getParentFile().getAbsolutePath();
        }

        List<FileHeader> headers = this.archive.getFileHeaders();
        for (FileHeader head : headers) {
            if (this.terminate) {
                break;
            }

            this.unrar(outputDir, charsetName, head);
        }
    }

    /**
     * 解压rar文件
     *
     * @param outputDir   解压后根目录（null表示解压到当前目录）
     * @param charsetName 为null即可
     * @param entryName   解压指定文件
     * @throws IOException
     */
    public void extract(String outputDir, String charsetName, String entryName) throws IOException {
        List<FileHeader> headers = this.archive.getFileHeaders();
        for (FileHeader head : headers) {
            if (this.terminate) {
                break;
            }

            String name = head.isUnicode() ? head.getFileNameW() : head.getFileNameString(); // 文件entryName
            name = name.replace('\\', '/');
            if (entryName != null && name.equals(entryName)) {
                this.unrar(outputDir, charsetName, head);
            }
        }
    }

    /**
     * 解压rar文件
     *
     * @param outputDir   解压后根目录（null表示解压到当前目录）
     * @param charsetName 为null即可
     * @param head        头信息
     * @throws IOException
     */
    public void unrar(String outputDir, String charsetName, FileHeader head) throws IOException {
        String name = head.isUnicode() ? head.getFileNameW() : head.getFileNameString(); // 文件entryName
        String filepath = FileUtils.replaceFolderSeparator(FileUtils.joinFilepath(outputDir, name));
        if (head.isDirectory()) {
            if (STD.out.isDebugEnabled()) {
                STD.out.debug("unrar " + filepath + " ..");
            }
            File dir = new File(filepath);
            FileUtils.createDirectory(dir);
        } else {
            if (STD.out.isDebugEnabled()) {
                STD.out.debug("unrar " + filepath + " ..");
            }

            File file = new File(filepath);
            FileUtils.createDirectory(file.getParentFile());
            FileOutputStream os = new FileOutputStream(file, false);
            try {
                this.archive.extractFile(head, os);
            } catch (RarException e) {
                throw new IOException(outputDir, e);
            } finally {
                IO.close(os);
            }
        }
    }

    public List<FileHeader> getEntrys(String charsetName, String regex, boolean ignoreCase) {
        List<FileHeader> headers = this.archive.getFileHeaders();
        List<FileHeader> list = new ArrayList<FileHeader>(headers.size());
        for (FileHeader head : headers) {
            if (this.terminate) {
                break;
            }

            if (regex != null) {
                String name = head.isUnicode() ? head.getFileNameW() : head.getFileNameString(); // 文件entryName
                name = name.replace('\\', '/');
                String fname = FileUtils.getFilename(regex);
                if (ignoreCase) {
                    if (name.equalsIgnoreCase(fname)) {
                        list.add(head);
                    }
                } else {
                    if (name.equals(fname) || name.matches(regex)) {
                        list.add(head);
                    }
                }
            }
        }
        return list;
    }

    public void setFile(File file) {
        IO.close(this.archive);
        try {
            this.archive = new Archive(file);
            this.rarFile = file;
            if (STD.out.isDebugEnabled()) {
                this.archive.getMainHeader().print();
            }
        } catch (Exception e) {
            throw new RuntimeException("setFile(" + file + ")", e);
        }
    }

    public boolean removeEntry(String charsetName, String... entryName) {
        throw new UnsupportedOperationException();
    }

    public void terminate() {
        this.terminate = true;
    }

    public boolean isTerminate() {
        return this.terminate;
    }

}
