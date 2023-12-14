package icu.etl.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import icu.etl.collection.Throwables;

/**
 * 错误信息输出流
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-04-12
 */
public class EasyJobWriterImpl implements EasyJobWriter {

    /** 错误信息集合 */
    private List<String> messages;

    /** 发生错误的并发任务名集合 */
    private List<String> taskNames;

    /** 异常集合 */
    private List<Exception> throwables;

    /**
     * 初始化
     */
    public EasyJobWriterImpl() {
        int size = 10;
        this.messages = new ArrayList<String>(size);
        this.taskNames = new ArrayList<String>(size);
        this.throwables = new ArrayList<Exception>(size);
    }

    public boolean hasError() {
        return this.messages.size() > 0 || this.taskNames.size() > 0 || this.throwables.size() > 0;
    }

    public void addError(String name, String message, Exception e) {
        this.taskNames.add(name);
        this.messages.add(message);
        this.throwables.add(e);
    }

    /**
     * 清空
     */
    public void clear() {
        this.taskNames.clear();
        this.messages.clear();
        this.throwables.clear();
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(this.messages);
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(this.taskNames);
    }

    public List<Exception> getThrowables() {
        return Collections.unmodifiableList(this.throwables);
    }

    /**
     * 转为异常信息
     *
     * @param message 错误信息
     * @return 异常信息
     */
    public Exception toException(String message) {
        Throwables es = new Throwables(message);
        for (int i = 0; i < this.taskNames.size(); i++) {
            String msg = this.messages.get(i);
            Throwable e = this.throwables.get(i);
            es.add(msg, e);
        }
        return es;
    }
}