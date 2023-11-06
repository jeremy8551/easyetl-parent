package icu.etl.increment;

import java.io.IOException;

import icu.etl.io.TextTableFileReader;
import icu.etl.util.Terminate;

/**
 * 增量剥离算法
 *
 * @author jeremy8551@qq.com
 */
public interface IncrementArith extends Terminate {

    /**
     * 剥离增量数据 <br>
     * 算法思想：<br>
     * newIdx : 比较文本的当前数据<br>
     * oldIdx : 被比较文本当前数据<br>
     * newIdx.index : 比较文本的当前数据的索引<br>
     * oldIdx.index : 被比较文本当前数据的索引<br>
     * <br>
     * <br>
     * 伪代码：<br>
     * {@literal while(newIdx <> null) { }<br>
     * if(newIdx.index == oldIdx.index) { <br>
     * if(newIdx != oldIdx) { <br>
     * &nbsp;&nbsp;newIdx 写入增量文本 (变更数据) <br>
     * } <br>
     * newIdx++; <br>
     * oldIdx++; <br>
     * continue; <br>
     * } <br>
     * {@literal if(newIdx.index < oldIdx.index) { } <br>
     * &nbsp;&nbsp;newIdx 写入增量文本 (新增数据) <br>
     * newIdx++; <br>
     * continue; <br>
     * } <br>
     * oldIdx++; <br>
     * }
     *
     * @param ic    增量剥离规则
     * @param newIn 被比较文本
     * @param oldIn 比较文本
     * @param out   输出流
     * @throws IOException
     */
    void execute(IncrementRuler ic, TextTableFileReader newIn, TextTableFileReader oldIn, IncrementHandler out) throws IOException;

}