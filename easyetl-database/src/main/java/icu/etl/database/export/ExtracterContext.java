package icu.etl.database.export;

import javax.sql.DataSource;
import java.io.File;
import java.util.List;

import icu.etl.database.JdbcConverterMapper;
import icu.etl.io.TextTable;
import icu.etl.printer.Progress;

/**
 * 数据卸载的上下文信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-11-13
 */
public class ExtracterContext {

    private ExportEngine extracter;
    private List<ExtractUserListener> listener;
    private JdbcConverterMapper converters;
    private TextTable format;
    private DataSource dataSource;
    private File messagefile;
    private Progress progress;
    private String target;
    private String source;
    private String charFilter;
    private String escapes;
    private String dateformat;
    private String timeformat;
    private String timestampformat;
    private int cacheLines;
    private long maximum;
    private boolean append;
    private boolean title;
    private Object httpServletRequest;
    private Object httpServletResponse;

    /**
     * 初始化
     *
     * @param parent 卸数引擎
     */
    public ExtracterContext(ExportEngine parent) {
        this.extracter = parent;
    }

    /**
     * 返回当前上下文归属的数据卸载器
     *
     * @return 卸数引擎
     */
    public ExportEngine getExtracter() {
        return extracter;
    }

    /**
     * 设置当前上下文归属的数据卸载器
     *
     * @param extracter 卸数引擎
     */
    protected void setExtracter(ExportEngine extracter) {
        this.extracter = extracter;
    }

    /**
     * 设置监听器
     *
     * @param list 监听器集合
     */
    public void setListener(List<ExtractUserListener> list) {
        this.listener = list;
    }

    /**
     * 返回监听器
     *
     * @return 监听器集合
     */
    public List<ExtractUserListener> getListener() {
        return this.listener;
    }

    /**
     * 返回数据源信息
     *
     * @return 数据源信息
     */
    public String getSource() {
        return this.source;
    }

    /**
     * 设置数据源信息
     *
     * @param str 数据源信息
     */
    public void setSource(String str) {
        this.source = str;
    }

    /**
     * 返回数据卸载位置信息
     *
     * @return 数据卸载位置信息
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * 设置数据卸载位置信息
     *
     * @param str 数据卸载位置信息
     */
    public void setTarget(String str) {
        this.target = str;
    }

    /**
     * 设置数据卸载进度信息接口
     *
     * @param out 进度信息接口
     */
    public void setProgress(Progress out) {
        this.progress = out;
    }

    /**
     * 返回数据卸载进度信息接口
     *
     * @return 进度信息接口
     */
    public Progress getProgress() {
        return this.progress;
    }

    /**
     * 返回数据格式
     *
     * @return 数据格式
     */
    public TextTable getFormat() {
        return this.format;
    }

    /**
     * 设置数据格式
     *
     * @param format 数据格式
     */
    public void setFormat(TextTable format) {
        this.format = format;
    }

    /**
     * 返回非法字符，保存字符串时会过滤调非法字符
     *
     * @return 非法字符集合
     */
    public String getCharFilter() {
        return charFilter;
    }

    /**
     * 设置非法字符，保存字符串时会过滤调非法字符
     *
     * @param str 非法字符集合
     */
    public void setCharFilter(String str) {
        this.charFilter = str;
    }

    /**
     * 返回转义字符，保存字符串时会对所有转义字符进行转义
     *
     * @return 转义字符
     */
    public String getEscapes() {
        return escapes;
    }

    /**
     * 设置转义字符，保存字符串时会对所有转义字符进行转义
     *
     * @param escapes 转义字符
     */
    public void setEscapes(String escapes) {
        this.escapes = escapes;
    }

    /**
     * 返回日期格式
     *
     * @return 日期格式
     */
    public String getDateformat() {
        return dateformat;
    }

    /**
     * 设置日期格式
     *
     * @param dateformat 日期格式
     */
    public void setDateformat(String dateformat) {
        this.dateformat = dateformat;
    }

    /**
     * 返回时间格式
     *
     * @return 时间格式
     */
    public String getTimeformat() {
        return timeformat;
    }

    /**
     * 返回时间格式
     *
     * @param timeformat 时间格式
     */
    public void setTimeformat(String timeformat) {
        this.timeformat = timeformat;
    }

    /**
     * 返回时间撮格式
     *
     * @return 时间撮格式
     */
    public String getTimestampformat() {
        return timestampformat;
    }

    /**
     * 返回时间撮格式
     *
     * @param str 时间撮格式
     */
    public void setTimestampformat(String str) {
        this.timestampformat = str;
    }

    /**
     * 返回输出流缓存区行数
     *
     * @return 输出流缓存区行数
     */
    public int getCacheLines() {
        return cacheLines;
    }

    /**
     * 设置输出流缓存区行数
     *
     * @param n 输出流缓存区行数
     */
    public void setCacheLines(int n) {
        this.cacheLines = n;
    }

    /**
     * 返回卸载文件的最大记录数，超过最大值时会新建文件
     *
     * @return 返回0表示无最大值
     */
    public long getMaximum() {
        return maximum;
    }

    /**
     * 设置卸载文件的最大记录数，超过最大值时会新建文件
     *
     * @param n 设置 0 表示无最大值
     */
    public void setMaximum(long n) {
        this.maximum = n;
    }

    /**
     * 设置消息信息存储的文件
     *
     * @param message 消息文件
     */
    public void setMessagefile(File message) {
        this.messagefile = message;
    }

    /**
     * 返回消息信息存储的文件
     *
     * @return 消息信息存储的文件
     */
    public File getMessagefile() {
        return this.messagefile;
    }

    /**
     * 判断数据写入方式
     *
     * @return 返回 true 表示追加方式写入数据，false 表示覆盖原有数据
     */
    public boolean isAppend() {
        return append;
    }

    /**
     * 设置数据写入方式
     *
     * @param b 设置 true 表示追加方式写入数据，false 表示覆盖原有数据
     */
    public void setAppend(boolean b) {
        this.append = b;
    }

    /**
     * 判断是否要写入列名
     *
     * @return 返回 true 表示将列名写入文件
     */
    public boolean isTitle() {
        return this.title;
    }

    /**
     * 设置是否要写入列名
     *
     * @param b true 表示将列名写入文件
     */
    public void setTitle(boolean b) {
        this.title = b;
    }

    /**
     * 返回数据库连接池
     *
     * @return 数据库连接池
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 保存数据库连接池
     *
     * @param dataSource 数据库连接池
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 返回字段转换器映射关系
     *
     * @return 类型转换器映射
     */
    public JdbcConverterMapper getConverters() {
        return converters;
    }

    /**
     * 设置字段转换器映射关系
     *
     * @param converts 类型转换器映射
     */
    public void setConverters(JdbcConverterMapper converts) {
        this.converters = converts;
    }

    /**
     * 用于 http 方式卸载数据
     *
     * @return 返回 http 请求
     */
    public Object getHttpServletRequest() {
        return httpServletRequest;
    }

    /**
     * 用于 http 方式卸载数据
     *
     * @param request 设置 http 请求
     */
    public void setHttpServletRequest(Object request) {
        this.httpServletRequest = request;
    }

    /**
     * 用于 http 方式卸载数据
     *
     * @return 返回 http 响应
     */
    public Object getHttpServletResponse() {
        return httpServletResponse;
    }

    /**
     * 用于 http 方式卸载数据
     *
     * @param response 设置 http 响应
     */
    public void setHttpServletResponse(Object response) {
        this.httpServletResponse = response;
    }

}
