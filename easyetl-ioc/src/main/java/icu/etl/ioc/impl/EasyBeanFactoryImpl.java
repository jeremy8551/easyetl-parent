package icu.etl.ioc.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import icu.etl.annotation.EasyBean;
import icu.etl.ioc.EasyBeanFactory;
import icu.etl.ioc.EasyContext;
import icu.etl.ioc.EasyContextAware;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ArrayUtils;
import icu.etl.util.ClassUtils;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.ObjectUtils;
import icu.etl.util.ResourcesUtils;

/**
 * 接口实现类
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class EasyBeanFactoryImpl implements EasyBeanFactory {
    private final static Log log = LogFactory.getLog(EasyBeanFactoryImpl.class);

    /** 容器上下文信息 */
    private EasyContext context;

    public EasyBeanFactoryImpl(EasyContext context) {
        this.context = Ensure.notNull(context);
    }

    public <E> E createBean(Class<?> type, Object... args) {
        if (Modifier.isAbstract(type.getModifiers())) { // 不能是接口或抽象类
            throw new UnsupportedOperationException(ResourcesUtils.getMessage("ioc.standard.output.msg004", type.getName()));
        }

        BeanArgument argument = new BeanArgument("", args);
        E obj = this.create(type, argument);

        // 自动注入容器上下文信息
        if (obj instanceof EasyContextAware) {
            ((EasyContextAware) obj).setContext(this.context);
        }

        // 反射注入
        this.autoInjection(obj);
        return obj;
    }

    /**
     * 向实例对象中注入属性值
     *
     * @param obj 对象
     */
    public void autoInjection(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            EasyBean annotation = field.getAnnotation(EasyBean.class);
            if (annotation != null) {
                boolean isFinal = Modifier.isFinal(field.getModifiers()); // 判断属性是否被 final 修饰
                if (isFinal) {
                    throw new UnsupportedOperationException(ResourcesUtils.getMessage("ioc.standard.output.msg007", annotation.toString(), field.getName(), obj));
                }

                // 返回属性值
                Object value = ClassUtils.getField(obj, field);
                if (value == null) { // 如果属性值为null，则在容器中查找对应的对象
                    String name = ObjectUtils.coalesce(annotation.name(), "");
                    Object bean = this.context.getBean(field.getType(), name);
                    if (bean == null) {
                        throw new UnsupportedOperationException(ResourcesUtils.getMessage("ioc.standard.output.msg008", field.getType(), field.getName(), obj));
                    } else {
                        ClassUtils.setField(obj, field, bean);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <E> E create(Class<?> type, BeanArgument argument) {
        BeanConstructor constructors = new BeanConstructor(type, argument);
        StringBuilder buf = new StringBuilder();

        // 优先使用参数匹配的构造方法
        if (constructors.getMatchConstructor() != null) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("ioc.standard.output.msg001", type.getName(), constructors.getMatchConstructor().toGenericString()));
            }

            try {
                return (E) constructors.getMatchConstructor().newInstance(argument.getArgs());
            } catch (Throwable e) {
                String message = ResourcesUtils.getMessage("ioc.standard.output.msg002", type.getName(), constructors.getMatchConstructor().toGenericString(), BeanArgument.toString(argument.getArgs()));
                buf.append(FileUtils.lineSeparator).append(message);

                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
            }
        }

        // 使用无参构造方法
        if (constructors.getBaseConstructor() != null) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("ioc.standard.output.msg001", type.getName(), constructors.getBaseConstructor().toGenericString()));
            }

            try {
                return (E) constructors.getBaseConstructor().newInstance();
            } catch (Throwable e) {
                String message = ResourcesUtils.getMessage("ioc.standard.output.msg002", type.getName(), constructors.getBaseConstructor().toGenericString(), "");
                buf.append(FileUtils.lineSeparator).append(message);

                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
            }
        }

        // 使用其他构造方法
        List<Constructor<?>> others = constructors.getConstructors();
        for (Constructor<?> constructor : others) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("ioc.standard.output.msg001", type.getName(), constructor.toGenericString()));
            }

            Object[] parameters = this.toArgs(constructor.getParameterTypes(), argument.getArgs());
            try {
                return (E) constructor.newInstance(parameters);
            } catch (Throwable e) {
                String message = ResourcesUtils.getMessage("ioc.standard.output.msg002", type.getName(), constructor.toGenericString(), BeanArgument.toString(parameters));
                buf.append(FileUtils.lineSeparator).append(message);

                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
            }
        }

        throw new UnsupportedOperationException(ResourcesUtils.getMessage("ioc.standard.output.msg003", type.getName(), buf));
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
            Class<?> cls = types[i];
            if (ClassUtils.equals(EasyContext.class, cls)) { // 通过构造方法注入容器上下文信息
                array[i] = this.context;
                continue;
            }

            // 基础类型不能为null，设置默认值
            String name = cls.getName();
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

            Object value = ArrayUtils.indexOf(args, cls, 0);
            if (value != null) {
                array[i] = value;
                int index = ArrayUtils.indexOf(array, 0, value);
                Ensure.isFromZero(index);
                args[index] = null; // 参数被使用一次后, 不能再次使用
                continue;
            }

            array[i] = this.context.getBean(cls, args);
        }
        return array;
    }

}
