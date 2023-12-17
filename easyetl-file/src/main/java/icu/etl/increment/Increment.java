package icu.etl.increment;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import icu.etl.concurrent.AbstractJob;
import icu.etl.io.CommonTextTableFileReaderListener;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableFileWriter;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.printer.Progress;
import icu.etl.sort.OrderByExpression;
import icu.etl.sort.TableFileDeduplicateSorter;
import icu.etl.sort.TableFileSortContext;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.TimeWatch;

/**
 * 剥离增量任务
 *
 * @author jeremy8551@qq.com
 * @createtime 2010-01-19 02:45:22
 */
public class Increment extends AbstractJob {
    private final static Log log = LogFactory.getLog(Increment.class);

    /** 任务的配置信息 */
    private IncrementContext context;

    /**
     * 初始化
     *
     * @param context 上下文信息
     */
    public Increment(IncrementContext context) {
        super();
        this.context = Ensure.notNull(context);
        new IncrementContextValidator(context);
        this.setName(context.getName());
    }

    public int execute() throws Exception {
        TimeWatch watch = new TimeWatch();
        TextTableFile oldfile = this.context.getOldFile();
        TextTableFile newfile = this.context.getNewFile();
        IncrementArith arith = this.context.getArith();
        TextTableFileWriter newOuter = this.context.getNewWriter();
        TextTableFileWriter updOuter = this.context.getUpdWriter();
        TextTableFileWriter delOuter = this.context.getDelWriter();
        boolean sortNewFile = this.context.sortNewFile();
        boolean sortOldFile = this.context.sortOldFile();
        IncrementPosition position = this.context.getPosition();
        List<IncrementListener> listeners = this.context.getListeners();
        IncrementReplaceListener replaceList = this.context.getReplaceList();
        IncrementListenerImpl logger = this.context.getLogger();
        Progress newfileProgress = this.context.getNewfileProgress();
        Progress oldfileProgress = this.context.getOldfileProgress();
        TableFileSortContext newfileCxt = this.context.getNewfileSortContext();
        TableFileSortContext oldfileCxt = this.context.getOldfileSortContext();
        Comparator<String> comparator = this.context.getComparator();

        // 设置字符串排序的规则
        IncrementRuler ruler = new IncrementTableRuler(comparator, position);

        // 保留排序前的文件路径
        File beforeSortNewfile = newfile.getFile();
        File beforeSortOldfile = oldfile.getFile();

        // 保留排序后的文件路径
        File afterSortNewfile = newfile.getFile();
        File afterSortOldfile = oldfile.getFile();

        try {
            if (sortNewFile) { // 排序新数据
                TableFileDeduplicateSorter tfs = new TableFileDeduplicateSorter(newfileCxt);
                try {
                    this.status.add(tfs);
                    OrderByExpression[] orders = this.valueOf(position.getNewIndexPosition(), comparator);
                    afterSortNewfile = tfs.execute(newfile, orders);
                } finally {
                    this.status.remove(tfs);
                }
            }
        } finally {
            if (sortOldFile) { // 排序旧数据
                TableFileDeduplicateSorter tfs = new TableFileDeduplicateSorter(oldfileCxt);
                try {
                    this.status.add(tfs);
                    OrderByExpression[] orders = this.valueOf(position.getOldIndexPosition(), comparator);
                    afterSortOldfile = tfs.execute(oldfile, orders);
                } finally {
                    this.status.remove(tfs);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getIncrementMessage(3, this.getName()));
        }

        // 开始执行增量剥离
        try {
            this.status.add(arith);

            // 使用排序后的文件作为剥离增量依据
            oldfile.setAbsolutePath(afterSortOldfile.getAbsolutePath());
            newfile.setAbsolutePath(afterSortNewfile.getAbsolutePath());

            TextTableFileReader oldIn = oldfile.getReader(oldfileCxt.getReaderBuffer());
            oldIn.setListener(new CommonTextTableFileReaderListener(oldfileProgress));

            TextTableFileReader newIn = newfile.getReader(newfileCxt.getReaderBuffer());
            newIn.setListener(new CommonTextTableFileReaderListener(newfileProgress));

            IncrementHandler out = new IncrementFileWriter(newfile, oldfile, listeners, logger, replaceList, newOuter, updOuter, delOuter);
            arith.execute(ruler, newIn, oldIn, out);

            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getIncrementMessage(4, this.getName(), watch.useTime()));
            }
            return 0;
        } finally {
            this.status.remove(arith);

            // 排序后的文件与源文件路径不同，删除排序后的文件
            if (!afterSortOldfile.equals(beforeSortOldfile) && beforeSortOldfile.exists()) {
                FileUtils.deleteFile(afterSortOldfile);
            }
            if (!afterSortNewfile.equals(beforeSortNewfile) && beforeSortNewfile.exists()) {
                FileUtils.deleteFile(afterSortNewfile);
            }
        }
    }

    private OrderByExpression[] valueOf(int[] positions, Comparator<String> comparator) {
        OrderByExpression[] array = new OrderByExpression[positions.length];
        for (int i = 0; i < positions.length; i++) {
            array[i] = new OrderByExpression(positions[i], comparator, true);
        }
        return array;
    }

    /**
     * 返回剥离增量任务的上下文信息
     *
     * @return 上下文信息
     */
    public IncrementContext getContext() {
        return context;
    }

}
