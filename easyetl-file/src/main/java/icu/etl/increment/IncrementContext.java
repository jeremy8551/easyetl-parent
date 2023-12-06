package icu.etl.increment;

import java.util.Comparator;
import java.util.List;

import icu.etl.concurrent.ThreadSource;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileWriter;
import icu.etl.printer.Progress;
import icu.etl.sort.TableFileSortContext;

/**
 * 剥离增量配置信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2010-01-19 07:00:39
 */
public class IncrementContext {

    private String name;
    private TextTableFile newFile;
    private TextTableFile oldFile;
    private boolean sortNewFile;
    private boolean sortOldFile;
    private List<IncrementListener> listeners;
    private IncrementArith arith;
    private TextTableFileWriter newOuter;
    private TextTableFileWriter updOuter;
    private TextTableFileWriter delOuter;
    private IncrementPosition position;
    private Comparator<String> comparator;
    private IncrementLoggerListener logger;
    private IncrementReplaceList replaceList;
    private Progress newfileProgress;
    private Progress oldfileProgress;
    private TableFileSortContext oldfileSortContext;
    private TableFileSortContext newfileSortContext;
    private ThreadSource threadSource;

    /**
     * 初始化
     */
    public IncrementContext() {
        this.sortNewFile = true;
        this.sortOldFile = true;
    }

    /**
     * 返回任务名
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 设置任务名
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回新文件
     *
     * @param newFile
     */
    public void setNewFile(TextTableFile newFile) {
        this.newFile = newFile;
    }

    /**
     * 设置新文件
     *
     * @return
     */
    public TextTableFile getNewFile() {
        return newFile;
    }

    /**
     * 设置旧文件
     *
     * @param oldFile
     */
    public void setOldFile(TextTableFile oldFile) {
        this.oldFile = oldFile;
    }

    /**
     * 返回旧文件
     *
     * @return
     */
    public TextTableFile getOldFile() {
        return oldFile;
    }

    /**
     * 增量剥离监听器
     *
     * @return
     */
    public List<IncrementListener> getListeners() {
        return listeners;
    }

    /**
     * 增量剥离监听器
     *
     * @param listeners
     */
    public void setListeners(List<IncrementListener> listeners) {
        this.listeners = listeners;
    }

    /**
     * 返回剥离增量算法
     *
     * @return
     */
    public IncrementArith getArith() {
        return arith;
    }

    /**
     * 剥离增量算法
     *
     * @param arith
     */
    public void setArith(IncrementArith arith) {
        this.arith = arith;
    }

    /**
     * 返回新增数据的输出流
     *
     * @return
     */
    public TextTableFileWriter getNewWriter() {
        return newOuter;
    }

    /**
     * 设置新增数据的输出流
     *
     * @param newOuter
     */
    public void setNewWriter(TextTableFileWriter newOuter) {
        this.newOuter = newOuter;
    }

    /**
     * 返回变更数据的输出流
     *
     * @return
     */
    public TextTableFileWriter getUpdWriter() {
        return updOuter;
    }

    /**
     * 设置变更数据的输出流
     *
     * @param updOuter
     */
    public void setUpdWriter(TextTableFileWriter updOuter) {
        this.updOuter = updOuter;
    }

    /**
     * 返回删除数据的输出流
     *
     * @return
     */
    public TextTableFileWriter getDelWriter() {
        return delOuter;
    }

    /**
     * 设置删除数据的输出流
     *
     * @param delOuter
     */
    public void setDelWriter(TextTableFileWriter delOuter) {
        this.delOuter = delOuter;
    }

    /**
     * 返回位置信息
     *
     * @return
     */
    public IncrementPosition getPosition() {
        return position;
    }

    /**
     * 设置位置信息
     *
     * @param position
     */
    public void setPosition(IncrementPosition position) {
        this.position = position;
    }

    /**
     * 返回排序规则
     *
     * @return
     */
    public Comparator<String> getComparator() {
        return comparator;
    }

    /**
     * 设置排序规则
     *
     * @param comparator
     */
    public void setComparator(Comparator<String> comparator) {
        this.comparator = comparator;
    }

    /**
     * 返回日志输出接口
     *
     * @return
     */
    public IncrementLoggerListener getLogger() {
        return logger;
    }

    /**
     * 设置日志输出接口
     *
     * @param logger
     */
    public void setLogger(IncrementLoggerListener logger) {
        this.logger = logger;
    }

    /**
     * 返回替换字段的处理器集合
     *
     * @return
     */
    public IncrementReplaceList getReplaceList() {
        return replaceList;
    }

    /**
     * 设置替换字符的处理器集合
     *
     * @param replaceList
     */
    public void setReplaceList(IncrementReplaceList replaceList) {
        this.replaceList = replaceList;
    }

    /**
     * 设置新数据读取进度接口
     *
     * @return
     */
    public Progress getNewfileProgress() {
        return newfileProgress;
    }

    /**
     * 返回新数据读取进度接口
     *
     * @param newfileProgress
     */
    public void setNewfileProgress(Progress newfileProgress) {
        this.newfileProgress = newfileProgress;
    }

    /**
     * 返回旧数据读取进度接口
     *
     * @return
     */
    public Progress getOldfileProgress() {
        return oldfileProgress;
    }

    /**
     * 设置旧数据读取进度接口
     *
     * @param oldfileProgress
     */
    public void setOldfileProgress(Progress oldfileProgress) {
        this.oldfileProgress = oldfileProgress;
    }

    /**
     * 返回旧数据排序配置信息
     *
     * @return
     */
    public TableFileSortContext getOldfileSortContext() {
        return oldfileSortContext;
    }

    /**
     * 设置旧数据排序配置信息
     *
     * @param oldfileSortContext
     */
    public void setSortOldContext(TableFileSortContext oldfileSortContext) {
        this.oldfileSortContext = oldfileSortContext;
    }

    /**
     * 返回新数据排序配置信息
     *
     * @return
     */
    public TableFileSortContext getNewfileSortContext() {
        return newfileSortContext;
    }

    /**
     * 设置新数据排序配置信息
     *
     * @param newfileSortContext
     */
    public void setSortNewContext(TableFileSortContext newfileSortContext) {
        this.newfileSortContext = newfileSortContext;
    }

    /**
     * 返回 true 表示排序新数据文件
     *
     * @return
     */
    public boolean sortNewFile() {
        return sortNewFile;
    }

    /**
     * 设置 true 表示排序新数据文件
     *
     * @param sortNewFile
     */
    public void setSortNew(boolean sortNewFile) {
        this.sortNewFile = sortNewFile;
    }

    /**
     * 返回 true 表示排序旧数据文件
     *
     * @return
     */
    public boolean sortOldFile() {
        return sortOldFile;
    }

    /**
     * 设置 true 表示排序旧数据文件
     *
     * @param sortOldFile
     */
    public void setSortOld(boolean sortOldFile) {
        this.sortOldFile = sortOldFile;
    }

    /**
     * 设置线程池
     *
     * @return 线程池
     */
    public ThreadSource getThreadSource() {
        return threadSource;
    }

    /**
     * 返回线程池
     *
     * @param service 线程池
     */
    public void setThreadSource(ThreadSource service) {
        this.threadSource = service;
    }
}