package icu.etl.concurrent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 接口实现类
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/12/2
 */
public class EasyJobReaderImpl implements EasyJobReader {

    private Iterator<EasyJob> it;

    /** 终止状态 */
    private volatile boolean noTerminate;

    public EasyJobReaderImpl(List<EasyJob> list) {
        this.noTerminate = true;
        this.it = list.iterator();
    }

    public EasyJobReaderImpl(EasyJobReader in) throws Exception {
        this(toList(in));
    }

    private static List<EasyJob> toList(EasyJobReader in) throws Exception {
        List<EasyJob> list = new ArrayList<EasyJob>();
        while (in.hasNext()) {
            EasyJob next = in.next();
            if (next != null) {
                list.add(next);
            }
        }
        return list;
    }

    public boolean hasNext() {
        return this.it.hasNext();
    }

    public EasyJob next() {
        return this.it.next();
    }

    public boolean isTerminate() {
        return !this.noTerminate;
    }

    public void terminate() {
        this.noTerminate = false;
    }

    public void close() {
        this.it = null;
    }

}
