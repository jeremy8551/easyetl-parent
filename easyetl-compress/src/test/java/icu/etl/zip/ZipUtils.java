package icu.etl.zip;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import icu.etl.ioc.EasyetlContext;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
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
     * 将文件或目录参数 fileOrDir 压缩到参数 compressFile 文件中
     *
     * @param context      容器上下文信息
     * @param fileOrDir    文件或目录
     * @param compressFile 压缩文件（依据压缩文件后缀rar, zip, tar, gz等自动选择压缩算法）
     * @param charsetName  压缩文件字符集（为空时默认使用UTF-8）
     * @param delete       true表示文件全部压缩成功后自动删除 {@code fileOrDir}
     * @throws IOException
     */
    public static void compress(EasyetlContext context, File fileOrDir, File compressFile, String charsetName, boolean delete) throws IOException {
        Compress compress = context.get(Compress.class, FileUtils.getFilenameSuffix(compressFile.getName()));
        try {
            compress.setFile(compressFile);
            compress.archiveFile(fileOrDir, null, StringUtils.defaultString(charsetName, StandardCharsets.UTF_8.name()));
        } finally {
            IO.close(compress);
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

    /**
     * 将压缩文件参数 file 解压文件到指定目录参数 dir 下
     *
     * @param context     容器上下文信息
     * @param file        文件, 压缩包（根据文件后缀自动选择压缩工具）
     * @param dir         解压的目录
     * @param charsetName 压缩包文件字符集（为空时默认为UTF-8）
     * @param delete      true表示全部文件解压成功后自动删除压缩文件参数file
     * @throws IOException
     */
    public static void uncompress(EasyetlContext context, File file, File dir, String charsetName, boolean delete) throws IOException {
        Compress compress = context.get(Compress.class, FileUtils.getFilenameSuffix(file.getName()));
        try {
            compress.setFile(file);
            compress.extract(dir.getAbsolutePath(), StringUtils.defaultString(charsetName, StandardCharsets.UTF_8.name()));
        } finally {
            IO.close(compress);
        }

        if (delete && !FileUtils.deleteFile(file)) {
            throw new RuntimeException("uncompress(" + file + ", " + dir + ", " + charsetName + ", " + delete + ")");
        }
    }

}
