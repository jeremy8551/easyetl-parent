package icu.etl.ioc;

import java.util.HashMap;
import java.util.Set;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ClassUtils;
import icu.etl.util.ResourcesUtils;

/**
 * 组件工厂管理器
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class BeanBuilderManager {
    private final static Log log = LogFactory.getLog(BeanBuilderManager.class);

    /** 容器上下文信息 */
    private EasyContext context;

    /** 组件接口与组件工厂类映射关系 */
    private HashMap<Class<?>, EasyBeanBuilder<?>> map;

    public BeanBuilderManager(EasyContext context) {
        this.map = new HashMap<Class<?>, EasyBeanBuilder<?>>();
        this.context = context;
    }

    /**
     * 注册组件
     *
     * @param type         组件的类信息
     * @param eventManager 事件管理器
     * @return 返回true表示注册成功，false表示注册失败
     */
    public boolean add(Class<?> type, BeanEventManager eventManager) {
        // 如果没有实现 EasyBeanBuilder 接口
        if (!EasyBeanBuilder.class.isAssignableFrom(type)) {
            return false;
        }

        String[] generics = ClassUtils.getInterfaceGenerics(type, EasyBeanBuilder.class);
        if (generics.length == 1) {
            String className = generics[0]; // BeanBuilder 类的范型
            Class<Object> genCls = ClassUtils.forName(className, true, this.context.getClassLoader());
            if (genCls == null) {
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("class.standard.output.msg011", className));
                }
                return false;
            }

            EasyBeanBuilder<?> builder = this.context.createBean(type);
            if (this.add(genCls, builder)) {
                // 如果组件工厂实现了监听接口
                if (builder instanceof BeanEventListener) {
                    eventManager.addListener((BeanEventListener) builder);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 注册组件工厂
     *
     * @param type    组件的类信息
     * @param builder 组件工厂类
     * @return 返回true表示注册成功，false表示注册失败
     */
    public boolean add(Class<?> type, EasyBeanBuilder<?> builder) {
        EasyBeanBuilder<?> factory = this.map.get(type);
        if (factory != null) {
            if (log.isWarnEnabled()) {
                log.warn(ResourcesUtils.getMessage("class.standard.output.msg026", type.getName(), builder.getClass().getName(), factory.getClass().getName()));
            }
            return false;
        } else {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("class.standard.output.msg019", type.getName(), builder.getClass().getName()));
            }
            this.map.put(type, builder);
            return true;
        }
    }

    /**
     * 返回组件工厂
     *
     * @param type 组件的类信息
     * @return 组件工厂
     */
    public EasyBeanBuilder<?> get(Class<?> type) {
        return this.map.get(type);
    }

    /**
     * 移除组件工厂
     *
     * @param type 组件的类信息
     * @return 组件工厂
     */
    public EasyBeanBuilder<?> remove(Class<?> type) {
        return this.map.remove(type);
    }

    /**
     * 返回组件的类信息集合
     *
     * @return 组件的类信息集合
     */
    public Set<Class<?>> keySet() {
        return this.map.keySet();
    }

    /**
     * 清空所有组件工厂
     */
    public void clear() {
        this.map.clear();
    }

}
