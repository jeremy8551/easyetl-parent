package icu.etl.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import icu.etl.util.ResourcesUtils;

public class StandardBeanCreator implements BeanCreator {

    /** 上下文信息 */
    private BeanContext context;

    /**
     * 初始化
     *
     * @param context 上下文信息
     */
    public StandardBeanCreator(BeanContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public <E> E getBean(Class<E> cls, Object... array) {
        // 优先使用接口工厂生成实例对象
        BeanBuilder<?> obj = this.context.getBuilder(cls);
        if (obj != null) {
            try {
                return (E) obj.build(this.context, array);
            } catch (Throwable e) {
                throw new RuntimeException(ResourcesUtils.getClassMessage(12, cls.getName()), e);
            }
        }

        // 查询接口的实现类
        Class<E> beanCls = this.context.getImplement(cls, array);
        if (beanCls == null) {
            return null;
        } else {
            return this.newInstance(beanCls);
        }
    }

    /**
     * 使用 Class 生成一个实例对象
     *
     * @param cls 类信息
     * @param <E> 类信息
     * @return 实例对象
     */
    private <E> E newInstance(Class<E> cls) {
        if (cls == null) {
            return null;
        } else {
            try {
                return cls.newInstance();
            } catch (Throwable e) {
                E obj = this.createByConstructor(cls);
                if (obj != null) {
                    return obj;
                }
                throw new IllegalArgumentException(ResourcesUtils.getClassMessage(12, cls.getName()), e);
            }
        }
    }

    /**
     * 向构造方法中注入参数，并将类实例化
     *
     * @param <E> 类
     * @param cls 类信息
     * @return 类的实例对象
     */
    @SuppressWarnings("unchecked")
    public <E> E createByConstructor(Class<E> cls) {
        Constructor<?>[] array = cls.getConstructors();
        for (Constructor<?> c : array) {
            Class<?>[] typeCls = c.getParameterTypes(); // 构造方法的参数类信息
            if (typeCls.length != 0 && c.getModifiers() == Modifier.PUBLIC) {
                Object[] typeVal = new Object[typeCls.length]; // 构造方法的参数值
                for (int i = 0; i < typeCls.length; i++) {
                    String name = typeCls[i].getName();

                    // 基础类型不能为null
                    if (name.equals("int")) {
                        typeVal[i] = 0;
                    } else if (name.equals("long")) {
                        typeVal[i] = 0;
                    } else if (name.equals("float")) {
                        typeVal[i] = 0;
                    } else if (name.equals("double")) {
                        typeVal[i] = 0;
                    } else if (name.equals("boolean")) {
                        typeVal[i] = false;
                    } else if (name.equals("byte")) {
                        typeVal[i] = (byte) 0;
                    } else if (name.equals("char")) {
                        typeVal[i] = ' ';
                    } else if (name.equals("short")) {
                        typeVal[i] = (short) 0;
                    } else {
                        typeVal[i] = this.getBean(typeCls[i]);
                    }
                }

                try {
                    return (E) c.newInstance(typeVal);
                } catch (Throwable e) {
                    continue;
                }
            }
        }
        return null;
    }

}
