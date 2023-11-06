package icu.etl.database.load;

import java.util.List;
import javax.sql.DataSource;

import icu.etl.database.JdbcConverterMapper;
import icu.etl.database.load.inernal.DataWriterContext;
import icu.etl.printer.Progress;
import icu.etl.util.Attribute;

/**
 * 数据加载程序的上下文信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-03-01
 */
public interface LoadEngineContext extends DataWriterContext {

    /**
     * 返回任务编号
     *
     * @return
     */
    String getId();

    /**
     * 设置读取文件时，输入流的缓冲区长度（单位字节）
     *
     * @param readBuffer
     */
    void setReadBuffer(int readBuffer);

    /**
     * 返回读取文件时输入流的缓冲区长度（单位字节）
     *
     * @return
     */
    int getReadBuffer();

    /**
     * 返回数据库连接池
     *
     * @return
     */
    DataSource getDataSource();

    /**
     * 设置数据库连接池
     *
     * @param dataSource
     */
    void setDataSource(DataSource dataSource);

    /**
     * 返回建立一致点的笔数
     *
     * @return
     */
    public long getSavecount();

    /**
     * 建立一致点的笔数
     *
     * @param savecount
     */
    public void setSavecount(long savecount);

    /**
     * 返回数据加载模式 <br>
     * replace <br>
     * insert <br>
     * merge <br>
     *
     * @return
     */
    LoadMode getLoadMode();

    /**
     * 设置数据加载模式
     *
     * @param mode replace <br>
     *             insert <br>
     *             merge <br>
     */
    void setLoadMode(LoadMode mode);

    /**
     * 设置数据源信息
     *
     * @param filepaths
     */
    void setFiles(List<String> filepaths);

    /**
     * 待装载的数据文件集合
     *
     * @return
     */
    List<String> getFiles();

    /**
     * 返回数据格式
     */
    String getFiletype();

    /**
     * 设置数据格式
     *
     * @param type
     */
    void setFiletype(String type);

    /**
     * 返回数据库表的编目信息
     *
     * @return
     */
    String getTableCatalog();

    /**
     * 设置数据库表所在编目信息
     *
     * @param catalog
     */
    void setTableCatalog(String catalog);

    /**
     * 返回数据落地的位置信息，可以是数据库表
     *
     * @return
     */
    String getTableName();

    /**
     * 设置数据落地的位置信息
     *
     * @param str
     */
    void setTableName(String str);

    /**
     * 返回表归属的模式名
     *
     * @return
     */
    String getTableSchema();

    /**
     * 设置表归属的模式名
     *
     * @param schema
     */
    void setTableSchema(String schema);

    /**
     * 设置数据库表中字段与文件中字段映射关系（可以是字段位置或字段名）
     *
     * @param colomns
     */
    void setTableColumn(List<String> colomns);

    /**
     * 设置 merge 语句关联字段
     *
     * @param columns
     */
    void setIndexColumn(List<String> columns);

    /**
     * 返回 merge 语句关联字段
     *
     * @return
     */
    List<String> getIndexColumn();

    /**
     * 返回数据库表中字段与文件中字段映射关系（可以是字段位置或字段名），在语句中的位置: tableName(1,2,3) 或 tableName(name1, name2..)
     *
     * @return
     */
    List<String> getTableColumn();

    /**
     * 返回文件中字段与数据库表中字段的映射关系（可以是字段位置）
     *
     * @return
     */
    List<String> getFileColumn();

    /**
     * 设置文件中字段与数据库表中字段的映射关系（可以是字段位置）
     *
     * @param list
     */
    void setFileColumn(List<String> list);

    /**
     * 返回数据装入失败时存储的表名（最后二列时发生错误时间和发生错误原因）
     *
     * @return
     */
    String getErrorTableName();

    /**
     * 设置数据装入失败时存储的表名（最后二列时发生错误时间和发生错误原因）
     *
     * @param tableName
     */
    void setErrorTableName(String tableName);

    /**
     * 设置数据装入失败时存储的表模式名（最后二列时发生错误时间和发生错误原因）
     *
     * @param schema
     */
    void setErrorTableSchema(String schema);

    /**
     * 返回数据装入失败时存储的表模式名（最后二列时发生错误时间和发生错误原因）
     *
     * @return
     */
    String getErrorTableSchema();

    /**
     * true 表示数据装载成功后重新生成统计信息
     *
     * @param value true 表示数据装载完毕后立刻重新生成统计信息 <br>
     *              false 表示数据装载完毕后不重新生成数据统计信息
     */
    void setStatistics(boolean value);

    /**
     * 返回 true 表示数据装载成功后执行重新生成统计信息 <br>
     * 返回 false 表示数据装载成功后不执行生成统计信息
     *
     * @return
     */
    boolean isStatistics();

    /**
     * 设置索引处理模式
     *
     * @param mode 处理模式 <br>
     *             REBUILD 装载数据完成后立刻重建索引 <br>
     *             INCREMENTAL 只针对新增数据部分重建索引 <br>
     *             AUTOSELECT 程序根据实际情况自主选择模式 <br>
     */
    void setIndexMode(IndexMode mode);

    /**
     * 返回索引处理模式
     *
     * @return 处理模式 <br>
     * REBUILD 装载数据完成后立刻重建索引 <br>
     * INCREMENTAL 只针对新增数据部分重建索引 <br>
     * AUTOSELECT 程序根据实际情况自主选择模式 <br>
     */
    IndexMode getIndexMode();

    /**
     * 返回进度输出组件，用于输出数据文件装载进度
     *
     * @return
     */
    Progress getProgress();

    /**
     * 设置进度输出组件，用于输出文件装载进度
     *
     * @param obj
     */
    void setProgress(Progress obj);

    /**
     * 返回用户自定义的映射关系
     *
     * @return
     */
    JdbcConverterMapper getConverters();

    /**
     * 保存用户自定义的映射关系
     *
     * @param converters
     */
    void setConverters(JdbcConverterMapper converters);

    /**
     * 返回 true 表示需要防止重复装载数据文件
     *
     * @return
     */
    boolean isNorepeat();

    /**
     * 设置 true 表示需要防止重复装载数据文件
     *
     * @param norepeat
     */
    void setNorepeat(boolean norepeat);

    /**
     * 返回所有属性
     *
     * @return
     */
    Attribute<String> getAttributes();

    /**
     * 设置属性集合
     *
     * @param obj
     */
    void setAttributes(Attribute<String> obj);

}
