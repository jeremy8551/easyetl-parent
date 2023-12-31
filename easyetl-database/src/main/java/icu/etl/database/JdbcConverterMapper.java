package icu.etl.database;

/**
 * JDBC 字段与实现类的映射关系
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-03-26
 */
public interface JdbcConverterMapper {

    /**
     * 返回 true 表示已设置字段的类型转换器
     *
     * @param key 字段名，字段类型或字段位置信息
     * @return
     */
    public boolean contains(String key);

    /**
     * 返回字段类型对应的数据类型转换器实例
     *
     * @param <E>
     * @param key 字段名，字段类型或字段位置信息
     * @return 返回 {@linkplain JdbcObjectConverter} 类 或 {@linkplain JdbcStringConverter} 类的实例
     */
    public <E> E get(String key);

}
