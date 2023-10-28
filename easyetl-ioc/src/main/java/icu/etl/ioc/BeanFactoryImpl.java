package icu.etl.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import icu.etl.util.ClassUtils;
import icu.etl.util.ResourcesUtils;

/**
 * 组件工厂的实现类
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class BeanFactoryImpl implements BeanFactory {

    private EasyContext context;

    public BeanFactoryImpl(EasyContext context) {
        this.context = context;
    }

    public <E> E createBean(Class<?> type, Object... args) {
        if (Modifier.isAbstract(type.getModifiers())) { // 不能是接口或抽象类
            throw new UnsupportedOperationException(ResourcesUtils.getIocMessage(4, type.getName()));
        }

        BeanArgument argument = new BeanArgument("", args);
        E obj = this.create(type, argument);

        // 自动注入容器上下文信息 TODO 改成反射注入
        if (obj instanceof EasyContextAware) {
            ((EasyContextAware) obj).setContext(this.context);
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    protected <E> E create(Class<?> type, BeanArgument argument) {
        BeanConstructor set = new BeanConstructor(type, argument);

        // 优先使用参数匹配的构造方法
        if (set.getMatchConstructor() != null) {
            if (Ioc.out.isDebugEnabled()) {
                Ioc.out.debug(ResourcesUtils.getIocMessage(1, type.getName(), set.getMatchConstructor().toGenericString()));
            }

            try {
                return (E) set.getMatchConstructor().newInstance(argument.getArgs());
            } catch (Throwable e) {
                if (Ioc.out.isDebugEnabled()) {
                    Ioc.out.debug(ResourcesUtils.getIocMessage(2, type.getName(), set.getMatchConstructor().toGenericString()), e);
                }
            }
        }

        // 使用无参构造方法
        if (set.getBaseConstructor() != null) {
            if (Ioc.out.isDebugEnabled()) {
                Ioc.out.debug(ResourcesUtils.getIocMessage(1, type.getName(), set.getBaseConstructor().toGenericString()));
            }

            try {
                return (E) set.getBaseConstructor().newInstance();
            } catch (Throwable e) {
                if (Ioc.out.isDebugEnabled()) {
                    Ioc.out.debug(ResourcesUtils.getIocMessage(2, type.getName(), set.getBaseConstructor().toGenericString()), e);
                }
            }
        }

        // 使用其他构造方法
        List<Constructor<?>> others = set.getConstructors();
        for (Constructor<?> c : others) {
            if (Ioc.out.isDebugEnabled()) {
                Ioc.out.debug(ResourcesUtils.getIocMessage(1, type.getName(), c.toGenericString()));
            }

            Object[] parameters = this.toArgs(c.getParameterTypes(), argument.getArgs());
            try {
                return (E) c.newInstance(parameters);
            } catch (Throwable e) {
                if (Ioc.out.isDebugEnabled()) {
                    Ioc.out.debug(ResourcesUtils.getIocMessage(2, type.getName(), c.toGenericString()), e);
                }
            }
        }

        throw new UnsupportedOperationException(ResourcesUtils.getIocMessage(3, type.getName()));
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
            if (ClassUtils.equals(EasyContext.class, types[i])) { // 通过构造方法注入容器上下文信息
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
