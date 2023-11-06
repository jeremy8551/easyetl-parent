package icu.etl.expression.parameter;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 数组类型参数
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-01-28
 */
public class ArrayParameter extends ExpressionParameter {

    /**
     * 初始化
     */
    public ArrayParameter() {
        this.type = Parameter.ARRAY;
        this.value = new ArrayList<Parameter>();
    }

    /**
     * 向数组中添加元素
     *
     * @param parameter
     */
    @SuppressWarnings("unchecked")
    public void add(Parameter parameter) {
        ArrayList<Parameter> list = (ArrayList<Parameter>) this.value;
        list.add(parameter);
    }

    /**
     * 判断参数是否在数组中
     *
     * @param parameter
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean exists(Parameter parameter) {
        ArrayList<Parameter> list = (ArrayList<Parameter>) this.value;
        return list.contains(parameter);
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @SuppressWarnings("unchecked")
    public String toString() {
        StringBuilder cb = new StringBuilder("[");
        ArrayList<Parameter> list = (ArrayList<Parameter>) this.value;
        for (Iterator<Parameter> it = list.iterator(); it.hasNext(); ) {
            Parameter parameter = it.next();
            cb.append(parameter.value());
            if (it.hasNext()) {
                cb.append(", ");
            }
        }
        cb.append("]");
        return cb.toString();
    }

    public Parameter copy() {
        ArrayParameter obj = new ArrayParameter();
        obj.type = this.type;
        obj.value = this.value;
        return obj;
    }

}
