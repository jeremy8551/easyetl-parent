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
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * 任务的消息文件
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-11-12
 */
public class EasyJobMessage {

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
     * @param charsetName 消息文件字符集
     * @throws IOException 解析消息文件发生错误
     */
    public EasyJobMessage(File file, String charsetName) throws IOException {
        this.charsetName = Ensure.notBlank(charsetName);
        this.attributes = new LinkedHashMap<String, String>();
        this.status = -1;

        if (FileUtils.isFile(file)) {
            this.file = file;
            this.parse(file, charsetName);
        }
    }

    /**
     * 读取消息文件
     *
     * @param file        消息文件
     * @param charsetName 消息文件字符集
     * @throws IOException 访问文件错误
     */
    protected void parse(File file, String charsetName) throws IOException {
        BufferedReader in = IO.getBufferedReader(file, charsetName);
        try {
            this.attributes.clear();
            for (String line; (line = in.readLine()) != null; ) {
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
        } finally {
            in.close();
        }
    }

    /**
     * 将缓存内容写入消息文件
     *
     * @throws IOException 访问文件错误
     */
    public synchronized void store() throws IOException {
        if (this.file == null) {
            return;
        }

        FileOutputStream out = new FileOutputStream(this.file, false);
        try {
            String str = this.toString();
            out.write(str.getBytes(this.charsetName));
            out.flush();
        } finally {
            out.close();
        }
    }

    /**
     * 返回消息文件
     *
     * @return 消息文件
     */
    public File getMessagefile() {
        return this.file;
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
     * @return 属性值
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
     * @return 启动时间
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
     * @return 结束时间
     */
    public String getFinish() {
        return this.attributes.get("finish");
    }

    /**
     * 判断任务是否正在运行
     *
     * @return 返回 true 表示任务正在运行
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
     * 判断任务是否被终止
     *
     * @return 返回 true 表示运行中的任务已被终止
     */
    public boolean isTerminate() {
        return this.status == 2;
    }

    public String toString() {
        CharTable ct = new CharTable();
        ct.addTitle("", CharTable.ALIGN_RIGHT);
        ct.addTitle("", CharTable.ALIGN_LEFT);
        ct.addTitle("", CharTable.ALIGN_LEFT);

        Set<String> keys = this.attributes.keySet();
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
        return ct.toString(CharTable.Style.simple);
    }

}
