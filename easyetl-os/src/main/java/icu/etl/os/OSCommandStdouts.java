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
     * @param commandId 命令
     * @return 标准输出信息集合
     */
    List<String> get(String commandId);

    /**
     * 返回命令编号集合
     *
     * @return 编号集合
     */
    Set<String> keys();

}
