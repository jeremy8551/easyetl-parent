package icu.etl.ioc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import icu.etl.annotation.EasyBean;
import icu.etl.log.STD;
import icu.etl.util.ClassUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringComparator;
import icu.etl.util.StringUtils;

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
    private Vector<BeanCreator> creators;

    /** 组件（接口或类）与实现类的映射关系 */
    private LinkedHashMap<Class<?>, BeanClassList> beanMap;

    /** 组件接口与组件工厂类映射关系 */
    private LinkedHashMap<Class<?>, BeanBuilder<?>> builders;

    /** true 表示发生变化时可以通知 {@linkplain BeanEventListener} 对象 */
    private AtomicBoolean notice;

    /** 容器 */
    private Map<String, Object> ioc;

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
     * 上下文信息
     *
     * @param loader 类加载器
     * @param args   参数数组
     */
    public AnnotationEasyetlContext(ClassLoader loader, String... args) {
        this.classLoader = loader == null ? ClassUtils.getDefaultClassLoader() : loader;
        this.args = args;
        this.beanMap = new LinkedHashMap<Class<?>, BeanClassList>();
        this.builders = new LinkedHashMap<Class<?>, BeanBuilder<?>>();
        this.ioc = Collections.synchronizedMap(new LinkedHashMap<String, Object>(50));
        this.notice = new AtomicBoolean(false);
        this.creators = new Vector<BeanCreator>();
        this.creators.add(new StandardBeanCreator(this));
        new BeanClassLoader().load(this);
    }

    public <E> List<E> getAttribute(Class<E> cls) {
        Collection<Object> values = this.ioc.values();
        List<E> list = new ArrayList<E>(values.size());
        for (Iterator<Object> it = values.iterator(); it.hasNext(); ) {
            Object obj = it.next();
            Class<?> objClass = obj.getClass();

            // 判断接口
            if (cls.isInterface() && ClassUtils.isInterfacePresent(objClass, cls)) {
                list.add((E) obj);
                continue;
            }

            // 判断类名
            if (ClassUtils.isExtendClass(objClass, cls)) {
                list.add((E) obj);
                continue;
            }
        }
        return list;
    }

    public <E> E getBean(Class<E> type, Object... args) {
        E obj;
        for (BeanCreator c : this.creators) {
            if ((obj = c.getBean(type, args)) != null) {
                if (obj instanceof EasyetlContextAware) {
                    ((EasyetlContextAware) obj).setContext(this);
                }
                return obj;
            }
        }
        return null;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public String[] getArgument() {
        String[] array = new String[args.length];
        System.arraycopy(this.args, 0, array, 0, this.args.length);
        return array;
    }

    public Set<Class<?>> getBeanBuilderClass() {
        return Collections.unmodifiableSet(this.builders.keySet());
    }

    public BeanBuilder<?> getBeanBuilder(Class<?> type) {
        return this.builders.get(type);
    }

    public synchronized BeanBuilder<?> removeBeanBuilder(Class<?> type) {
        return this.builders.remove(type);
    }

    public boolean containsBeanClass(Class<?> type, Class<?> impl) {
        List<BeanClass> list = this.beanMap.get(type);
        if (list != null) {
            for (BeanClass obj : list) {
                if (obj.getBeanClass().equals(impl)) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized List<BeanClass> removeBeanClass(Class<?> type) {
        List<BeanClass> list = this.beanMap.remove(type);
        BeanBuilder<?> builder = this.builders.get(type);

        if (list != null && builder != null && this.notice.get() && (builder instanceof BeanEventListener)) {
            BeanEventListener listener = (BeanEventListener) builder;
            for (BeanClass bean : list) {
                listener.removeBean(new StandardBeanEvent(this, bean.getBeanClass(), bean.getAnnotation()));
            }
        }
        return list;
    }

    public synchronized boolean addBuilder(Class<?> type, BeanBuilder<?> builder) {
        if (type == null) {
            throw new NullPointerException();
        }
        if (builder == null) {
            throw new NullPointerException();
        }

        BeanBuilder<?> obj = this.builders.get(type);
        if (obj != null) {
            if (STD.out.isWarnEnabled()) {
                STD.out.warn(ResourcesUtils.getClassMessage(26, type.getName(), builder.getClass().getName(), obj.getClass().getName()));
            }
            return false;
        } else {
            if (STD.out.isDebugEnabled()) {
                STD.out.debug(ResourcesUtils.getClassMessage(19, type.getName(), builder.getClass().getName()));
            }
            this.builders.put(type, builder);
            return true;
        }
    }

    /**
     * 查询类实现的所有接口，添加所有接口与实现类的映射关系
     *
     * @param beanclass  组件信息
     * @param comparator 排序规则（不能重复添加组件）
     */
    public synchronized boolean addBean(BeanClass beanclass, Comparator<BeanClass> comparator) {
        if (beanclass == null) {
            return false;
        }

        Class<?> cls = beanclass.getBeanClass();
        List<Class<?>> interfaces = ClassUtils.getAllInterface(cls, null);

        // 判断是否是工厂类
        for (Class<?> interfaceCls : interfaces) {
            if (interfaceCls.getName().equals(BeanBuilder.class.getName())) {
                String[] generics = ClassUtils.getInterfaceGenerics(cls, BeanBuilder.class);
                if (generics.length == 1) {
                    Class<Object> type = ClassUtils.forName(generics[0], true, this.getClassLoader());
                    return this.addBuilder(type, ClassUtils.newInstance(cls));
                }
            }
        }

        // 添加类和父类 与实现类的映射关系
        Class supercls = cls;
        while (supercls != null) {
            this.addBean(supercls, beanclass, comparator);
            supercls = supercls.getSuperclass();
            if (supercls.equals(Object.class)) {
                break;
            }
        }

        // 添加接口与实现类的映射关系
        for (Class<?> type : interfaces) {
//            System.out.println("add " + type.getName() + " " + beanclass.getImplementClass() + " " + beanclass.getAnnotationAsImplement());
            this.addBean(type, beanclass, comparator);
        }
        return true;
    }

    private void addBean(Class<?> type, BeanClass beanConfig, Comparator<BeanClass> comparator) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getClassMessage(21, type.getName(), beanConfig.getBeanClass().getName()));
        }

        BeanClassList list = this.beanMap.get(type);
        if (list == null) {
            list = new BeanClassList();
            this.beanMap.put(type, list);
        }

        if (!list.contains(beanConfig, comparator)) {
            list.add(beanConfig);

            if (this.notice.get()) { // 使用监听器通知
                BeanBuilder<?> obj = this.builders.get(type);
                if (obj instanceof BeanEventListener) {
                    ((BeanEventListener) obj).addBean(new StandardBeanEvent(this, beanConfig.getBeanClass(), beanConfig.getAnnotation()));
                }
            }
        }
    }

    public Set<Class<?>> getBeanClasses() {
        return Collections.unmodifiableSet(this.beanMap.keySet());
    }

    public List<BeanClass> getBeanClassList(Class<?> type) {
        BeanClassList list = this.beanMap.get(type);
        if (list == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(list);
        }
    }

    public <E> Class<E> getBeanClass(Class<E> type, Object... args) {
        if (type == null) {
            throw new NullPointerException();
        }

        // 从数组中查询参数
        List<String> params = new ArrayList<String>(args.length);
        for (Object obj : args) {
            if ((obj instanceof String) && params.size() < 4) {
                params.add((String) obj);
            }
        }

        switch (params.size()) {
            case 0:// 没有参数
                return this.getImplement(type, "", "", "", "");
            case 1: // 一个参数
                return this.getImplement(type, params.get(0), "", "", "");
            case 2: // 二个参数
                return this.getImplement(type, params.get(0), params.get(1), "", "");
            case 3: // 三个参数
                return this.getImplement(type, params.get(0), params.get(1), params.get(2), "");
            case 4: // 四个参数
                return this.getImplement(type, params.get(0), params.get(1), params.get(2), params.get(3));
            default:
                throw new IllegalArgumentException(StringUtils.toString(params));
        }
    }

    /**
     * 查询系统中配置的类信息
     *
     * @param type  接口信息
     * @param kind  相同接口下的种类信息
     * @param mode  种类下的模式信息
     * @param major 相同模式下的内核版本号
     * @param minor 相同内核版本号下的发行版本号
     * @return 组件的实现类
     */
    private <E> Class<E> getImplement(Class<E> type, String kind, String mode, String major, String minor) {
        BeanClassList list = this.beanMap.get(type);

        // 没有任何实现类
        if (list == null || list.size() == 0) {
            return null;
        }

        // 只有一个实现类
        if (list.size() == 1) {
            return list.get(0).getBeanClass();
        }

        // kind 属性相同
        BeanClassCache buffer = new BeanClassCache(list.size());
        for (BeanClass bean : list) {
            EasyBean anno = bean.getAnnotation();
            if (anno != null && StringComparator.compareTo(kind, anno.kind()) == 0) {
                buffer.add(bean);
            }
        }

        if (buffer.onlyOne()) {
            return buffer.getOnlyOne();
        }

        // 过滤 mode 不同的类
        if (StringUtils.isNotBlank(mode)) {
            for (int i = 0; i < buffer.size(); i++) {
                BeanClass bean = buffer.get(i);
                EasyBean anno = bean.getAnnotation();
                if (anno != null && StringComparator.compareTo(mode, anno.mode()) != 0) {
                    buffer.remove(i--);
                }
            }

            if (buffer.onlyOne()) {
                return buffer.getOnlyOne();
            }
        }

        // 过滤大版本号不同的类
        for (int i = 0; i < buffer.size(); i++) {
            BeanClass bean = buffer.get(i);
            EasyBean anno = bean.getAnnotation();
            if (anno != null && StringComparator.compareTo(major, anno.major()) != 0) {
                buffer.remove(i--);
            }
        }

        if (buffer.onlyOne()) {
            return buffer.getOnlyOne();
        }

        // 过滤小版本号不同的类
        for (int i = 0; i < buffer.size(); i++) {
            BeanClass bean = buffer.get(i);
            EasyBean anno = bean.getAnnotation();
            if (anno != null && StringComparator.compareTo(minor, anno.minor()) != 0) {
                buffer.remove(i--);
            }
        }

        if (buffer.onlyOne()) {
            return buffer.getOnlyOne();
        } else { // 对应多个
            StringBuilder buf = new StringBuilder();
            for (BeanClass obj : buffer) {
                buf.append(obj.getBeanClass().getName()).append(" ");
            }
            String msg = buf.toString().trim();
            throw new RuntimeException(ResourcesUtils.getClassMessage(13, StringUtils.toString(new String[]{kind, mode, major, minor}), type.getName(), msg));
        }
    }

}
