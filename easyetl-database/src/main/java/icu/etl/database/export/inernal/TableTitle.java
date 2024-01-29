package icu.etl.database.export.inernal;

import java.sql.ResultSet;
import java.sql.SQLException;

import icu.etl.database.Jdbc;
import icu.etl.database.JdbcDao;
import icu.etl.database.JdbcQueryStatement;
import icu.etl.database.export.ExtracterContext;
import icu.etl.io.TableLine;
import icu.etl.io.TextTable;
import icu.etl.ioc.EasyContext;
import icu.etl.util.StringUtils;

/**
 * 卸载数据的标题信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-18
 */
public class TableTitle implements TableLine {

    /** 从用户自定义属性中得到的字段标题 */
    private String[] values;

    /** 标题个数 */
    private int column;

    /**
     * 初始化
     *
     * @param context 卸数引擎上下文信息
     * @param ioc     容器上下文信息
     * @throws SQLException 数据库错误
     */
    public TableTitle(ExtracterContext context, EasyContext ioc) throws SQLException {
        JdbcDao dao = new JdbcDao(ioc, context.getDataSource().getConnection());
        try {
            JdbcQueryStatement query = dao.query(context.getSource(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = query.getResultSet();
            String[] fieldNames = Jdbc.getColumnName(resultSet);
            TextTable format = context.getFormat();

            this.column = fieldNames.length;
            this.values = new String[this.column + 1];
            for (int i = 0; i < fieldNames.length; i++) {
                int position = i + 1; // 位置信息
                String name = (position <= this.column) ? format.getColumnName(position) : null; // 表格标题

                if (StringUtils.isBlank(name)) {
                    this.values[position] = fieldNames[i];
                } else {
                    this.values[position] = name;
                }
            }

            dao.rollback();
        } finally {
            dao.close();
        }
    }

    public boolean isColumnBlank(int position) {
        return StringUtils.isBlank(this.values[position]);
    }

    public String getColumn(int position) {
        return this.values[position];
    }

    public void setColumn(int position, String value) {
        this.values[position] = value;
    }

    public int getColumn() {
        return this.column;
    }

}
