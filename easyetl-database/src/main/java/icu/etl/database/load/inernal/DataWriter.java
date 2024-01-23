package icu.etl.database.load.inernal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import icu.etl.database.DatabaseException;
import icu.etl.database.DatabaseTable;
import icu.etl.database.JdbcDao;
import icu.etl.database.JdbcStringConverter;
import icu.etl.database.load.LoadTable;
import icu.etl.io.TableLine;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;

/**
 * 数据库输出流
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-06-10
 */
public class DataWriter {

    /** 输出流编号工厂 */
    private static AtomicInteger IDFACTORY = new AtomicInteger(0);

    /** 数据库输出流的唯一编号 */
    private int id;

    /** 数据分析器 */
    private DatabaseTable table;

    /** 成功提交的记录数 */
    private long commit;

    /** 成功越过的记录数 */
    private long skip;

    /** 因不符合规则被过滤的记录数 */
    private long reject;

    /** 因数据库报错被过滤的记录数 */
    private long delete;

    /** 未提交到数据库的记录总数 */
    private long count;

    /** 建立一致点的阀值（每次写入 saveCount 条记录后建立一致点） */
    private long saveCount;

    /** 数据库操作接口 */
    private JdbcDao dao;

    /** 数据库表中字段对应的处理类 */
    private JdbcStringConverter[] converters;

    /** 数据源中字段顺序 */
    private int[] positions;

    /** 数据库处理器 */
    private PreparedStatement statement;

    /** 插入字段个数 */
    private int column;

    /** true 表示数据库输出流正在被使用 */
    private AtomicBoolean alive;

    /**
     * 初始化
     *
     * @param dao       数据库操作接口
     * @param target    数据库表信息
     * @param saveCount 记录数
     * @throws Exception 初始化类型转换器发生错误
     */
    public DataWriter(JdbcDao dao, LoadTable target, long saveCount) throws Exception {
        super();
        Ensure.notNull(target);
        this.dao = Ensure.notNull(dao);
        this.id = IDFACTORY.addAndGet(1);
        this.alive = new AtomicBoolean(false);
        this.saveCount = Ensure.isFromOne(saveCount);
        this.table = target.getTable();
        this.statement = target.getStatement();
        this.positions = target.getFilePositions();
        this.converters = target.getConverters();
        this.column = target.getColumn();
    }

    /**
     * 打开输入流
     *
     * @throws SQLException 数据库错误
     */
    public synchronized void open() throws SQLException {
        if (this.alive.compareAndSet(false, true)) {
            this.skip = 0;
            this.commit = 0;
            this.delete = 0;
            this.reject = 0;
            this.count = 0;
            this.dao.openLoadMode(this.table.getFullName());
        } else {
            throw new DatabaseException(ResourcesUtils.getMessage("load.standard.output.msg012"));
        }
    }

    /**
     * 将文件记录中的字段持久化到数据库表中
     *
     * @param line 文件记录信息
     * @return 返回 true 表示已提交数据库事物, 返回 false 表示已提交数据但未提交事物
     * @throws Exception 提交数据发生错误
     */
    public boolean write(TableLine line) throws Exception {
        for (int i = 0; i < this.column; i++) {
            int position = this.positions[i];
            String value = line.getColumn(position);
            this.converters[i].execute(value);
        }
        this.statement.addBatch();

        // 批量提交事物
        if (++this.count >= this.saveCount) {
            this.save();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 提交数据库批量接口中缓存的数据
     *
     * @throws SQLException 数据库错误
     */
    public void commit() throws SQLException {
        if (this.count > 0) {
            this.save();
        }
    }

    /**
     * 保存数据到数据库表
     *
     * @throws SQLException 数据库错误
     */
    private void save() throws SQLException {
        this.statement.executeBatch();
        this.dao.commitLoadMode(this.table.getFullName());
        this.commit += this.count;
        this.count = 0;
    }

    /**
     * 判断数据输出流是否为有效状态
     *
     * @return 返回 true 表示数据输出流为有效状态
     */
    public boolean isAlive() {
        return this.alive.get();
    }

    /**
     * 标记数据输出流为失效状态
     *
     * @throws SQLException 数据库错误
     */
    public void close() throws SQLException {
        if (this.alive.compareAndSet(true, false)) {
            this.commit();
            this.dao.closeLoadMode(this.table.getFullName()); // 关闭数据库表的快速装载模式
        }
    }

    /**
     * 返回已提交事物的数据记录数
     *
     * @return 记录数
     */
    public long getCommitRecords() {
        return this.commit;
    }

    /**
     * 返回越过的数据记录数
     *
     * @return 记录数
     */
    public long getSkipRecords() {
        return this.skip;
    }

    /**
     * 返回因主键冲突导致未能加载的数据记录数
     *
     * @return 记录数
     */
    public long getRejectedRecords() {
        return this.reject;
    }

    /**
     * 返回因不符合设置规则导致未能加载的数据记录数
     *
     * @return 记录数
     */
    public long getDeleteRecords() {
        return this.delete;
    }

    /**
     * 返回数据库参数个数
     *
     * @return 记录数
     */
    public int getColumn() {
        return this.column;
    }

    public boolean equals(Object obj) {
        return obj instanceof DataWriter && this.id == ((DataWriter) obj).id;
    }

}
