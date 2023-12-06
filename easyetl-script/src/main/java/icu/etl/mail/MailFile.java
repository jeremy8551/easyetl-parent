package icu.etl.mail;

import java.io.File;
import java.io.IOException;

import icu.apache.mail.common.EmailAttachment;
import icu.etl.ioc.EasyContext;
import icu.etl.util.CharsetName;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import icu.etl.zip.Compress;

/**
 * 用于描述邮件文件
 */
public class MailFile {

    private String disposition = EmailAttachment.ATTACHMENT;

    private File file;

    private String description;

    private String name;

    public MailFile(EasyContext context, String disposition, File file, String name, String description) throws IOException {
        this(context, file);
        this.disposition = disposition;
        this.name = name;
        this.description = description;
    }

    public MailFile(EasyContext context, File file, String name, String description) throws IOException {
        this(context, file);
        this.name = name;
        this.description = description;
    }

    public MailFile(EasyContext context, File file) throws IOException {
        if (file == null) {
            throw new NullPointerException();
        }

        if (file.exists() && file.isDirectory()) {
            File compressFile = FileUtils.getFileNoRepeat(FileUtils.getTempDir(MailFile.class), FileUtils.changeFilenameExt(file.getName(), "zip"));
            FileUtils.createFile(compressFile);
            this.compress(context, file, compressFile, StringUtils.CHARSET, false);
            this.file = compressFile;
            this.name = FileUtils.changeFilenameExt(file.getName(), "zip");
            this.description = file.getName();
        } else {
            this.file = file;
        }
    }

    /**
     * 将文件或目录参数 fileOrDir 压缩到参数 compressFile 文件中
     *
     * @param context      容器上下文信息
     * @param fileOrDir    文件或目录
     * @param compressFile 压缩文件（依据压缩文件后缀rar, zip, tar, gz等自动选择压缩算法）
     * @param charsetName  压缩文件字符集（为空时默认使用UTF-8）
     * @param delete       true表示文件全部压缩成功后自动删除 {@code fileOrDir}
     * @throws IOException
     */
    public void compress(EasyContext context, File fileOrDir, File compressFile, String charsetName, boolean delete) throws IOException {
        Compress compress = context.getBean(Compress.class, FileUtils.getFilenameSuffix(compressFile.getName()));
        try {
            compress.setFile(compressFile);
            compress.archiveFile(fileOrDir, null, StringUtils.defaultString(charsetName, CharsetName.UTF_8));
        } finally {
            compress.close();
        }

        if (delete) {
            if (fileOrDir.isFile()) {
                if (FileUtils.deleteFile(fileOrDir)) {
                    return;
                } else {
                    throw new RuntimeException("compress(" + fileOrDir + ", " + compressFile + ", " + charsetName + ", " + delete + ")");
                }
            }

            if (fileOrDir.isDirectory()) {
                if (FileUtils.clearDirectory(fileOrDir) && fileOrDir.delete()) {
                    return;
                } else {
                    throw new RuntimeException("compress(" + fileOrDir + ", " + compressFile + ", " + charsetName + ", " + delete + ")");
                }
            }
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
