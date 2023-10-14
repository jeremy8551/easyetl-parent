package icu.etl.increment;

import icu.etl.io.TextTableLine;

/**
 * 发生增量数据的监听器 <br>
 * <br>
 * 监听运行规则如下: <br>
 * 在剥离出新增数据之前（即执行 {@linkplain IncrementHandler#handleCreateRecord(TextTableLine)} 方法之前）调用 {@linkplain #beforeCreateRecord(TextTableLine)} 方法 <br>
 * 在剥离出发生变更数据之前（即执行 {@linkplain IncrementHandler#handleUpdateRecord(TextTableLine, TextTableLine, int)} 方法之前）调用 {@linkplain #beforeUpdateRecord(TextTableLine, TextTableLine, int)} 方法 <br>
 * 在剥离出已删除数据之前（即执行 {@linkplain IncrementHandler#handleDeleteRecord(TextTableLine)} 方法之前）调用 {@linkplain #beforeDeleteRecord(TextTableLine)} 方法 <br>
 * <br>
 * 在剥离出新增数据之后（即执行 {@linkplain IncrementHandler#handleCreateRecord(TextTableLine)} 方法之后）调用 {@linkplain #afterCreateRecord(TextTableLine)} 方法 <br>
 * 在剥离出发生变更数据之后（即执行 {@linkplain IncrementHandler#handleUpdateRecord(TextTableLine, TextTableLine, int)} 方法之后）调用 {@linkplain #afterUpdateRecord(TextTableLine, TextTableLine, int)} 方法 <br>
 * 在剥离出已删除数据之后（即执行 {@linkplain IncrementHandler#handleDeleteRecord(TextTableLine)} 方法之后）调用 {@linkplain #afterDeleteRecord(TextTableLine)} 方法 <br>
 *
 * @author jeremy8551@qq.com
 */
public interface IncrementListener {

    /**
     * 在处理新增行记录前执行的接口 <br>
     * 新增行记录：在表格文件中存在，但在旧表格文件中不存在的行信息
     *
     * @param line 在新表格文件中发现的新增行记录
     */
    void beforeCreateRecord(TextTableLine line);

    /**
     * 在处理新增行记录后执行的接口 <br>
     * 新增行记录：在表格文件中存在，但在旧表格文件中不存在的行信息
     *
     * @param line 在新表格文件中发现的新增行记录
     */
    void afterCreateRecord(TextTableLine line);

    /**
     * 在处理变更行记录前执行的接口 <br>
     * 变更行记录： 新表格文件与旧表格文件中索引字段相同，但非索引字段不同的行信息
     *
     * @param newLine  新表格文件中的行信息
     * @param oldLine  旧表格文件中的行信息
     * @param position 首个不同值的非索引字段位置信息，从 1 开始
     */
    void beforeUpdateRecord(TextTableLine newLine, TextTableLine oldLine, int position);

    /**
     * 在处理变更行记录后执行的接口 <br>
     * 变更行记录： 新表格文件与旧表格文件中索引字段相同，但非索引字段不同的行信息
     *
     * @param newLine  新表格文件中的行信息
     * @param oldLine  旧表格文件中的行信息
     * @param position 首个不同值的非索引字段位置信息，从 1 开始
     */
    void afterUpdateRecord(TextTableLine newLine, TextTableLine oldLine, int position);

    /**
     * 旧表格文件中行信息被删除时的监听接口 <br>
     * 已删除记录：在旧表格文件中存在，但在新表格文件中不存在的行信息
     *
     * @param line 从旧表格文件中被删除的行信息
     */
    void beforeDeleteRecord(TextTableLine line);

    /**
     * 旧表格文件中行信息被删除时的监听接口 <br>
     * 已删除记录：在旧表格文件中存在，但在新表格文件中不存在的行信息
     *
     * @param line 从旧表格文件中被删除的行信息
     */
    void afterDeleteRecord(TextTableLine line);

}
