package icu.etl.database.load.parallel;

/**
 * 加载数据的结果集
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-03-17
 */
public interface ResultSet {

    long getReadCount();

    long getCommitCount();

    long getDeleteCount();

    long getRejectCount();

    long getSkipCount();

    long getErrorCount();

    String toString();

    void addTotal(long read, long skip, long commit, long delete, long reject);

}
