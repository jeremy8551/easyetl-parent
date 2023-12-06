package icu.etl.ioc.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import icu.etl.ioc.EasyBeanFactory;
import icu.etl.ioc.EasyContext;
import icu.etl.ioc.EasyContextAware;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ArrayUtils;
import icu.etl.util.ClassUtils;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;

/**
 * 接口实现类
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class EasyBeanFactoryImpl implements EasyBeanFactory {
    private final static Log log = LogFactory.getLog(EasyBeanFactoryImpl.class);

    private EasyContext context;

    public EasyBeanFactoryImpl(EasyContext context) {
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
        BeanConstructor constructors = new BeanConstructor(type, argument);
        StringBuilder buf = new StringBuilder();

        // 优先使用参数匹配的构造方法
        if (constructors.getMatchConstructor() != null) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getIocMessage(1, type.getName(), constructors.getMatchConstructor().toGenericString()));
            }

            try {
                return (E) constructors.getMatchConstructor().newInstance(argument.getArgs());
            } catch (Throwable e) {
                String message = ResourcesUtils.getIocMessage(2, type.getName(), constructors.getMatchConstructor().toGenericString(), BeanArgument.toString(argument.getArgs()));
                buf.append(FileUtils.lineSeparator).append(message);

                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
            }
        }

        // 使用无参构造方法
        if (constructors.getBaseConstructor() != null) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getIocMessage(1, type.getName(), constructors.getBaseConstructor().toGenericString()));
            }

            try {
                return (E) constructors.getBaseConstructor().newInstance();
            } catch (Throwable e) {
                String message = ResourcesUtils.getIocMessage(2, type.getName(), constructors.getBaseConstructor().toGenericString(), "");
                buf.append(FileUtils.lineSeparator).append(message);

                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
            }
        }

        // 使用其他构造方法
        List<Constructor<?>> others = constructors.getConstructors();
        for (Constructor<?> c : others) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getIocMessage(1, type.getName(), c.toGenericString()));
            }

            Object[] parameters = this.toArgs(c.getParameterTypes(), argument.getArgs());
            try {
                return (E) c.newInstance(parameters);
            } catch (Throwable e) {
                String message = ResourcesUtils.getIocMessage(2, type.getName(), c.toGenericString(), BeanArgument.toString(parameters));
                buf.append(FileUtils.lineSeparator).append(message);

                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
            }
        }

        throw new UnsupportedOperationException(ResourcesUtils.getIocMessage(3, type.getName(), buf));
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

            Object param = ArrayUtils.indexOf(args, types[i], 0);
            if (param != null) {
                array[i] = param;
                int index = ArrayUtils.indexOf(array, 0, param);
                Ensure.isFromZero(index);
                args[index] = null; // 参数被使用一次后, 不能再次使用
                continue;
            }

            array[i] = this.context.getBean(types[i], args);
        }
        return array;
    }

}
