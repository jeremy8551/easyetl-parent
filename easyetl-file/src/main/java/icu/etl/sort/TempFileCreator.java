package icu.etl.sort;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.Dates;
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
    private final static Log log = LogFactory.getLog(TempFileCreator.class);

    /** 找到一个可用文件名的超时时间, 10秒 */
    public static long FINDFILE_TIMEOUT = 10 * 1000;

    private Object lock1 = new Object();

    private Object lock2 = new Object();

    private Object lock3 = new Object();

    /** 排序文件名 */
    private String filename;

    /** 排序文件 */
    private File file;

    /** 临时文件存储目录 */
    private File parent;

    /**
     * 初始化
     *
     * @param dir  临时文件存储目录
     * @param file 数据文件
     */
    public TempFileCreator(File dir, File file) throws IOException {
        this.filename = FileUtils.getFilenameNoSuffix(file.getName());
        this.file = file;

        // 使用设定目录
        if (dir != null) {
            this.parent = new File(dir, "." + this.filename);

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getIoxMessage(55, this.parent.getAbsolutePath()));
            }

            if (FileUtils.createDirectory(this.parent)) {
                return;
            } else {
                throw new IOException(ResourcesUtils.getIoxMessage(46, this.parent.getAbsolutePath()));
            }
        }

        // 尝试使用数据文件所在目录
        this.parent = new File(file.getParentFile(), "." + this.filename);
        if (this.parent.exists()) {
            if (!this.parent.isDirectory()) {
                throw new IOException(ResourcesUtils.getIoxMessage(50, this.parent.getAbsolutePath()));
            }

            List<File> list = FileUtils.isWriting(this.parent, 500);
            if (list.size() > 0) {
                StringBuilder buf = new StringBuilder(100);
                for (File mf : list) {
                    buf.append('\n').append(mf.getAbsolutePath());
                }
                throw new IOException(ResourcesUtils.getIoxMessage(47, this.parent.getAbsolutePath(), buf));
            }

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getIoxMessage(55, this.parent.getAbsolutePath()));
            }

            if (FileUtils.clearDirectory(this.parent)) { // 清空目录中的临时文件
                return;
            } else {
                throw new IOException(ResourcesUtils.getIoxMessage(47, this.parent.getAbsolutePath()));
            }
        } else {
            FileUtils.createDirectory(this.parent);
            if (this.parent.exists()) {
                return;
            }
        }

        // 使用临时目录
        this.parent = new File(FileUtils.getTempDir(TableFileDeduplicateSorter.class), "." + this.filename);
        if (this.parent.exists()) {
            if (this.parent.isDirectory()) {
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getIoxMessage(55, this.parent.getAbsolutePath()));
                }
                
                FileUtils.clearDirectory(this.parent);
            } else {
                throw new IOException(ResourcesUtils.getIoxMessage(50, this.parent.getAbsolutePath()));
            }
        } else {
            FileUtils.createDirectory(this.parent);
            if (this.parent.exists()) {
                return;
            } else {
                throw new IOException(ResourcesUtils.getIoxMessage(46, this.parent.getAbsolutePath()));
            }
        }
    }

    /**
     * 返回最终排序结果文件
     *
     * @return 排序结果文件
     */
    public File toSortfile() throws IOException {
        File file = new File(FileUtils.changeFilenameExt(this.file.getAbsolutePath(), "sort"));
        long time = System.currentTimeMillis();
        int no = 1;
        while (file.exists()) {
            file = new File(this.file.getParentFile(), FileUtils.getFilenameNoSuffix(this.file.getName()) + ".sort" + no++);
            if (System.currentTimeMillis() - time >= FINDFILE_TIMEOUT) { // 如果5秒内还有找到可用的合并文件名，则直接返回合并后的文件
                throw new IOException(ResourcesUtils.getIoxMessage(51, file.getAbsolutePath()));
            }
        }
        return file;
    }

    /**
     * 返回备份文件
     *
     * @return 备份文件
     */
    public File toBakfile() throws IOException {
        int no = 1;
        File file = new File(this.file.getAbsolutePath() + no++);
        long time = System.currentTimeMillis();
        while (file.exists()) {
            file = new File(this.file.getAbsolutePath() + no++);
            if (System.currentTimeMillis() - time >= FINDFILE_TIMEOUT) { // 查找不重复的文件名用了10秒，报错
                throw new IOException(ResourcesUtils.getIoxMessage(51, file.getAbsolutePath()));
            }
        }
        return file;
    }

    /**
     * 删除排序文件产生的临时文件
     */
    public void deleteTempfiles() throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getIoxMessage(52, this.parent.getAbsolutePath()));
        }

        if (!FileUtils.delete(this.parent)) {
            throw new IOException(ResourcesUtils.getIoxMessage(49, this.parent.getAbsolutePath()));
        }
    }

    /**
     * 生成清单文件
     *
     * @return 清单文件
     * @throws IOException 查找文件超时
     */
    public File toListfile() throws IOException {
        synchronized (this.lock1) {
            File file = new File(this.parent, "list" + Dates.format17(new Date()));
            long time = System.currentTimeMillis();
            while (file.exists()) {
                file = new File(this.file.getParentFile(), "list" + Dates.format17(new Date()));
                if (System.currentTimeMillis() - time >= FINDFILE_TIMEOUT) { // 查找不重复的文件名用了10秒，报错
                    throw new IOException(ResourcesUtils.getIoxMessage(51, file.getAbsolutePath()));
                }
            }
            FileUtils.createFile(file);
            return file;
        }
    }

    /**
     * 生成合并后的临时文件
     *
     * @return 临时文件
     * @throws IOException 查找文件超时
     */
    public File toMergeFile() throws IOException {
        synchronized (this.lock2) {
            File mergefile = new File(this.parent, StringUtils.toRandomUUID());
            long time = System.currentTimeMillis();
            while (mergefile.exists()) {
                mergefile = new File(file.getParentFile(), StringUtils.toRandomUUID());
                if (System.currentTimeMillis() - time >= FINDFILE_TIMEOUT) { // 查找不重复的文件名用了10秒，报错
                    throw new IOException(ResourcesUtils.getIoxMessage(51, file.getAbsolutePath()));
                }
            }
            FileUtils.createFile(mergefile);
            return mergefile;
        }
    }

    /**
     * 生成临时文件
     *
     * @return 临时文件
     * @throws IOException 查找文件超时
     */
    public File toTempFile() throws IOException {
        synchronized (this.lock3) {
            File file = new File(this.parent, StringUtils.toRandomUUID());
            long time = System.currentTimeMillis();
            while (file.exists()) {
                file = new File(file.getParentFile(), StringUtils.toRandomUUID());
                if (System.currentTimeMillis() - time >= FINDFILE_TIMEOUT) { // 查找不重复的文件名用了10秒，报错
                    throw new IOException(ResourcesUtils.getIoxMessage(51, file.getAbsolutePath()));
                }
            }
            FileUtils.createFile(file);
            return file;
        }
    }

}
