package icu.etl.printer;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.concurrent.atomic.AtomicLong;

import icu.etl.util.Dates;
import icu.etl.util.Numbers;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;

/**
 * 百分比进度输出功能
 *
 * @author jeremy8551@qq.com
 */
public class Progress {

    /** 计时器 */
    protected TimeWatch watch;

    /** 数字：100 */
    protected Long hand;

    /** 总次数 */
    protected Long total;

    /** 当前次数 */
    protected AtomicLong count;

    /** 最后一次输出的百分比 */
    protected long last;

    /** 输出信息 */
    protected String message;

    /** 信息输出接口 */
    protected ProgressPrinter out;

    /** 输出多任务程序时，用于标示每个任务的编号 */
    protected String taskId;

    /** true 表示当前是多任务输出程序 */
    protected boolean isMulityTask;

    /** 总次数的中文说明信息 */
    protected String totalRecordsStr;

    /** true 表示显示任务编号信息 */
    protected boolean needTaskId;

    /** true 表示显示剩余时间 */
    protected boolean needLeftTime;

    /** true 表示输出进度百分比信息 */
    protected boolean needPrcess;

    /** true 表示输出总次数信息 */
    protected boolean needTotalRecords;

    /** 最后一次显示的剩余时间 */
    protected long lastLeftTime;

    /**
     * 初始化
     *
     * @param out     进度信息输出接口
     * @param message 输出进度信息: <br>
     *                ${process} 显示当前进度百分比信息 <br>
     *                ${totalRecord} 显示总次数信息 <br>
     *                ${leftTime} 用于显示剩余时间信息
     * @param total   总次数
     */
    public Progress(ProgressPrinter out, String message, long total) {
        this(out, message, total, new Format() {
            private final static long serialVersionUID = 1L;
            private DecimalFormat decimalFormat = new DecimalFormat("#,###.##"); // 设置千位分隔符

            public StringBuffer format(Object obj, StringBuffer buffer, FieldPosition position) {
                BigDecimal value = new BigDecimal((Long) obj);
                buffer.append(this.decimalFormat.format(value));
                return buffer;
            }

            public Object parseObject(String source, ParsePosition pos) {
                throw new UnsupportedOperationException(source);
            }
        });
    }

    /**
     * 初始化
     *
     * @param out     进度信息输出接口
     * @param message 输出进度信息: <br>
     *                ${process} 显示当前进度百分比信息 <br>
     *                ${totalRecord} 显示总次数信息 <br>
     *                ${leftTime} 用于显示剩余时间信息
     * @param total   总次数
     * @param format  总记录数的格式化工具
     */
    public Progress(ProgressPrinter out, String message, long total, Format format) {
        if (out == null) {
            throw new NullPointerException();
        }
        if (total < 0) {
            throw new IllegalArgumentException(String.valueOf(total));
        }

        this.out = out;
        this.total = Long.valueOf(total);
        this.hand = Long.valueOf(100);
        this.totalRecordsStr = format.format(Long.valueOf(total));
        this.last = 0;
        this.count = new AtomicLong(0);
        this.isMulityTask = false;
        this.watch = new TimeWatch();
        this.setMessage(message);
    }

    /**
     * 初始化
     *
     * @param taskId  多任务程序输出时，每个任务的唯一编号
     * @param out     进度信息输出接口
     * @param message 输出进度信息: <br>
     *                ${taskId} 显示当前任务的编号 <br>
     *                ${process} 显示当前进度百分比信息 <br>
     *                ${totalRecord} 显示总次数信息 <br>
     *                ${leftTime} 用于显示剩余时间信息
     * @param total   总次数
     */
    public Progress(String taskId, ProgressPrinter out, String message, long total) {
        this(out, message, total);
        this.taskId = taskId;
        this.isMulityTask = true;
    }

    /**
     * 设置进度输出信息
     *
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
        this.needPrcess = message != null && message.indexOf("${process}") != -1;
        this.needTotalRecords = message != null && message.indexOf("${totalRecord}") != -1;
        this.needLeftTime = message != null && message.indexOf("${leftTime}") != -1;
        this.needTaskId = message != null && message.indexOf("${taskId}") != -1;
    }

    /**
     * 返回进度输出信息
     *
     * @return
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * 输出进度信息
     */
    public void print() {
        this.print(this.count.incrementAndGet(), true);
    }

    /**
     * 输出进度信息
     *
     * @param print true表示输出进度信息 false表示不输出进度信息但是会计数
     */
    public void print(boolean print) {
        this.print(this.count.incrementAndGet(), print);
    }

    /**
     * 输出进度信息 <br>
     * <br>
     * 当前百分比数值 = 计数器 * 100 / 总次数
     *
     * @param count 计数器
     * @param print true表示输出进度信息 false表示不输出进度信息但是会计数
     */
    public void print(long count, boolean print) {
        if (this.total.compareTo(new Long(0)) == 0) {
            return;
        }

        Long denom = Numbers.multiply(count, this.hand); // 分母
        long process = Numbers.divide(denom, this.total).longValue();
        if (process > this.last && process <= 100) {
            if (this.out != null && this.message != null) {
                String str = this.message;

                if (this.needTaskId) {
                    str = StringUtils.replaceAll(str, "${taskId}", this.taskId);
                }
                if (this.needPrcess) {
                    str = StringUtils.replaceAll(str, "${process}", String.valueOf(process));
                }
                if (this.needTotalRecords) {
                    str = StringUtils.replaceAll(str, "${totalRecord}", this.totalRecordsStr);
                }
                if (this.needLeftTime) {
                    if (process < 100) { // The remaining time is displayed when it does not reach 100%
                        long useSeconds = this.watch.useSeconds(); // use seconds
                        if (useSeconds == 0) {
                            useSeconds = 1;
                        }

                        long lefttime = (useSeconds * (100 - process)) / process; // Estimate remaining time
                        if (lefttime == 0) {
                            lefttime = 1;
                        }
                        if (this.lastLeftTime > 0 && lefttime > this.lastLeftTime && (lefttime - this.lastLeftTime) <= 60) { // The error is within the last minute
                            lefttime = this.lastLeftTime;
                        }

                        str = StringUtils.replaceAll(str, "${leftTime}", ResourcesUtils.getIoxMessage(30, StringUtils.removeBlank(Dates.format((int) lefttime, false))));
                        this.lastLeftTime = lefttime;
                    } else {
                        str = StringUtils.replaceAll(str, "${leftTime}", "");
                    }
                }

                if (print) {
                    if (this.isMulityTask) {
                        this.out.println(this.taskId, str);
                    } else {
                        this.out.println(str);
                    }
                }
            }
            this.last = process;
        } else if (process < this.last) {
            // 31 上一次输出的进度比例大于当前计算的比例
        }
    }

    /**
     * 返回进度输出接口
     *
     * @return
     */
    public ProgressPrinter getPrinter() {
        return out;
    }

    /**
     * 设置当前次数
     *
     * @param count
     */
    public void setCount(long count) {
        this.count = new AtomicLong(count);
    }

    /**
     * 返回当前次数
     *
     * @return
     */
    public AtomicLong getCount() {
        return count;
    }

    /**
     * 返回任务编号（仅适用于多任务时）
     *
     * @return
     */
    public String getTaskId() {
        return this.taskId;
    }

    /**
     * 重置所有参数
     */
    public void reset() {
        this.last = 0;
        this.count = new AtomicLong(0);
        this.watch.start();
        this.lastLeftTime = 0;
    }

    public String toString() {
        return Progress.class.getSimpleName() + "[taskId=" + taskId + ", message=" + message + ", total=" + total + ", count=" + count + ", last=" + last + ", out=" + out + ", isMulityTask=" + isMulityTask + ", time=" + watch + ", totalRecordsStr=" + totalRecordsStr + ", needLeftTime=" + needLeftTime + ", needPrcess=" + needPrcess + ", needTotalRecords=" + needTotalRecords + "]";
    }

}
