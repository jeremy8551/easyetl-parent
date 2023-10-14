package icu.etl.increment;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import icu.etl.concurrent.Executor;
import icu.etl.expression.OrderByExpression;
import icu.etl.io.CommonTextTableFileReaderListener;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableFileWriter;
import icu.etl.printer.Progress;
import icu.etl.sort.TableFileSortContext;
import icu.etl.sort.TableFileSorter;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.TimeWatch;

/**
 * 剥离增量任务
 *
 * @author jeremy8551@qq.com
 * @createtime 2010-01-19 02:45:22
 */
public class Increment extends Executor {

    /** 任务的配置信息 */
    private IncrementContext context;

    /**
     * 初始化
     *
     * @param context
     * @throws IOException
     */
    public Increment(IncrementContext context) throws IOException {
        super();
        this.context = new IncrementValidator(context).getContext();
        this.setName(context.getName());
    }

    public void execute() throws IOException {
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
        IncrementReplaceList replaceList = this.context.getReplaceList();
        IncrementLogger logger = this.context.getLogger();
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
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getIncrementMessage(1, this.getName()));
                }

                TableFileSorter tfs = new TableFileSorter(newfileCxt);
                try {
                    this.observers.add(tfs);
                    afterSortNewfile = tfs.sort(newfile, this.valueOf(position.getNewIndexPosition(), comparator, true));
                } finally {
                    this.observers.remove(tfs);
                }
            }
        } finally {
            if (sortOldFile) { // 排序旧数据
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getIncrementMessage(2, this.getName()));
                }

                TableFileSorter tfs = new TableFileSorter(oldfileCxt);
                try {
                    this.observers.add(tfs);
                    afterSortOldfile = tfs.sort(oldfile, this.valueOf(position.getOldIndexPosition(), comparator, true));
                } finally {
                    this.observers.remove(tfs);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getIncrementMessage(3, this.getName()));
        }

        // 开始执行剥离增量数据
        try {
            this.observers.add(arith);

            // 使用排序后的文件作为剥离增量依据
            oldfile.setAbsolutePath(afterSortOldfile.getAbsolutePath());
            newfile.setAbsolutePath(afterSortNewfile.getAbsolutePath());

            TextTableFileReader oldIn = oldfile.getReader(oldfileCxt.getReaderBuffer());
            oldIn.setListener(new CommonTextTableFileReaderListener(oldfileProgress));

            TextTableFileReader newIn = newfile.getReader(newfileCxt.getReaderBuffer());
            newIn.setListener(new CommonTextTableFileReaderListener(newfileProgress));

            IncrementHandler out = new IncrementFileWriter(newfile, oldfile, listeners, logger, replaceList, newOuter, updOuter, delOuter);
            arith.execute(ruler, newIn, oldIn, out);
            log.debug(ResourcesUtils.getIncrementMessage(4, this.getName(), this.getId(), watch.useTime()));
        } finally {
            this.observers.remove(arith);

            // 排序后的文件与源文件路径不同，删除排序后的文件
            if (!afterSortOldfile.equals(beforeSortOldfile) && beforeSortOldfile.exists()) {
                afterSortOldfile.delete();
            }
            if (!afterSortNewfile.equals(beforeSortNewfile) && beforeSortNewfile.exists()) {
                afterSortNewfile.delete();
            }
        }
    }

    private OrderByExpression[] valueOf(int[] positions, Comparator<String> comparator, boolean asc) {
        OrderByExpression[] array = new OrderByExpression[positions.length];
        for (int i = 0; i < positions.length; i++) {
            array[i] = new OrderByExpression(positions[i], comparator, asc);
        }
        return array;
    }

    /**
     * 返回剥离增量任务的上下文信息
     *
     * @return
     */
    public IncrementContext getContext() {
        return context;
    }

    public int getPRI() {
        return 0;
    }

}
