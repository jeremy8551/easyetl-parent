package icu.etl.expression.operation;

import icu.etl.expression.parameter.Parameter;

/**
 * 运算操作
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-15 15:33:14
 */
public interface Operator {

    /**
     * 运算优先级 <br>
     * 优先级按照从高到低的顺序书写，也就是优先级为1的优先级最高，优先级14的优先级最低。 <br>
     *
     * @return 1到14
     */
    int getPriority();

    /**
     * 运算操作
     *
     * @param d1 操作数1
     * @param d2 操作数2
     * @return
     */
    Parameter execute(Parameter d1, Parameter d2);

}