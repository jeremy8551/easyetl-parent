package icu.etl.ioc;

import java.util.ArrayList;
import java.util.List;

import icu.etl.ioc.impl.EasyBeanFactoryImpl;
import icu.etl.ioc.impl.EasyBeanInfoImpl;
import icu.etl.ioc.scan.BeanClassScanner;
import icu.etl.ioc.scan.BeanSpiScanner;
import icu.etl.ioc.scan.EasyScanPatternList;
import icu.etl.util.ClassUtils;
import icu.etl.util.Ensure;
import icu.etl.util.Settings;

/**
 * 容器上下文信息
 *
 * @author jeremy8551@qq.com
 */
public class EasyBeanContext implements EasyContext {

    /** 类加载器 */
    private ClassLoader classLoader;

    /** 启动参数 */
    private String[] args;

    /** Ioc容器管理器 */
    private ContainerContextManager iocManager;

    /** 组件信息表 */
    private EasyBeanTable table;

    /** 单个组件的构建工厂 */
    private BeanBuilderManager builders;

    /** 所有组件的实例化工厂 */
    private EasyBeanFactoryImpl factory;

    /** 事件管理器 */
    private BeanEventManager eventManager;

    /** 上级容器 */
    private EasyContext parent;

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
     * @param classLoader 类加载器
     * @param args        参数数组
     */
    public EasyBeanContext(ClassLoader classLoader, String... args) {
        this(classLoader);
        this.setArgument(args);

        // 扫描并加载组件
        EasyScanPatternList list = new EasyScanPatternList();
        list.addProperty();
        list.addArgument(args);
        list.addGroupID();
        this.loadBeanInfo(list.toArray());
    }

    /**
     * 容器上下文信息
     *
     * @param classLoader 类加载器
     */
    public EasyBeanContext(ClassLoader classLoader) {
        this.setClassLoader(classLoader);
        this.iocManager = new ContainerContextManager(this);
        this.factory = new EasyBeanFactoryImpl(this);
        this.table = new EasyBeanTable(this);
        this.eventManager = new BeanEventManager(this);
        this.builders = new BeanBuilderManager(this);
    }

    public EasyContext getParent() {
        return parent;
    }

    public void setParent(EasyContext parent) {
        this.parent = parent;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = (classLoader == null) ? ClassUtils.getDefaultClassLoader() : classLoader;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public String[] getArgument() {
        String[] array = new String[this.args.length];
        System.arraycopy(this.args, 0, array, 0, this.args.length);
        return array;
    }

    public void setArgument(String... args) {
        this.args = args;
    }

    public synchronized EasyContainerContext removeIoc(String name) {
        return this.iocManager.remove(name);
    }

    public synchronized EasyContainerContext addIoc(EasyContainerContext ioc) {
        return this.iocManager.add(ioc);
    }

    public <E> E createBean(Class<?> type, Object... args) {
        return this.factory.createBean(type, args);
    }

    public synchronized void removeBeanInfo() {
        this.table.clear();
        this.builders.clear();
        this.eventManager.clear();
    }

    public synchronized int loadBeanInfo(String... args) {
        // SPI扫描
        BeanSpiScanner scanner1 = new BeanSpiScanner();
        int load1 = scanner1.load(this);

        // 类扫描
        BeanClassScanner scanner2 = new BeanClassScanner();
        int load2 = scanner2.load(this, args);

        // 刷新
        this.table.refresh();
        return load1 + load2;
    }

    public EasyBeanInfoValue getBeanInfo(Class<?> type, String name) {
        Ensure.notNull(type);
        return this.table.get(type).indexOf(name).getBeanInfo();
    }

    public <E> E getBean(Class<E> type, Object... args) {
        return this.iocManager.getBean(type, args);
    }

    public boolean addBean(Class<?> type) {
        return this.addBean(new EasyBeanInfoImpl(type));
    }

    public synchronized boolean addBean(EasyBeanInfoValue beanInfo) {
        if (beanInfo == null) {
            return false;
        }

        boolean add = false;
        Class<?> cls = beanInfo.getType();
        if (this.builders.add(cls, this.eventManager)) {
            add = true;
        }

        // 添加类和父类 与实现类的映射关系
        Class<?> supercls = cls;
        while (supercls != null && !supercls.equals(Object.class)) {
            if (this.table.get(supercls).push(beanInfo)) {
                this.eventManager.addBeanEvent(beanInfo);
                add = true;
            }
            supercls = supercls.getSuperclass();
        }

        // 添加接口与实现类的映射关系
        List<Class<?>> interfaces = ClassUtils.getAllInterface(cls, null);
        for (Class<?> type : interfaces) {
            if (this.table.get(type).push(beanInfo)) {
                this.eventManager.addBeanEvent(beanInfo);
                add = true;
            }
        }
        return add;
    }

    public synchronized List<EasyBeanInfo> removeBeanInfoList(Class<?> type) {
        return new ArrayList<EasyBeanInfo>(this.table.remove(type));
    }

    public boolean containsBeanInfo(Class<?> type, Class<?> cls) {
        return this.table.get(type).contains(cls);
    }

    public List<EasyBeanInfo> getBeanInfoList(Class<?> type) {
        return new ArrayList<EasyBeanInfo>(this.table.get(type));
    }

    public List<EasyBeanInfo> getBeanInfoList(Class<?> type, String name) {
        Ensure.notNull(type);
        return new ArrayList<EasyBeanInfo>(this.table.get(type).indexOf(name));
    }

    public List<Class<?>> getBeanInfoTypes() {
        return new ArrayList<Class<?>>(this.table.keySet());
    }

    public List<Class<?>> getBeanBuilderType() {
        return new ArrayList<Class<?>>(this.builders.keySet());
    }

    public EasyBeanBuilder<?> getBeanBuilder(Class<?> type) {
        return this.builders.get(type);
    }

    public synchronized EasyBeanBuilder<?> removeBeanBuilder(Class<?> type) {
        return this.builders.remove(type);
    }

    public synchronized boolean addBeanBuilder(Class<?> type, EasyBeanBuilder<?> builder) {
        Ensure.notNull(type);
        Ensure.notNull(builder);
        return this.builders.add(type, builder);
    }

    public String getName() {
        return Settings.getApplicationName();
    }
}
