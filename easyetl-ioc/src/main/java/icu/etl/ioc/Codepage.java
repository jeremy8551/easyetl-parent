package icu.etl.ioc;

import java.util.Map;

/**
 * 代码页
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-18
 */
public interface Codepage {

    /**
     * 查询代码页对应的字符集名 <br>
     * 根据字符集名查找对应的代码页 <br>
     *
     * @param key 代码页或字符集名
     * @return
     */
    String get(String key);

    /**
     * 查询代码页对应的字符集名
     *
     * @param codepage 代码页编号
     * @return
     */
    String get(int codepage);

    /**
     * 返回所有代码页与字符集的映射关系
     *
     * @return
     */
    Map<String, String> getAll();

}
