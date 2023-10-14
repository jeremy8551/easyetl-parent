package icu.etl.mail;

import java.io.File;
import java.io.IOException;

import icu.apache.mail.common.EmailAttachment;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import icu.etl.zip.ZipUtils;

/**
 * 用于描述邮件文件
 */
public class MailFile {

    private String disposition = EmailAttachment.ATTACHMENT;

    private File file;

    private String description;

    private String name;

    public MailFile(String disposition, File file, String name, String description) throws IOException {
        this(file);
        this.disposition = disposition;
        this.name = name;
        this.description = description;
    }

    public MailFile(File file, String name, String description) throws IOException {
        this(file);
        this.name = name;
        this.description = description;
    }

    public MailFile(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException();
        }

        if (file.exists() && file.isDirectory()) {
            File compressFile = FileUtils.getFileNoRepeat(FileUtils.getTempDir(MailFile.class), FileUtils.changeFilenameExt(file.getName(), "zip"));
            FileUtils.createFile(compressFile);
            ZipUtils.compress(file, compressFile, StringUtils.CHARSET, false);
            this.file = compressFile;
            this.name = FileUtils.changeFilenameExt(file.getName(), "zip");
            this.description = file.getName();
        } else {
            this.file = file;
        }
    }

    public String getDisposition() {
        return StringUtils.defaultString(this.disposition, EmailAttachment.ATTACHMENT);
    }

    public File getFile() {
        return file;
    }

    public String getPath() {
        return this.file.getAbsolutePath();
    }

    public String getDescription() {
        return StringUtils.defaultString(description, this.file.getName());
    }

    public String getName() {
        return StringUtils.defaultString(this.name, this.file.getName());
    }

}
