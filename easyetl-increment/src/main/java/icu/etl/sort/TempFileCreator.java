package icu.etl.sort;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 临时文件工厂
 *
 * @author jeremy8551@qq.com
 * @createtime 2023-09-23
 */
public class TempFileCreator {

    private final Object lock1 = new Object();

    private final Object lock2 = new Object();

    private final Object lock3 = new Object();

    /** 排序文件 */
    private File file;

    /** 临时文件存储目录 */
    private File parent;

    /**
     * 初始化
     *
     * @param dir  目录，在这个目录下建立临时文件目录
     * @param file 数据文件
     */
    public TempFileCreator(File dir, File file) throws IOException {
        this.file = Ensure.notNull(file);

        if (dir == null) {
            dir = file.getParentFile(); // 使用文件所在目录
        }

        // 使用文件所在目录
        if (this.create(dir)) {
            return;
        }

        // 直接在数据文件所在的目录下创建临时文件
        File newfile = FileUtils.allocate(file.getParentFile(), null);
        if (FileUtils.createFile(newfile) && FileUtils.delete(newfile)) {
            this.parent = null;
            return;
        }

        // 使用临时目录
        if (this.create(FileUtils.getTempDir(TempFileCreator.class.getSimpleName(), "sort", "file"))) {
            return;
        }

        throw new IOException(ResourcesUtils.getIoxMessage(46, this.parent.getAbsolutePath()));
    }

    /**
     * 创建目录
     *
     * @param dir 目录
     * @return 返回true表示操作成功 false表示操作失败
     */
    protected boolean create(File dir) {
        String dirName = "." + FileUtils.getFilenameNoSuffix(this.file.getName());
        this.parent = new File(dir, dirName);

        // 创建目录
        if (!FileUtils.createDirectory(this.parent)) {
            this.parent = null;
            return false;
        }
        FileUtils.assertClearDirectory(this.parent);

        // 尝试创建文件
        File newfile = FileUtils.allocate(this.parent, null);
        if (FileUtils.createFile(newfile)) {
            return FileUtils.deleteFile(newfile);
        } else {
            this.parent = null;
            return false;
        }
    }

    /**
     * 返回最终排序结果文件
     *
     * @return 排序结果文件
     */
    public File toSortfile() {
        String filename = FileUtils.changeFilenameExt(this.file.getName(), "sort");
        return FileUtils.allocate(this.file.getParentFile(), filename);
    }

    /**
     * 返回备份文件
     *
     * @return 备份文件
     */
    public File toBakfile() {
        return FileUtils.allocate(this.file.getParentFile(), this.file.getName());
    }

    /**
     * 删除排序文件产生的临时文件
     */
    public void deleteTempfiles() {
        if (this.parent == null) {
            File parent = this.file.getParentFile();
            File[] array = FileUtils.array(parent.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    String filename = FileUtils.getFilenameNoExt(name);
                    String ext = FileUtils.getFilenameExt(name);
                    String prefix = file.getName();
                    if (filename.startsWith(prefix) && StringUtils.inArray(ext, "list", "merge", "temp")) {
                        String part = filename.substring(prefix.length());
                        if (StringUtils.isNumber(part)) {
                            return true;
                        }
                    }
                    return false;
                }
            }));

            for (File file : array) {
                FileUtils.deleteFile(file);
            }
        } else {
            FileUtils.delete(this.parent, 10, 100);
        }
    }

    /**
     * 生成清单文件
     *
     * @return 清单文件
     */
    public File toListfile() {
        synchronized (this.lock1) {
            if (this.parent == null) {
                return FileUtils.createNewFile(this.file.getParentFile(), this.file.getName() + ".list");
            } else {
                return FileUtils.createNewFile(this.parent, "list" + Dates.format17());
            }
        }
    }

    /**
     * 生成合并后的临时文件
     *
     * @return 临时文件
     */
    public File toMergeFile() {
        synchronized (this.lock2) {
            if (this.parent == null) {
                return FileUtils.createNewFile(this.file.getParentFile(), this.file.getName() + ".merge");
            } else {
                return FileUtils.createNewFile(this.parent, "merge" + Dates.format17());
            }
        }
    }

    /**
     * 生成临时文件
     *
     * @return 临时文件
     */
    public File toTempFile() {
        synchronized (this.lock3) {
            if (this.parent == null) {
                return FileUtils.createNewFile(this.file.getParentFile(), this.file.getName() + ".temp");
            } else {
                return FileUtils.createNewFile(this.parent, "temp" + Dates.format17());
            }
        }
    }

}
