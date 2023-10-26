package icu.etl.ioc;

import java.lang.reflect.Constructor;
import java.util.List;

import icu.etl.util.ClassUtils;
import icu.etl.util.ResourcesUtils;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class BeanConstructor {

    private EasyetlContext context;

    public BeanConstructor(EasyetlContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public <E> E newInstance(Class<?> type, Object... args) {
        return this.newInstance(type, new BeanArgument("", args));
    }

    /**
     * 使用 Class 生成一个实例对象
     *
     * @param type     类信息
     * @param argument 参数
     * @param <E>      类信息
     * @return 实例对象
     */
    public <E> E newInstance(Class<?> type, BeanArgument argument) {
        E obj = this.create(type, argument);
        if (obj instanceof EasyetlContextAware) { // 自动注入容器上下文信息 TODO 改成反射注入
            ((EasyetlContextAware) obj).setContext(this.context);
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    protected <E> E create(Class<?> type, BeanArgument argument) {
        BeanConstructorParser parser = new BeanConstructorParser(type, argument);

        // 优先使用参数匹配的构造方法
        if (parser.getMatchConstructor() != null) {
            System.out.println(type.getName() + " Constructor size: " + parser.getMatchConstructor().getParameterTypes().length);
            try {
                return (E) parser.getMatchConstructor().newInstance(argument.getArgs());
            } catch (Throwable e) {
                if (Ioc.out.isDebugEnabled()) {
                    Ioc.out.debug(e.getLocalizedMessage(), e);
                }
            }
        }

        // 使用无参构造方法
        if (parser.getBaseConstructor() != null) {
            System.out.println(type.getName() + " base Constructor size: " + parser.getBaseConstructor().getParameterTypes().length);
            try {
                return (E) parser.getBaseConstructor().newInstance();
            } catch (Throwable e) {
                if (Ioc.out.isDebugEnabled()) {
                    Ioc.out.debug(e.getLocalizedMessage(), e);
                }
            }
        }

        // 使用其他构造方法
        List<Constructor<?>> others = parser.getConstructors();
        for (Constructor<?> c : others) {
            Object[] parameters = this.toArgs(c.getParameterTypes(), argument.getArgs());
            System.out.println(type.getName() + " Constructor size o: " + c.getParameterTypes().length);
            try {
                return (E) c.newInstance(parameters);
            } catch (Throwable e) {
                if (Ioc.out.isDebugEnabled()) {
                    Ioc.out.debug(e.getLocalizedMessage(), e);
                }
            }
        }

        throw new UnsupportedOperationException(ResourcesUtils.getClassMessage(12, type.getName()));
    }

    /**
     * TODO 需要优化，防止循环bean，从外部参数数组中取参数值
     *
     * @param types 构造方法的参数类型
     * @param args  外部参数数组
     * @return 构造方法参数数组
     */
    protected Object[] toArgs(Class<?>[] types, Object[] args) {
        Object[] array = new Object[types.length]; // 构造方法的参数值
        for (int i = 0; i < types.length; i++) {
            if (ClassUtils.equals(EasyetlContext.class, types[i])) { // 通过构造方法注入容器上下文信息
                array[i] = this.context;
                continue;
            }

            // 基础类型不能为null，设置默认值
            String name = types[i].getName();
            if (name.equals("int")) {
                array[i] = 0;
                continue;
            }

            if (name.equals("long")) {
                array[i] = 0;
                continue;
            }

            if (name.equals("float")) {
                array[i] = 0;
                continue;
            }

            if (name.equals("double")) {
                array[i] = 0;
                continue;
            }

            if (name.equals("boolean")) {
                array[i] = false;
                continue;
            }

            if (name.equals("byte")) {
                array[i] = (byte) 0;
                continue;
            }

            if (name.equals("char")) {
                array[i] = ' ';
                continue;
            }

            if (name.equals("short")) {
                array[i] = (short) 0;
                continue;
            }

            array[i] = this.context.getBean(types[i], args);
        }
        return array;
    }

}
