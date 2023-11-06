package icu.etl.ioc;

import java.util.HashMap;
import java.util.Set;

import icu.etl.util.ClassUtils;
import icu.etl.util.ResourcesUtils;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class BeanBuilderManager {

    private EasyContext context;

    /** 组件接口与组件工厂类映射关系 */
    private final HashMap<Class<?>, BeanBuilder<?>> map;

    public BeanBuilderManager(EasyContext context) {
        this.map = new HashMap<Class<?>, BeanBuilder<?>>();
        this.context = context;
    }

    public boolean add(Class<?> cls, BeanEventManager register) {
        /** 如果没有实现 {@linkplain BeanBuilder} 接口 **/
        if (!BeanBuilder.class.isAssignableFrom(cls)) {
            return false;
        }

        String[] generics = ClassUtils.getInterfaceGenerics(cls, BeanBuilder.class);
        if (generics.length == 1) {
            String className = generics[0]; // BeanBuilder 类的范型
            Class<Object> genCls = ClassUtils.forName(className, true, this.context.getClassLoader());
            if (genCls == null) {
                if (Ioc.out.isDebugEnabled()) {
                    Ioc.out.debug(ResourcesUtils.getClassMessage(11, className));
                }
                return false;
            }

            BeanBuilder<?> builder = this.context.createBean(cls);
            if (this.add(genCls, builder)) {
                // 如果组件工厂实现了监听接口
                if (builder instanceof BeanEventListener) {
                    register.addListener((BeanEventListener) builder);
                }
                return true;
            }
        }
        return false;
    }

    public boolean add(Class<?> type, BeanBuilder<?> builder) {
        BeanBuilder<?> factory = this.map.get(type);
        if (factory != null) {
            if (Ioc.out.isWarnEnabled()) {
                Ioc.out.warn(ResourcesUtils.getClassMessage(26, type.getName(), builder.getClass().getName(), factory.getClass().getName()));
            }
            return false;
        } else {
            if (Ioc.out.isDebugEnabled()) {
                Ioc.out.debug(ResourcesUtils.getClassMessage(19, type.getName(), builder.getClass().getName()));
            }
            this.map.put(type, builder);
            return true;
        }
    }

    public BeanBuilder<?> get(Class<?> type) {
        return this.map.get(type);
    }

    public BeanBuilder<?> remove(Class<?> type) {
        return this.map.remove(type);
    }

    public Set<Class<?>> keySet() {
        return this.map.keySet();
    }

    public void clear() {
        this.map.clear();
    }

}
