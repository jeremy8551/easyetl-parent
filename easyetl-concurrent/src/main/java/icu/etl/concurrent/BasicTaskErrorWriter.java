package icu.etl.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 通用的错误信息记录器
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-04-12
 */
public class BasicTaskErrorWriter implements ExecutorErrorWriter {

    /** 错误信息序号 */
    private int index = 0;

    /** 错误信息集合 */
    private List<String> message;

    /**
     * 初始化
     */
    public BasicTaskErrorWriter() {
        this.message = new ArrayList<String>(20);
    }

    public String getErrorMessage() {
        return this.message.get(this.index++);
    }

    public boolean hasError() {
        return this.index < this.message.size();
    }

    public void addError(String id, String message, Throwable e) {
        this.message.add(message);
    }

    public void close() {
        if (this.message != null) {
            this.message.clear();
        }
        this.index = 0;
    }

    /**
     * 清空错误信息
     */
    public void clear() {
        this.close();
    }

    /**
     * 读取所有错误信息
     *
     * @return
     */
    public List<String> getErrorMessages() {
        List<String> list = new Vector<String>();
        list.addAll(this.message);
        return list;
    }

}