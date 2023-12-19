package icu.etl.increment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileWriter;
import icu.etl.io.TextTableLine;

/**
 * 接口实现类
 *
 * @author jeremy8551@qq.com
 */
public class IncrementHandlerImpl implements IncrementHandler {

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

    public IncrementHandlerImpl(TextTableFile newfile, TextTableFile oldfile, List<IncrementListener> listeners, IncrementListenerImpl logger, IncrementReplaceListener replaces, TextTableFileWriter newout, TextTableFileWriter updout, TextTableFileWriter delout) {
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

    public void handleCreateRecord(TextTableLine line) throws IOException {
        if (this.outNewRecords) {
            TextTableLineImpl record = new TextTableLineImpl(this.newfile, line);
            for (IncrementListener l : this.listeners) {
                l.beforeCreateRecord(record);
            }

            if (this.newfileEqualsNewout) {
                this.newout.addLine(record.getContent());
            } else {
                this.newout.addLine(record);
            }

            for (IncrementListener l : this.listeners) {
                l.afterCreateRecord(record);
            }
        }
    }

    public void handleUpdateRecord(TextTableLine newLine, TextTableLine oldLine, int position) throws IOException {
        if (this.outUpdRecords) {
            TextTableLineImpl newRecord = new TextTableLineImpl(this.newfile, newLine);
            TextTableLineImpl oldRecord = new TextTableLineImpl(this.oldfile, oldLine);
            for (IncrementListener l : this.listeners) {
                l.beforeUpdateRecord(newRecord, oldRecord, position);
            }

            if (this.newfileEqualUpdout) {
                this.updout.addLine(newRecord.getContent());
            } else {
                this.updout.addLine(newRecord);
            }

            for (IncrementListener l : this.listeners) {
                l.afterUpdateRecord(newRecord, oldRecord, position);
            }
        }
    }

    public void handleDeleteRecord(TextTableLine line) throws IOException {
        if (this.outDelRecords) {
            TextTableLineImpl record = new TextTableLineImpl(this.oldfile, line);
            for (IncrementListener l : this.listeners) {
                l.beforeDeleteRecord(record);
            }

            if (this.oldfileEqualDelout) {
                this.delout.addLine(record.getContent());
            } else {
                this.delout.addLine(record);
            }

            for (IncrementListener l : this.listeners) {
                l.afterDeleteRecord(record);
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
