package icu.etl.increment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileWriter;
import icu.etl.io.TextTableLine;

/**
 * 将增量数据保存到文件
 *
 * @author jeremy8551@qq.com
 */
public class IncrementFileWriter implements IncrementHandler {

    private ArrayList<IncrementListener> listeners;
    private TextTableFileWriter newout;
    private TextTableFileWriter updout;
    private TextTableFileWriter delout;
    private boolean outNewRecords;
    private boolean outUpdRecords;
    private boolean outDelRecords;
    private TextTableFile newfile;
    private TextTableFile oldfile;

    private boolean newfileEqualsNewout;
    private boolean newfileEqualUpdout;
    private boolean oldfileEqualDelout;

    public IncrementFileWriter(TextTableFile newfile, TextTableFile oldfile, List<IncrementListener> listeners, IncrementLogger logger, IncrementReplaceList replaces, TextTableFileWriter newout, TextTableFileWriter updout, TextTableFileWriter delout) {
        this.listeners = new ArrayList<IncrementListener>();
        if (logger != null) {
            this.listeners.add(logger);
        }
        if (replaces != null) {
            this.listeners.add(replaces);
        }
        if (listeners != null) {
            this.listeners.addAll(listeners);
        }
        this.newout = newout;
        this.updout = updout;
        this.delout = delout;
        this.outNewRecords = newout != null;
        this.outUpdRecords = updout != null;
        this.outDelRecords = delout != null;
        this.newfile = newfile;
        this.oldfile = oldfile;

        this.newfileEqualsNewout = this.newfile != null && this.newout != null && this.newfile.equalsStyle(this.newout.getTable());
        this.newfileEqualUpdout = this.newfile != null && this.updout != null && this.newfile.equalsStyle(this.updout.getTable());
        this.oldfileEqualDelout = this.oldfile != null && this.delout != null && this.oldfile.equalsStyle(this.delout.getTable());
    }

    public void handleCreateRecord(TextTableLine in) throws IOException {
        if (this.outNewRecords) {
            for (IncrementListener l : this.listeners) {
                l.beforeCreateRecord(in);
            }

            if (newfileEqualsNewout) {
                this.newout.addLine(in.getContent());
            } else {
                this.newout.addLine(in);
            }

            for (IncrementListener l : this.listeners) {
                l.afterCreateRecord(in);
            }
        }
    }

    public void handleUpdateRecord(TextTableLine newLine, TextTableLine oldLine, int position) throws IOException {
        if (this.outUpdRecords) {
            for (IncrementListener l : this.listeners) {
                l.beforeUpdateRecord(newLine, oldLine, position);
            }

            if (newfileEqualUpdout) {
                this.updout.addLine(newLine.getContent());
            } else {
                this.updout.addLine(newLine);
            }

            for (IncrementListener l : this.listeners) {
                l.afterUpdateRecord(newLine, oldLine, position);
            }
        }
    }

    public void handleDeleteRecord(TextTableLine in) throws IOException {
        if (this.outDelRecords) {
            for (IncrementListener l : this.listeners) {
                l.beforeDeleteRecord(in);
            }

            if (oldfileEqualDelout) {
                this.delout.addLine(in.getContent());
            } else {
                this.delout.addLine(in);
            }

            for (IncrementListener l : this.listeners) {
                l.afterDeleteRecord(in);
            }
        }
    }

    public void close() throws IOException {
        this.listeners.clear();
        this.listeners.trimToSize();

        if (this.newout != null) {
            this.newout.close();
            this.newout = null;
        }

        if (this.updout != null) {
            this.updout.close();
            this.updout = null;
        }

        if (this.delout != null) {
            this.delout.close();
            this.delout = null;
        }
    }

}
