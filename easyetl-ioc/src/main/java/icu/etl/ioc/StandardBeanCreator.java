package icu.etl.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import icu.etl.util.ResourcesUtils;

public class StandardBeanCreator implements BeanCreator {

    /** 上下文信息 */
    private EasyetlContext context;

    /**
     * 初始化
     *
     * @param context 上下文信息
     */
    public StandardBeanCreator(EasyetlContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public <E> E getBean(Class<E> cls, Object... args) {
        List<E> beanList = this.context.getAttribute(cls); // 优先从容器中取对象
        if (beanList.size() == 1) {
            return beanList.get(0);
        }

        // 优先使用接口工厂生成实例对象
        BeanBuilder<?> factory = this.context.getBeanBuilder(cls);
        if (factory != null) {
            try {
                return (E) factory.build(this.context, args);
            } catch (Throwable e) {
                throw new RuntimeException(ResourcesUtils.getClassMessage(12, cls.getName()), e);
            }
        }

        // 查询接口的实现类
        Class<E> beanClass = this.context.getBeanClass(cls, args);
        if (beanClass != null) {
            return this.newInstance(beanClass, args);
        }

        // 默认从容器中取最后一个对象
        if (beanList.size() >= 2) {
            return beanList.get(beanList.size() - 1);
        }
        return null;
    }

    /**
     * 使用 Class 生成一个实例对象
     *
     * @param cls  类信息
     * @param args 参数
     * @param <E>  类信息
     * @return 实例对象
     */
    public <E> E newInstance(Class<E> cls, Object... args) {
        try {
            return cls.newInstance();
        } catch (Throwable e) {
            E obj = this.createByConstructor(cls, args);
            if (obj == null) {
                throw new IllegalArgumentException(ResourcesUtils.getClassMessage(12, cls.getName()), e);
            } else {
                return obj;
            }
        }
    }

    /**
     * 向构造方法中注入参数，并将类实例化
     *
     * @param <E>  类
     * @param cls  类信息
     * @param args 参数
     * @return 类的实例对象
     */
    @SuppressWarnings("unchecked")
    public <E> E createByConstructor(Class<E> cls, Object... args) {
        Constructor<?>[] array = cls.getConstructors();
        for (Constructor<?> c : array) {
            Class<?>[] parameterTypes = c.getParameterTypes(); // 构造方法的参数类信息
            if (parameterTypes.length != 0 && c.getModifiers() == Modifier.PUBLIC) {
                Object[] parameters = new Object[parameterTypes.length]; // 构造方法的参数值
                for (int i = 0; i < parameterTypes.length; i++) {
                    String className = parameterTypes[i].getName();

                    // 基础类型不能为null
                    if (className.equals("int")) {
                        parameters[i] = 0;
                    } else if (className.equals("long")) {
                        parameters[i] = 0;
                    } else if (className.equals("float")) {
                        parameters[i] = 0;
                    } else if (className.equals("double")) {
                        parameters[i] = 0;
                    } else if (className.equals("boolean")) {
                        parameters[i] = false;
                    } else if (className.equals("byte")) {
                        parameters[i] = (byte) 0;
                    } else if (className.equals("char")) {
                        parameters[i] = ' ';
                    } else if (className.equals("short")) {
                        parameters[i] = (short) 0;
                    } else if (className.equals(EasyetlContext.class.getName())) { // 通过构造方法注入容器上下文信息
                        parameters[i] = this.context;
                    } else {
                        parameters[i] = this.getBean(parameterTypes[i], args);
                    }
                }

                try {
                    return (E) c.newInstance(parameters);
                } catch (Throwable e) {
                    continue;
                }
            }
        }
        return null;
    }

}
