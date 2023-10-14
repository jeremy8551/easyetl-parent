package icu.etl.script.internal;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.Format;

import icu.etl.database.JdbcBatchStatement;
import icu.etl.database.JdbcConverterMapper;
import icu.etl.database.JdbcDao;
import icu.etl.database.JdbcStringConverter;
import icu.etl.database.load.converter.AbstractConverter;

/**
 * 数据库批处理程序
 *
 * @author jeremy8551@qq.com
 */
public class ScriptStatement {

    /** 批处理程序名 */
    protected String name;

    /** 批处理器集合 */
    protected JdbcBatchStatement batch;

    /** 字段对应的批处理器 */
    protected JdbcStringConverter[] array;

    /** 格式化工具 */
    protected Format format;

    /**
     * 初始化
     *
     * @param dao    数据库操作类
     * @param format 格式化工具
     * @param batch  批量提交的记录数
     * @param name   批处理器名
     * @param sql    SQL语句
     * @throws Exception
     */
    public ScriptStatement(JdbcDao dao, Format format, int batch, String name, String sql) throws Exception {
        this.batch = dao.update(sql, batch);
        this.name = name;
        this.format = format;

        JdbcConverterMapper mapper = dao.getDialect().getStringConverters(); // 返回数据库默认的字段处理器集合
        PreparedStatement statement = this.batch.getPreparedStatement();
        ParameterMetaData meta = statement.getParameterMetaData();

        // 设置字符对应的类型转换器
        int length = meta.getParameterCount();
        this.array = new JdbcStringConverter[length];
        for (int i = 0; i < length; i++) {
            int position = i + 1;
            String typeName = meta.getParameterTypeName(position);

            JdbcStringConverter obj = mapper.get(typeName);
            obj.setAttribute(AbstractConverter.STATEMENT, statement);
            obj.setAttribute(AbstractConverter.COLUMNNAME, "");
            obj.setAttribute(AbstractConverter.COLUMNSIZE, length);
            obj.setAttribute(AbstractConverter.POSITION, position);
            obj.setAttribute(AbstractConverter.ISNOTNULL, false);
            obj.init();
            this.array[i] = obj;
        }
    }

    /**
     * 返回参数个数
     *
     * @return
     */
    public int getParameterCount() {
        return this.array.length;
    }

    /**
     * 设置字段值
     *
     * @param index 参数位置，从 0 开始
     * @param value 参数值
     * @throws Exception
     */
    public void setParameter(int index, Object value) throws Exception {
        this.array[index].execute(this.format.format(value));
    }

    /**
     * 提交缓存
     *
     * @throws SQLException
     */
    public void executeBatch() throws SQLException {
        this.batch.addBatch();
        this.batch.executeBatch();
    }

    /**
     * 关闭批处理程序
     *
     * @throws SQLException
     */
    public void close() throws SQLException {
        this.name = null;
        this.array = null;
        this.batch.close();
    }

}
