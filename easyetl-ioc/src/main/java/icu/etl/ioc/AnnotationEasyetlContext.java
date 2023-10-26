package icu.etl.ioc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import icu.etl.util.ClassUtils;

/**
 * 组件工厂的上下文信息
 *
 * @author jeremy8551@qq.com
 */
public class AnnotationEasyetlContext implements EasyetlContext {

    /** 类加载器 */
    private ClassLoader classLoader;

    /** 启动参数 */
    private String[] args;

    /** 组件工厂集合 */
    private EasyetlIocManager iocs;

    /** 组件（接口或类）与实现类的映射关系 */
    private BeanInfoManager beans;

    /** 组件接口与组件工厂类映射关系 */
    private BeanBuilderManager builders;

    /** 组件构造方法的工具 */
    private BeanConstructor factory;

    /** true 表示发生变化时可以通知 {@linkplain BeanEventListener} 对象 */
    private AtomicBoolean notice;

    /**
     * 上下文信息
     *
     * @param args 参数数组，格式如下:
     *             org.test 表示扫描这个包名下的类信息
     *             !org.test 表示扫描包时，排除掉这个包名下的类
     *             sout:debug 表示使用控制台输出debug级别的日志
     */
    public AnnotationEasyetlContext(String... args) {
        this(null, args);
    }

    /**
     * 容器上下文信息
     *
     * @param loader 类加载器
     * @param args   参数数组
     */
    public AnnotationEasyetlContext(ClassLoader loader, String... args) {
        this.classLoader = (loader == null) ? ClassUtils.getDefaultClassLoader() : loader;
        this.args = args;
        this.iocs = new EasyetlIocManager(this);
        this.factory = new BeanConstructor(this);
        this.beans = new BeanInfoManager();
        this.builders = new BeanBuilderManager(this);
        this.notice = new AtomicBoolean(false);
        this.refresh();
    }

    /**
     * 初始化操作
     */
    public synchronized void refresh() {
        this.beans.clear();
        this.builders.clear();
        new BeanClassLoader().load(this);
        this.beans.refresh();
        List<BeanInfo> list = this.beans.getNolazyBeanInfoList();
        for (BeanInfo beanInfo : list) {
            this.createBean(beanInfo.getType());
        }
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public String[] getArgument() {
        String[] array = new String[args.length];
        System.arraycopy(this.args, 0, array, 0, this.args.length);
        return array;
    }

    public synchronized EasyetlIoc removeIoc(String name) {
        return this.iocs.remove(name);
    }

    public synchronized EasyetlIoc addIoc(EasyetlIoc ioc) {
        return this.iocs.add(ioc);
    }

    public <E> E createBean(Class<?> type, Object... args) {
        return this.factory.newInstance(type, args);
    }

    public BeanInfo getBeanInfo(Class<?> type, String name) {
        if (type == null) {
            throw new NullPointerException();
        }
        return this.beans.get(type).indexOf(name).getBeanInfo();
    }

    public <E> E getBean(Class<E> type, Object... args) {
        return this.iocs.getBean(type, args);
    }

    /**
     * 查询类实现的所有接口，添加所有接口与实现类的映射关系
     *
     * @param beanInfo   组件信息
     * @param comparator 判断组件重复的规则，可以为null
     * @return 返回true表示添加成功 false表示失败
     */
    public synchronized boolean addBean(BeanInfo beanInfo, Comparator<BeanInfo> comparator) {
        if (beanInfo == null) {
            return false;
        }

        boolean add = false;
        Class<?> cls = beanInfo.getType();

        List<Class<?>> interfaces = ClassUtils.getAllInterface(cls, null);
        for (Class<?> incls : interfaces) {
            // 添加组件工厂
            if (ClassUtils.equals(BeanBuilder.class, incls)) {
                if (this.builders.add(cls)) {
                    add = true;
                }
            }

            // 添加监听器
            if (ClassUtils.equals(BeanEventListener.class, incls)) {
                this.beans.addListener(this.createBean(cls));
                add = true;
            }
        }

        // 添加类和父类 与实现类的映射关系
        Class<?> supercls = cls;
        while (supercls != null && !supercls.equals(Object.class)) {
            if (this.beans.get(supercls).add(beanInfo, comparator)) {
                add = true;
            }
            supercls = supercls.getSuperclass();
        }

        // 添加接口与实现类的映射关系
        for (Class<?> type : interfaces) {
            if (this.beans.get(type).add(beanInfo, comparator)) {
                add = true;
            }
        }
        return add;
    }

    public synchronized List<BeanInfo> removeBeanInfoList(Class<?> type) {
        return Collections.unmodifiableList(this.beans.remove(type));
    }

    public boolean containsBeanInfo(Class<?> type, Class<?> cls) {
        return this.beans.get(type).contains(cls);
    }

    public List<BeanInfo> getBeanInfoList(Class<?> type) {
        return Collections.unmodifiableList(this.beans.get(type));
    }

    public List<BeanInfo> getBeanInfoList(Class<?> type, String name) {
        if (type == null) {
            throw new NullPointerException();
        }
        return Collections.unmodifiableList(this.beans.get(type).indexOf(name));
    }

    public List<Class<?>> getTypes() {
        return new ArrayList<Class<?>>(this.beans.keySet());
    }

    public List<Class<?>> getBeanBuilderType() {
        return new ArrayList<Class<?>>(this.builders.keySet());
    }

    public BeanBuilder<?> getBeanBuilder(Class<?> type) {
        return this.builders.get(type);
    }

    public synchronized BeanBuilder<?> removeBeanBuilder(Class<?> type) {
        return this.builders.remove(type);
    }

    public synchronized boolean addBuilder(Class<?> type, BeanBuilder<?> builder) {
        if (type == null) {
            throw new NullPointerException();
        }
        if (builder == null) {
            throw new NullPointerException();
        }
        return this.builders.add(type, builder);
    }

}
