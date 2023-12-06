package icu.etl.ioc.impl;

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

    /** 所有构造方法 */
    private List<Constructor<?>> list;

    /** 外部传入的构造方法的参数 */
    private BeanArgument argument;

    /** 有参构造方法 */
    private Constructor<?> argsConstrucetor;

    /** 无参构造方法 */
    private Constructor<?> baseConstructor;

    /**
     * 初始化
     *
     * @param type     类信息
     * @param argument 外部传入的构造方法参数
     */
    public BeanConstructor(Class<?> type, BeanArgument argument) {
        Constructor<?>[] array = type.getConstructors();
        this.list = new ArrayList<Constructor<?>>(array.length);
        this.argument = argument;
        this.parse(array);
    }

    /**
     * 分析构造方法
     *
     * @param array 构造方法数组
     */
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

    /**
     * 判断构造方法参数类型与外部输入参数是否匹配
     *
     * @param types 构造方法的参数类型数组
     * @return 返回true表示匹配, false表示不匹配
     */
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

    /**
     * 返回有参构造方法
     *
     * @return 构造方法
     */
    public Constructor<?> getMatchConstructor() {
        return argsConstrucetor;
    }

    /**
     * 返回无参构造方法
     *
     * @return 构造方法
     */
    public Constructor<?> getBaseConstructor() {
        return baseConstructor;
    }

    /**
     * 返回所有构造方法
     *
     * @return 构造方法
     */
    public List<Constructor<?>> getConstructors() {
        return list;
    }

}
