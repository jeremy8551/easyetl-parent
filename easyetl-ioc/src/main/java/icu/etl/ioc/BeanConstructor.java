package icu.etl.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 组件的构造方法
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class BeanConstructor {

    private List<Constructor<?>> list;

    private BeanArgument argument;

    private Constructor<?> argsConstrucetor;

    private Constructor<?> baseConstructor;

    public BeanConstructor(Class<?> type, BeanArgument argument) {
        Constructor<?>[] array = type.getConstructors();
        this.list = new ArrayList<Constructor<?>>(array.length);
        this.argument = argument;
        this.parse(array);
    }

    protected void parse(Constructor<?>[] array) {
        for (Constructor<?> c : array) {
            // 必须是public修饰的构造方法
            if (c.getModifiers() != Modifier.PUBLIC) {
                continue;
            }

            // 无参构造方法
            Class<?>[] types = c.getParameterTypes(); // 构造方法的参数类信息
            if (types.length == 0) {
                this.baseConstructor = c;
                continue;
            }

            // 外部参数与构造方法中的参数匹配
            if (types.length == this.argument.size()) {
                if (this.match(types)) {
                    this.argsConstrucetor = c;
                } else {
                    this.list.add(0, c); // 参数个数匹配的优先级高
                }
                continue;
            }

            this.list.add(c);
        }
    }

    protected boolean match(Class<?>[] types) {
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i]; // 方法中定义的参数类型
            if (type == null) {
                continue;
            }

            // 方法中的参数值
            Object value = this.argument.get(i);
            if (value == null) {
                continue;
            }

            if (!type.isAssignableFrom(value.getClass())) {
                return false;
            }
        }
        return true;
    }

    public Constructor<?> getMatchConstructor() {
        return argsConstrucetor;
    }

    public Constructor<?> getBaseConstructor() {
        return baseConstructor;
    }

    public List<Constructor<?>> getConstructors() {
        return list;
    }

}
