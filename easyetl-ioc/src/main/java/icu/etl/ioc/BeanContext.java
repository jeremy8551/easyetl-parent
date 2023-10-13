package icu.etl.ioc;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringComparator;
import icu.etl.util.StringUtils;

/**
 * 组件工厂的上下文信息
 *
 * @author jeremy8551@qq.com
 */
public class BeanContext implements BeanRegister {

    /** 类加载器 */
    private ClassLoader classLoader;

    /** 启动参数 */
    private String[] args;

    /** 组件工厂集合 */
    private Vector<BeanCreator> creators;

    /** 组件接口或组件类与实现类的映射关系 */
    private LinkedHashMap<Class<?>, List<BeanConfig>> impls;

    /** 组件接口与组件工厂类映射关系 */
    private LinkedHashMap<Class<?>, BeanBuilder<?>> builders;

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
    public BeanContext(String... args) {
        this(null, args);
    }

    /**
     * 上下文信息
     *
     * @param loader 类加载器
     * @param args   参数数组
     */
    public BeanContext(ClassLoader loader, String... args) {
        BeanFactory.setContext(this);
        this.impls = new LinkedHashMap<Class<?>, List<BeanConfig>>();
        this.builders = new LinkedHashMap<Class<?>, BeanBuilder<?>>();
        this.notice = new AtomicBoolean(false);
        this.creators = new Vector<BeanCreator>();
        this.creators.add(new StandardBeanCreator(this));
        this.classLoader = loader;
        this.args = args;
        new BeanContextInit().load(this);
    }

    /**
     * 返回类加载器
     *
     * @return 类加载器
     */
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * 返回启动参数
     *
     * @return 启动参数数组
     */
    public String[] getArgument() {
        String[] array = new String[args.length];
        System.arraycopy(this.args, 0, array, 0, this.args.length);
        return array;
    }

    /**
     * 返回组件工厂集合
     *
     * @return 组件工厂集合
     */
    public List<BeanCreator> getCreators() {
        return this.creators;
    }

    /**
     * 保存接口信息与工厂类的映射关系
     *
     * @param type    接口信息
     * @param builder 接口的工厂类
     * @return 返回true表示添加成功 false表示未添加
     */
    public synchronized boolean addBuilder(Class<?> type, BeanBuilder<?> builder) {
        if (type == null) {
            throw new NullPointerException();
        }
        if (builder == null) {
            throw new NullPointerException();
        }

        BeanBuilder<?> obj = this.builders.get(type);
        if (obj != null && builder.getClass().equals(obj.getClass())) {
            return false;
        } else {
            this.builders.put(type, builder);
            return true;
        }
    }

    /**
     * 返回所有组件工厂的类信息（按组件添加的顺序）
     *
     * @return 返回组件工程集合
     */
    public Set<Class<?>> getBuilderClass() {
        return Collections.unmodifiableSet(this.builders.keySet());
    }

    /**
     * 查询接口信息对应的工厂
     *
     * @param type 接口信息
     * @return 组件工厂的类信息
     */
    public BeanBuilder<?> getBuilder(Class<?> type) {
        return this.builders.get(type);
    }

    /**
     * 删除接口信息对应的工厂
     *
     * @param type 接口信息
     * @return 组件工厂实例
     */
    public synchronized BeanBuilder<?> removeBuilder(Class<?> type) {
        return this.builders.remove(type);
    }

    /**
     * 判断接口的实现类是否存在
     *
     * @param type 接口信息
     * @param impl 接口的实现信息
     * @return true表示存在组件实现类
     */
    public boolean containsImplement(Class<?> type, Class<?> impl) {
        List<BeanConfig> list = this.impls.get(type);
        if (list != null) {
            for (BeanConfig obj : list) {
                if (obj.getImplementClass().equals(impl)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 删除接口信息对应的实现类集合
     *
     * @param type 组件信息
     * @return 组件实现类的集合
     */
    public synchronized List<BeanConfig> removeImplement(Class<?> type) {
        List<BeanConfig> list = this.impls.remove(type);
        BeanBuilder<?> builder = this.builders.get(type);

        if (list != null && builder != null && this.notice.get() && (builder instanceof BeanEventListener)) {
            BeanEventListener listener = (BeanEventListener) builder;
            for (BeanConfig bean : list) {
                listener.removeImplement(new StandardBeanEvent(this, bean.getImplementClass(), bean.getAnnotation()));
            }
        }
        return list;
    }

    public synchronized void add(BeanConfig anno, Comparator<BeanConfig> comparator) {
        if (anno == null) {
            return;
        }

        Class<?> type = anno.getType();
        Annotation annotation = anno.getAnnotation();
        Class<Object> cls = anno.getImplementClass();

        List<BeanConfig> list = this.impls.get(type);
        if (list == null) {
            list = new ArrayList<BeanConfig>();
            this.impls.put(type, list);
        }

        boolean add = true;
        for (BeanConfig bean : list) {
            if (comparator == null) {
                if (anno.getImplementClass().equals(bean.getImplementClass())) {
                    add = false;
                    break;
                }
            } else if (comparator.compare(anno, bean) == 0) {
                add = false;
                break;
            }
        }

        if (add) {
            list.add(anno);

            if (this.notice.get()) { // 使用监听器通知
                BeanBuilder<?> obj = this.builders.get(type);
                if (obj instanceof BeanEventListener) {
                    ((BeanEventListener) obj).addImplement(new StandardBeanEvent(this, cls, annotation));
                }
            }
        }
    }

    /**
     * 返回所有组件类信息（按组件添加的顺序）
     *
     * @return 组件种类的集合
     */
    public Set<Class<?>> getImplements() {
        return Collections.unmodifiableSet(this.impls.keySet());
    }

    /**
     * 查找类信息对应的实现类集合
     *
     * @param type 类信息
     * @return 组件对应的所有实现类
     */
    public List<BeanConfig> getImplements(Class<?> type) {
        List<BeanConfig> list = this.impls.get(type);
        if (list == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(list);
        }
    }

    /**
     * 查询接口信息对应的实现类
     *
     * @param type 接口信息
     * @param args 查询参数 <br>
     *             数组中第一个字符串对应 {@linkplain EasyBeanClass#kind()} <br>
     *             数组中第二个字符串对应 {@linkplain EasyBeanClass#mode()} <br>
     *             数组中第三个字符串对应 {@linkplain EasyBeanClass#major()} <br>
     *             数组中第四个字符串对应 {@linkplain EasyBeanClass#minor()} <br>
     * @param <E>  组件类信息
     * @return 组件的实现类
     */
    public <E> Class<E> getImplement(Class<E> type, Object... args) {
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
        List<BeanConfig> list = this.impls.get(type);

        // 没有任何实现类
        if (list == null || list.size() == 0) {
            return null;
        }

        // 只有一个实现类
        if (list.size() == 1) {
            return list.get(0).getImplementClass();
        }

        // kind 属性相同
        BeanAnnotationList beans = new BeanAnnotationList(list.size());
        for (BeanConfig bean : list) {
            EasyBeanClass anno = bean.getAnnotationAsImplement();
            if (anno != null && StringComparator.compareTo(kind, anno.kind()) == 0) {
                beans.add(bean);
            }
        }

        if (beans.onlyOne()) {
            return beans.getOnlyOne();
        }

        // 过滤 mode 不同的类
        if (StringUtils.isNotBlank(mode)) {
            for (int i = 0; i < beans.size(); i++) {
                BeanConfig bean = beans.get(i);
                EasyBeanClass anno = bean.getAnnotationAsImplement();
                if (anno != null && StringComparator.compareTo(mode, anno.mode()) != 0) {
                    beans.remove(i--);
                }
            }

            if (beans.onlyOne()) {
                return beans.getOnlyOne();
            }
        }

        // 过滤大版本号不同的类
        for (int i = 0; i < beans.size(); i++) {
            BeanConfig bean = beans.get(i);
            EasyBeanClass anno = bean.getAnnotationAsImplement();
            if (anno != null && StringComparator.compareTo(major, anno.major()) != 0) {
                beans.remove(i--);
            }
        }

        if (beans.onlyOne()) {
            return beans.getOnlyOne();
        }

        // 过滤小版本号不同的类
        for (int i = 0; i < beans.size(); i++) {
            BeanConfig bean = beans.get(i);
            EasyBeanClass anno = bean.getAnnotationAsImplement();
            if (anno != null && StringComparator.compareTo(minor, anno.minor()) != 0) {
                beans.remove(i--);
            }
        }

        if (beans.onlyOne()) {
            return beans.getOnlyOne();
        } else { // 对应多个
            StringBuilder buf = new StringBuilder();
            for (BeanConfig obj : beans) {
                buf.append(obj.getImplementClass().getName()).append(" ");
            }
            String msg = buf.toString().trim();
            throw new RuntimeException(ResourcesUtils.getClassMessage(13, StringUtils.toString(new String[]{kind, mode, major, minor}), type.getName(), msg));
        }
    }

}
