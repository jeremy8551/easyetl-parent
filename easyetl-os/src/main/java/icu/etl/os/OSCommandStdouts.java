package icu.etl.os;

import java.util.List;
import java.util.Set;

/**
 * 返回命令标准输出信息映射关系
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-07-09
 */
public interface OSCommandStdouts {

    /**
     * 返回命令对应的标准输出信息
     *
     * @param commandId
     * @return
     */
    List<String> get(String commandId);

    /**
     * 返回命令编号集合
     *
     * @return
     */
    Set<String> keys();

}
