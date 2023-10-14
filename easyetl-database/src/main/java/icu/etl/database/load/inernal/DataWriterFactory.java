package icu.etl.database.load.inernal;

import java.sql.SQLException;
import java.util.Vector;

import icu.etl.database.JdbcDao;
import icu.etl.database.load.LoadEngineContext;
import icu.etl.database.load.LoadTable;

public class DataWriterFactory {

    /** 数据库操作接口 */
    private JdbcDao dao;

    /** 数据库输出流集合 */
    private Vector<DataWriter> list;

    /** 数据卸载任务上下文信息 */
    private LoadEngineContext context;

    /** 目标表信息 */
    private LoadTable target;

    /**
     * 初始化
     *
     * @param dao
     * @param context
     * @param target
     */
    public DataWriterFactory(JdbcDao dao, LoadEngineContext context, LoadTable target) {
        super();
        this.list = new Vector<DataWriter>();
        this.context = context;
        this.target = target;
        this.dao = dao;
    }

    /**
     * 返回一个空闲数据库输出流
     *
     * @return
     * @throws Exception
     */
    public synchronized DataWriter create() throws Exception {
        for (DataWriter writer : this.list) {
            if (writer != null && !writer.isAlive()) {
                writer.open();
                return writer;
            }
        }

        DataWriter out = new DataWriter(this.dao, this.target, this.context.getSavecount());
        out.open();
        this.list.add(out);
        return out;
    }

    /**
     * 关闭数据库输出流
     *
     * @throws SQLException
     */
    public synchronized void close() throws SQLException {
        for (DataWriter out : this.list) {
            if (out != null) {
                out.close();
            }
        }
    }

}
