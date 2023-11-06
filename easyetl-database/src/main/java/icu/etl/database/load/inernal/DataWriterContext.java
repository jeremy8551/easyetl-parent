package icu.etl.database.load.inernal;

import java.util.List;

import icu.etl.database.JdbcConverterMapper;

public interface DataWriterContext {

    /**
     * 返回字段名集合 tableName(1,2,3) 或 tableName(name1, name2..)
     *
     * @return
     */
    List<String> getTableColumn();

    /**
     * 返回数据字段顺序
     *
     * @return
     */
    List<String> getFileColumn();

    /**
     * 返回用户自定义的映射关系
     *
     * @return
     */
    JdbcConverterMapper getConverters();

}
