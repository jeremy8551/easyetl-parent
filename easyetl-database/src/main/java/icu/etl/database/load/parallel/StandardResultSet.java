package icu.etl.database.load.parallel;

import icu.etl.util.CharTable;

public class StandardResultSet implements ResultSet {

    private long total;
    private long commit;
    private long delete;
    private long reject;
    private long skip;
    private long error;

    public StandardResultSet() {
    }

    public long getErrorCount() {
        return error;
    }

    public void setErrorCount(long error) {
        this.error = error;
    }

    public long getReadCount() {
        return total;
    }

    public void setReadRecords(long value) {
        this.total = value;
    }

    public long getCommitCount() {
        return commit;
    }

    public void setCommitRecords(long value) {
        this.commit = value;
    }

    public long getDeleteCount() {
        return this.delete;
    }

    public void setDeleteRecords(long value) {
        this.delete = value;
    }

    public long getRejectCount() {
        return reject;
    }

    public void setRejectRecords(long value) {
        this.reject = value;
    }

    public long getSkipCount() {
        return skip;
    }

    public void setSkipRecords(long value) {
        this.skip = value;
    }

    public synchronized void addTotal(long read, long skip, long commit, long delete, long reject) {
        this.total += read;
        this.skip += skip;
        this.commit += commit;
        this.delete += delete;
        this.reject += reject;
    }

    public String toString() {
        CharTable table = new CharTable();
        table.setDelimiter("");
        table.addTitle("");
        table.addTitle("");
        table.addTitle("");

        table.addCell("Number of rows read");
        table.addCell("    = ");
        table.addCell(String.valueOf(this.total));

        table.addCell("Number of rows skipped");
        table.addCell("    = ");
        table.addCell(String.valueOf(this.skip));

        table.addCell("Number of rows rejected");
        table.addCell("    = ");
        table.addCell(String.valueOf(this.reject));

        table.addCell("Number of rows deleted");
        table.addCell("    = ");
        table.addCell(String.valueOf(this.delete));

        table.addCell("Number of rows committed");
        table.addCell("    = ");
        table.addCell(String.valueOf(this.commit));
        return table.toSimpleShape().ltrim().toString();
    }

}
