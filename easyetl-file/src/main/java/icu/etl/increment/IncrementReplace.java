package icu.etl.increment;

import icu.etl.annotation.EasyBean;

/**
 * 对增量数据中字段进行修改
 *
 * @author jeremy8551@qq.com
 * @createtime 2010-01-19 02:45:22
 */
@EasyBean(builder = IncrementReplaceBuilder.class)
public interface IncrementReplace {

    /**
     * 返回修改字段的位置信息
     *
     * @return
     */
    int getPosition();

    /**
     * 新增数据的字段值
     *
     * @return
     */
    String getValue();

}