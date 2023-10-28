package icu.etl.ioc;

import java.util.ArrayList;
import java.util.List;

import icu.etl.util.ClassUtils;

/**
 * 容器上下文信息
 *
 * @author jeremy8551@qq.com
 */
public class EasyBeanContext implements EasyContext {

    /** 组件工厂集合 */
    private IocContextManager iocs;

    /** 组件（接口或类）与实现类的映射关系 */
    private BeanInfoManager beans;

    /** 组件接口与组件工厂类映射关系 */
    private BeanBuilderManager builders;

    /** 组件构造方法的工具 */
    private BeanFactoryImpl factory;

    /** 监听器管理 */
    private BeanEventManager listeners;

    /** 参数管理器 */
    private EasyContextInit init;

    /**
     * 上下文信息
     *
     * @param args 参数数组，格式如下:
     *             org.test 表示扫描这个包名下的类信息
     *             !org.test 表示扫描包时，排除掉这个包名下的类
     *             sout:debug 表示使用控制台输出debug级别的日志
     */
    public EasyBeanContext(String... args) {
        this(null, args);
    }

    /**
     * 容器上下文信息
     *
     * @param loader 类加载器
     * @param args   参数数组
     */
    public EasyBeanContext(ClassLoader loader, String... args) {
        this.init = new EasyContextInit(loader, args);
        this.iocs = new IocContextManager(this);
        this.factory = new BeanFactoryImpl(this);
        this.beans = new BeanInfoManager(this);
        this.listeners = new BeanEventManager(this);
        this.builders = new BeanBuilderManager(this);
        this.refresh();
    }

    /**
     * 初始化操作
     */
    public synchronized void refresh() {
        // 清空数据
        this.beans.clear();
        this.builders.clear();
        this.listeners.clear();

        // 重新加载
        this.init.scann(this);
        this.beans.refresh();
    }

    /**
     * 返回上下文编号
     *
     * @return 唯一编号
     */
    public String getId() {
        return "easyetl";
    }

    public ClassLoader getClassLoader() {
        return this.init.getClassLoader();
    }

    public String[] getArgument() {
        return this.init.getArgument();
    }

    public synchronized IocContext removeIoc(String name) {
        return this.iocs.remove(name);
    }

    public synchronized IocContext addIoc(IocContext ioc) {
        return this.iocs.add(ioc);
    }

    public <E> E createBean(Class<?> type, Object... args) {
        return this.factory.createBean(type, args);
    }

    public BeanInfoRegister getBeanInfo(Class<?> type, String name) {
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
     * @param beanInfo 组件信息
     * @return 返回true表示添加成功 false表示失败
     */
    public synchronized boolean addBean(BeanInfoRegister beanInfo) {
        if (beanInfo == null) {
            return false;
        }

        boolean add = false;
        Class<?> cls = beanInfo.getType();
        if (this.builders.add(cls, this.listeners)) {
            add = true;
        }

        // 添加类和父类 与实现类的映射关系
        Class<?> supercls = cls;
        while (supercls != null && !supercls.equals(Object.class)) {
            if (this.beans.get(supercls).push(beanInfo)) {
                this.listeners.addBeanEvent(beanInfo);
                add = true;
            }
            supercls = supercls.getSuperclass();
        }

        // 添加接口与实现类的映射关系
        List<Class<?>> interfaces = ClassUtils.getAllInterface(cls, null);
        for (Class<?> type : interfaces) {
            if (this.beans.get(type).push(beanInfo)) {
                this.listeners.addBeanEvent(beanInfo);
                add = true;
            }
        }
        return add;
    }

    public synchronized List<BeanInfo> removeBeanInfoList(Class<?> type) {
        return new ArrayList<BeanInfo>(this.beans.remove(type));
    }

    public boolean containsBeanInfo(Class<?> type, Class<?> cls) {
        return this.beans.get(type).contains(cls);
    }

    public List<BeanInfo> getBeanInfoList(Class<?> type) {
        return new ArrayList<BeanInfo>(this.beans.get(type));
    }

    public List<BeanInfo> getBeanInfoList(Class<?> type, String name) {
        if (type == null) {
            throw new NullPointerException();
        }
        return new ArrayList<BeanInfo>(this.beans.get(type).indexOf(name));
    }

    public List<Class<?>> getBeanInfoTypes() {
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

    public synchronized boolean addBeanBuilder(Class<?> type, BeanBuilder<?> builder) {
        if (type == null) {
            throw new NullPointerException();
        }
        if (builder == null) {
            throw new NullPointerException();
        }
        return this.builders.add(type, builder);
    }

}
