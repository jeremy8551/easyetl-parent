package icu.etl.expression;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import icu.etl.expression.operation.Operator;
import icu.etl.expression.parameter.Parameter;
import icu.etl.util.ResourcesUtils;

/**
 * 运算公式
 *
 * @author jeremy8551@qq.com
 * @createtime 2014-05-27 14:22:12
 */
public class Formula {

    private List<Parameter> datas;

    private List<Operator> operations;

    private int loop;

    /**
     * 初始化
     *
     * @param datas      参数
     * @param operations 运算操作符
     */
    public Formula(List<Parameter> datas, List<Operator> operations) {
        this.datas = datas;
        this.operations = operations;
        this.loop = 0;
    }

    /**
     * 返回公式中参数个数
     *
     * @return
     */
    public int getParameterSize() {
        return this.datas.size();
    }

    /**
     * 返回公式中的参数集合
     *
     * @return
     */
    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(this.datas);
    }

    /**
     * 返回公式中的操作符集合
     *
     * @return
     */
    public List<Operator> getOperators() {
        return Collections.unmodifiableList(this.operations);
    }

    /**
     * 执行算术运算
     *
     * @return 运算结果
     */
    public Parameter execute() {
        if (this.datas.size() == 0) {
            return null;
        }

        if (this.datas.size() == 1) {
            if (this.operations.isEmpty()) {
                return this.datas.get(0);
            } else {
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(39), this.operations.size());
            }
        }

        // 按从高到低顺序排序运算符优先级
        int[] array = new int[this.operations.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = this.operations.get(i).getPriority();
        }
        Arrays.sort(array);

        // 按运算符优先级执行运算
        int last = -1;
        for (int priority : array) {
            if (priority == last) { // 不能重复执行
                continue;
            } else {
                last = priority;
            }

            for (int index = 0; index < this.operations.size(); index++) {
                int next = index + 1;
                if (next >= this.datas.size()) {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(39), index);
                }

                // 如果等于运算符优先级，执行运算并替换原公式中的参数
                Operator operation = this.operations.get(index); // 运算符
                if (operation.getPriority() == priority) {
                    Parameter left = this.datas.get(index);
                    Parameter right = this.datas.get(next);
                    Parameter data = this.execute(left, operation, right);

                    this.datas.set(next, data); // 替换参数
                    this.datas.remove(index); // 删除无效参数
                    this.operations.remove(index);
                    index--;
                }
            }

            if (this.datas.size() == 1) {
                if (this.operations.size() == 0) {
                    return this.datas.get(0);
                } else {
                    throw new ExpressionException(ResourcesUtils.getExpressionMessage(39));
                }
            }
        }

        // 预防无限循环
        if (++this.loop >= 100) {
            throw new ExpressionException(ResourcesUtils.getExpressionMessage(2, this.toString(this.datas, this.operations)));
        } else {
            return this.execute();
        }
    }

    /**
     * 执行运算
     *
     * @param left      左侧参数
     * @param operation 运算操作符
     * @param right     右侧参数
     * @return
     */
    public Parameter execute(Parameter left, Operator operation, Parameter right) {
        if (operation == null) {
            if (left == null && right == null) {
                return null;
            } else if (left != null) {
                left.execute();
                return left;
            } else {
                right.execute();
                return right;
            }
        } else {
            if (left == null || right == null) {
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(40, left, right));
            }

            left.execute();
            right.execute();

            if (Expression.out.isDebugEnabled()) {
                Expression.out.debug(operation + "data1=" + left + ", data2=" + right);
            }

            Parameter parameter = operation.execute(left, right);
            if (parameter == null) {
                throw new ExpressionException(ResourcesUtils.getExpressionMessage(41));
            }

            if (Expression.out.isDebugEnabled()) {
                Expression.out.debug(operation + "data1=" + left + ", data2=" + right + " " + ResourcesUtils.getExpressionMessage(42, parameter));
            }

            parameter.execute();
            int type = parameter.getType();
            Object value = parameter.value();

            left.setType(type);
            left.setValue(value);

            right.setType(type);
            right.setValue(value);
            return parameter;
        }
    }

    /**
     * used by test
     *
     * @param datas      参数
     * @param operations 运算操作符
     * @return
     */
    protected String toString(List<Parameter> datas, List<Operator> operations) {
        Iterator<Parameter> it = datas.iterator();
        Iterator<Operator> it1 = operations.iterator();
        StringBuilder buf = new StringBuilder();
        while (it.hasNext()) {
            Parameter data = it.next();
            Object value = data.value();
            buf.append(value == null ? data : value);
            buf.append(" ");

            if (it1.hasNext()) {
                buf.append(it1.next().getClass().getSimpleName());
                buf.append(" ");
            }
        }

        while (it1.hasNext()) {
            buf.append(it1.next().getClass().getSimpleName());
            buf.append(" ");
        }
        return buf.toString();
    }
}
