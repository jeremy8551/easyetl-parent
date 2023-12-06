package icu.etl.increment;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import icu.etl.concurrent.AbstractJob;
import icu.etl.io.CommonTextTableFileReaderListener;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableFileWriter;
import icu.etl.io.TextTableLine;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.printer.Progress;
import icu.etl.sort.OrderByExpression;
import icu.etl.sort.TableFileDeduplicateSorter;
import icu.etl.sort.TableFileSortContext;
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
     * @throws IOException 访问文件错误
     */
    public Increment(IncrementContext context) throws IOException {
        super();
        new IncrementContextValidator(context);
        this.context = context;
        this.setName(context.getName());
    }

    public int execute() throws IOException {
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
        IncrementLoggerListener logger = this.context.getLogger();
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
                    OrderByExpression[] orders = this.valueOf(position.getNewIndexPosition(), comparator, true);
                    afterSortNewfile = tfs.execute(newfile, orders);
//                    this.check(newfileCxt, newfile, afterSortNewfile, orders);
                } finally {
                    this.status.remove(tfs);
                }
            }
        } finally {
            if (sortOldFile) { // 排序旧数据
                TableFileDeduplicateSorter tfs = new TableFileDeduplicateSorter(oldfileCxt);
                try {
                    this.status.add(tfs);
                    OrderByExpression[] orders = this.valueOf(position.getOldIndexPosition(), comparator, true);
                    afterSortOldfile = tfs.execute(oldfile, orders);
//                    this.check(oldfileCxt, oldfile, afterSortOldfile, orders);
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
                afterSortOldfile.delete();
            }
            if (!afterSortNewfile.equals(beforeSortNewfile) && beforeSortNewfile.exists()) {
                afterSortNewfile.delete();
            }
        }
    }

    /**
     * 检查表格型文件中是否有重复数据
     *
     * @param cxt       上下文信息
     * @param tableFile 数据文件（需要排序的文件）
     * @param sortfile  排序后的文件
     * @param array     唯一索引字段
     * @throws IOException 有重复数据时抛出异常
     */
    public void check(TableFileSortContext cxt, TextTableFile tableFile, File sortfile, OrderByExpression... array) throws IOException {
        String[] indexs = new String[array.length];
        Arrays.fill(indexs, "");

        TextTableFile clone = tableFile.clone();
        clone.setAbsolutePath(sortfile.getAbsolutePath());
        TextTableFileReader in = clone.getReader(cxt.getReaderBuffer());
        try {
            String ls = "";
            TextTableLine line;
            while ((line = in.readLine()) != null) {
                boolean equals = true;
                for (int i = 0; i < array.length; i++) {
                    OrderByExpression field = array[i];
                    String value = line.getColumn(field.getPosition());
                    if (!indexs[i].equals(value)) {
                        equals = false;
                    }
                }

                if (equals) {
                    StringBuilder buf = new StringBuilder(100);
                    buf.append("数据文件剥离增量失败!\n");
                    buf.append("数据文件: ").append(sortfile.getAbsolutePath()).append('\n');
                    buf.append("唯一索引: ");
                    for (int i = 0; i < array.length; i++) {
                        OrderByExpression field = array[i];
                        buf.append("第").append(field.getPosition()).append("个字段");//.append(':').append(indexs[i]);
                        if (i < array.length - 1) {
                            buf.append(", ");
                        }
                    }
                    buf.append('\n');
//                    buf.append("重复数据: \n").append(ls).append('\n').append(line.getContent());
                    throw new IOException(buf.toString());
                } else {
                    ls = line.getContent();
                    for (int i = 0; i < array.length; i++) {
                        indexs[i] = line.getColumn(array[i].getPosition());
                    }
                }
            }
        } finally {
            in.close();
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

}
