package icu.etl.increment;

import icu.etl.io.Table;
import icu.etl.io.TableLine;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableLine;
import icu.etl.printer.Printer;
import icu.etl.util.ArrayUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 记录增量数据日志输出接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-17
 */
public class IncrementLogger implements IncrementListener {

    private Printer out;
    private int[] newComparePosition;
    private int[] oldComparePosition;
    private int[] newIndexPosition;
    private int[] oldIndexPosition;
    private TextTableFile oldfile;
    private TextTableFile newfile;

    public IncrementLogger(Printer out, IncrementPosition position, TextTableFile oldfile, TextTableFile newfile) {
        super();

        if (out == null) {
            throw new NullPointerException();
        }

        this.out = out;
        this.newIndexPosition = position.getNewIndexPosition();
        this.oldIndexPosition = position.getOldIndexPosition();
        this.newComparePosition = ArrayUtils.shift(position.getNewComparePosition());
        this.oldComparePosition = ArrayUtils.shift(position.getOldComparePosition());
        this.oldfile = oldfile;
        this.newfile = newfile;
    }

    public void beforeCreateRecord(TextTableLine line) {
    }

    public void afterCreateRecord(TextTableLine line) {
        LogStr str = new LogStr();
        str.append(ResourcesUtils.getIncrementMessage(48, this.newfile.getAbsolutePath(), line.getLineNumber()));
        str.appendIndex(this.newfile, line, this.newIndexPosition);
        this.out.println(str);
    }

    public void beforeUpdateRecord(TextTableLine newLine, TextTableLine oldLine, int position) {
    }

    public void afterUpdateRecord(TextTableLine newLine, TextTableLine oldLine, int position) {
        LogStr str = new LogStr();
        str.append(ResourcesUtils.getIncrementMessage(49, this.newfile.getAbsolutePath(), newLine.getLineNumber()));
        str.appendIndex(this.newfile, newLine, this.newIndexPosition);
        str.append(", ");

        int newPosition = this.newComparePosition[position];
        int oldPosition = this.oldComparePosition[position];
        String columnName = this.newfile.getColumnName(newPosition);

        if (StringUtils.isBlank(columnName)) {
            str.append(ResourcesUtils.getIncrementMessage(52, newPosition));
        } else {
            str.append(columnName).append(' ');
        }
        str.append(ResourcesUtils.getIncrementMessage(53, oldLine.getColumn(oldPosition)));
        str.append(ResourcesUtils.getIncrementMessage(54, newLine.getColumn(newPosition)));
        this.out.println(str);
    }

    public void beforeDeleteRecord(TextTableLine in) {
    }

    public void afterDeleteRecord(TextTableLine line) {
        LogStr str = new LogStr();
        str.append(ResourcesUtils.getIncrementMessage(50, this.oldfile.getAbsolutePath(), line.getLineNumber()));
        str.appendIndex(this.oldfile, line, this.oldIndexPosition);
        this.out.println(str);
    }

    protected class LogStr {
        private StringBuilder buf;

        public LogStr() {
            this.buf = new StringBuilder(100);
        }

        public LogStr append(String str) {
            this.buf.append(str);
            return this;
        }

        public LogStr append(char c) {
            this.buf.append(c);
            return this;
        }

        /**
         * 追加关键字信息
         *
         * @param table     表格型数据
         * @param line      表格型数据的行信息
         * @param positions 关键字的位置信息
         * @return
         */
        public LogStr appendIndex(Table table, TableLine line, int[] positions) {
            for (int i = 0; i < positions.length; ) {
                int position = positions[i];
                String columnName = table.getColumnName(position);

                if (StringUtils.isBlank(columnName)) {
                    this.buf.append(ResourcesUtils.getIncrementMessage(51, position));
                } else {
                    this.buf.append(columnName).append('=');
                }

                this.buf.append("'");
                this.buf.append(StringUtils.trimBlank(line.getColumn(position)));
                this.buf.append("'");

                if (++i < positions.length) {
                    this.buf.append(", ");
                }
            }
            return this;
        }

        public String toString() {
            return this.buf.toString();
        }
    }
}
