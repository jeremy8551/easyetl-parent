package icu.etl.increment;

import java.util.Comparator;
import java.util.List;

import icu.etl.concurrent.ThreadSource;
import icu.etl.io.TableColumnComparator;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileWriter;
import icu.etl.printer.Progress;
import icu.etl.sort.TableFileSortContext;
import icu.etl.util.Ensure;

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
    private IncrementListenerImpl logger;
    private IncrementReplaceListener replaceList;
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
        this.arith = new IncrementArithImpl();
        this.comparator = new TableColumnComparator();
    }

    /**
     * 返回任务名
     *
     * @return 任务名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置任务名
     *
     * @param name 任务名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回新文件
     *
     * @param file 文件
     */
    public void setNewFile(TextTableFile file) {
        this.newFile = file;
    }

    /**
     * 设置新文件
     *
     * @return 文件
     */
    public TextTableFile getNewFile() {
        return newFile;
    }

    /**
     * 设置旧文件
     *
     * @param file 文件
     */
    public void setOldFile(TextTableFile file) {
        this.oldFile = file;
    }

    /**
     * 返回旧文件
     *
     * @return 文件
     */
    public TextTableFile getOldFile() {
        return oldFile;
    }

    /**
     * 增量剥离监听器
     *
     * @return 监听器集合
     */
    public List<IncrementListener> getListeners() {
        return listeners;
    }

    /**
     * 增量剥离监听器
     *
     * @param listeners 监听器集合
     */
    public void setListeners(List<IncrementListener> listeners) {
        this.listeners = listeners;
    }

    /**
     * 返回剥离增量算法
     *
     * @return 算法实现对象
     */
    public IncrementArith getArith() {
        return arith;
    }

    /**
     * 剥离增量算法
     *
     * @param arith 算法实现对象
     */
    public void setArith(IncrementArith arith) {
        this.arith = Ensure.notNull(arith);
    }

    /**
     * 返回新增数据的输出流
     *
     * @return 返回输出流
     */
    public TextTableFileWriter getNewWriter() {
        return newOuter;
    }

    /**
     * 设置新增数据的输出流
     *
     * @param out 输出流
     */
    public void setNewWriter(TextTableFileWriter out) {
        this.newOuter = out;
    }

    /**
     * 返回变更数据的输出流
     *
     * @return 输出流
     */
    public TextTableFileWriter getUpdWriter() {
        return updOuter;
    }

    /**
     * 设置变更数据的输出流
     *
     * @param out 输出流
     */
    public void setUpdWriter(TextTableFileWriter out) {
        this.updOuter = out;
    }

    /**
     * 返回删除数据的输出流
     *
     * @return 输出流
     */
    public TextTableFileWriter getDelWriter() {
        return delOuter;
    }

    /**
     * 设置删除数据的输出流
     *
     * @param out 输出流
     */
    public void setDelWriter(TextTableFileWriter out) {
        this.delOuter = out;
    }

    /**
     * 返回位置信息
     *
     * @return 位置信息
     */
    public IncrementPosition getPosition() {
        return this.position;
    }

    /**
     * 设置位置信息
     *
     * @param position 位置信息
     */
    public void setPosition(IncrementPosition position) {
        this.position = position;
    }

    /**
     * 返回排序规则
     *
     * @return 排序规则
     */
    public Comparator<String> getComparator() {
        return this.comparator;
    }

    /**
     * 设置排序规则
     *
     * @param comparator 排序规则
     */
    public void setComparator(Comparator<String> comparator) {
        this.comparator = Ensure.notNull(comparator);
    }

    /**
     * 返回日志输出接口
     *
     * @return 监听器
     */
    public IncrementListenerImpl getLogger() {
        return this.logger;
    }

    /**
     * 设置日志输出接口
     *
     * @param logger 监听器
     */
    public void setLogger(IncrementListenerImpl logger) {
        this.logger = logger;
    }

    /**
     * 返回替换字段的处理器集合
     *
     * @return 监听器
     */
    public IncrementReplaceListener getReplaceList() {
        return this.replaceList;
    }

    /**
     * 设置替换字符的处理器集合
     *
     * @param listener 监听器
     */
    public void setReplaceList(IncrementReplaceListener listener) {
        this.replaceList = listener;
    }

    /**
     * 设置新数据读取进度接口
     *
     * @return 进度输出接口
     */
    public Progress getNewfileProgress() {
        return this.newfileProgress;
    }

    /**
     * 返回新数据读取进度接口
     *
     * @param progress 进度输出接口
     */
    public void setNewfileProgress(Progress progress) {
        this.newfileProgress = progress;
    }

    /**
     * 返回旧数据读取进度接口
     *
     * @return 进度输出接口
     */
    public Progress getOldfileProgress() {
        return this.oldfileProgress;
    }

    /**
     * 设置旧数据读取进度接口
     *
     * @param progress 进度输出接口
     */
    public void setOldfileProgress(Progress progress) {
        this.oldfileProgress = progress;
    }

    /**
     * 返回旧数据的排序上下文信息
     *
     * @return 排序上下文信息
     */
    public TableFileSortContext getOldfileSortContext() {
        return oldfileSortContext;
    }

    /**
     * 设置旧数据排序配置信息
     *
     * @param context 排序上下文信息
     */
    public void setSortOldContext(TableFileSortContext context) {
        this.oldfileSortContext = context;
    }

    /**
     * 返回新数据排序配置信息
     *
     * @return 排序上下文信息
     */
    public TableFileSortContext getNewfileSortContext() {
        return this.newfileSortContext;
    }

    /**
     * 设置新数据排序配置信息
     *
     * @param context 排序上下文信息
     */
    public void setSortNewContext(TableFileSortContext context) {
        this.newfileSortContext = context;
    }

    /**
     * 判断是否排序新文件
     *
     * @return 返回true表示排序新文件 false表示排序新文件
     */
    public boolean isSortNewfile() {
        return sortNewFile;
    }

    /**
     * 设置是否排序新文件
     *
     * @param b 设置true表示排序新文件 false表示不排序新文件
     */
    public void setSortNewfile(boolean b) {
        this.sortNewFile = b;
    }

    /**
     * 判断是否排序旧文件
     *
     * @return 返回true表示排序旧文件 false表示不排序旧文件
     */
    public boolean isSortOldfile() {
        return this.sortOldFile;
    }

    /**
     * 设置是否排序旧文件
     *
     * @param b true表示排序旧文件 false表示不排序旧文件
     */
    public void setSortOldfile(boolean b) {
        this.sortOldFile = b;
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