package icu.etl.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import icu.etl.util.ClassUtils;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class BeanConstructorSet {

    private List<Constructor<?>> list;

    private BeanArgument argument;

    private Constructor<?> argsConstrucetor;

    private Constructor<?> baseConstructor;

    public BeanConstructorSet(Class<?> type, BeanArgument argument) {
        Constructor<?>[] array = type.getConstructors();
        this.list = new ArrayList<Constructor<?>>(array.length);
        this.argument = argument;
        this.parse(array);
    }

    public void parse(Constructor<?>[] array) {
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

    public boolean match(Class<?>[] types) {
        for (int i = 0; i < types.length; i++) {
            Class<?> construnctorType = types[i];
            if (construnctorType == null) {
                continue;
            }

            Object parameter = this.argument.get(i);
            if (parameter == null) {
                continue;
            }

            Class<?> argumentType = parameter.getClass();
            if (!construnctorType.equals(argumentType) && !ClassUtils.isInterfacePresent(argumentType, construnctorType)) {
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
