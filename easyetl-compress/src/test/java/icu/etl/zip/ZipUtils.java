package icu.etl.zip;

import java.io.File;
import java.io.IOException;

import icu.etl.ioc.EasyContext;
import icu.etl.util.CharsetName;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

/**
 * 压缩工具类
 *
 * @author jeremy8551@qq.com
 * @createtime 2009-12-19
 */
public class ZipUtils {

    public ZipUtils() {
    }

    /**
     * 将文件或目录 {@code file} 压缩到 {@code compressFile} 文件中
     *
     * @param context      容器上下文信息
     * @param file         文件或目录
     * @param compressFile 压缩文件（依据压缩文件后缀rar, zip, tar, gz等自动选择压缩算法）
     * @param charsetName  压缩文件字符集（为空时默认使用UTF-8）
     * @param delete       true表示文件全部压缩成功后自动删除 {@code fileOrDir}
     * @throws IOException 访问文件错误
     */
    public static void compress(EasyContext context, File file, File compressFile, String charsetName, boolean delete) throws IOException {
        Compress c = context.getBean(Compress.class, FileUtils.getFilenameSuffix(compressFile.getName()));
        try {
            c.setFile(compressFile);
            c.archiveFile(file, null, StringUtils.defaultString(charsetName, CharsetName.UTF_8));
        } finally {
            c.close();
        }

        if (delete) {
            FileUtils.assertDelete(file);
        }
    }

    /**
     * 将压缩文件 {@code file} 解压文件到目录 {@code dir} 下
     *
     * @param context      容器上下文信息
     * @param compressFile 压缩文件
     * @param dir          解压的目录
     * @param charsetName  压缩包文件字符集（为空时默认为UTF-8）
     * @param delete       true表示全部文件解压成功后自动删除压缩文件参数file
     * @throws IOException 访问文件错误
     */
    public static void uncompress(EasyContext context, File compressFile, File dir, String charsetName, boolean delete) throws IOException {
        if (StringUtils.isBlank(charsetName)) {
            charsetName = CharsetName.UTF_8;
        }

        Compress c = context.getBean(Compress.class, FileUtils.getFilenameSuffix(compressFile.getName()));
        try {
            c.setFile(compressFile);
            c.extract(dir.getAbsolutePath(), charsetName);
        } finally {
            c.close();
        }

        if (delete) {
            FileUtils.assertDelete(compressFile);
        }
    }

}
