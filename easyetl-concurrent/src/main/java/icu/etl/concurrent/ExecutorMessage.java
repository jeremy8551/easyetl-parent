package icu.etl.concurrent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import icu.etl.util.CharTable;
import icu.etl.util.Dates;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * 任务的消息文件
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-11-12
 */
public class ExecutorMessage {

    /** -1-未启动 0-已启动 1-已完成 2-已终止 */
    private int status;

    /** 消息文件的字符集 */
    private String charsetName;

    /** 消息文件 */
    private File file;

    /** 属性集合 */
    private Map<String, String> attributes;

    /**
     * 初始化
     *
     * @param file        消息文件
     * @param charsetName 文件字符集
     * @throws IOException
     */
    public ExecutorMessage(File file, String charsetName) throws IOException {
        if (StringUtils.isBlank(charsetName)) {
            throw new IllegalArgumentException(charsetName);
        } else {
            this.attributes = new LinkedHashMap<String, String>();
            this.status = -1;
            this.load(file, charsetName);
        }
    }

    /**
     * 返回消息文件
     *
     * @return
     */
    public File getMessagefile() {
        return this.file;
    }

    /**
     * 读取消息文件
     *
     * @param file        消息文件
     * @param charsetName 消息文件字符集
     * @throws IOException
     */
    protected boolean load(File file, String charsetName) throws IOException {
        this.charsetName = charsetName;
        if (file == null) {
            return false;
        }

        this.file = file;
        if (!file.exists() || !file.isFile()) {
            return false;
        }

        BufferedReader in = IO.getBufferedReader(file, charsetName);
        try {
            this.attributes.clear();
            for (String line = null; (line = in.readLine()) != null; ) {
                String[] array = StringUtils.trimBlank(StringUtils.splitProperty(line));
                if (array != null) {
                    this.attributes.put(array[0], array[1]);
                }
            }

            if (this.attributes.containsKey("finish")) { // 上一次任务已完成时需要清空
                this.attributes.clear();
            } else {
                this.status = 2;
            }
            return true;
        } finally {
            in.close();
        }
    }

    /**
     * 将缓存内容写入消息文件
     *
     * @throws IOException
     */
    public synchronized boolean store() throws IOException {
        if (this.file == null) {
            return false;
        }

        FileOutputStream out = new FileOutputStream(this.file, false);
        try {
            String str = this.toString();
            out.write(str.getBytes(this.charsetName));
            out.flush();
            return true;
        } finally {
            out.close();
        }
    }

    public String toString() {
        Set<String> keys = this.attributes.keySet();
        CharTable ct = new CharTable();
        ct.setDelimiter("   ");
        ct.addTitle("", CharTable.ALIGN_RIGHT);
        ct.addTitle("", CharTable.ALIGN_LEFT);
        ct.addTitle("", CharTable.ALIGN_LEFT);

        for (String key : keys) {
            ct.addCell(key);
            ct.addCell("=");

            Object value = this.attributes.get(key);
            if (value != null) {
                ct.addCell(StringUtils.escapeLineSeparator(StringUtils.toString(value)));
            } else {
                ct.addCell("");
            }
        }
        return ct.toSimpleShape().ltrim().toString();
    }

    /**
     * 删除属性
     *
     * @param key 属性名
     * @return 属性值
     */
    public Object removeAttribute(String key) {
        return this.attributes.remove(key);
    }

    /**
     * 保存属性
     *
     * @param key 属性名
     * @param obj 属性值
     */
    public void setAttribute(String key, String obj) {
        this.attributes.put(key, obj);
    }

    /**
     * 查询属性值
     *
     * @param key 属性名
     * @return
     */
    public String getAttribute(String key) {
        return this.attributes.get(key);
    }

    /**
     * 设置启动时间
     */
    public void start() {
        this.setAttribute("start", Dates.currentTimeStamp());
        this.status = 0;
    }

    /**
     * 返回启动时间
     *
     * @return
     */
    public String getStart() {
        return this.getAttribute("start");
    }

    /**
     * 保存结束时间
     */
    public void finish() {
        this.setAttribute("finish", Dates.currentTimeStamp());
        this.status = 1;
    }

    /**
     * 返回结束时间
     *
     * @return
     */
    public String getFinish() {
        return this.attributes.get("finish");
    }

    /**
     * 返回 true 表示任务正在运行
     *
     * @return
     */
    public boolean isRunning() {
        return this.status == 0;
    }

    /**
     * 终止运行中的任务
     */
    public void terminate() {
        this.status = 2;
    }

    /**
     * 返回 true 表示运行中的任务已被终止
     *
     * @return
     */
    public boolean isTerminate() {
        return this.status == 2;
    }

}
