package icu.etl.util;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * 类信息工具
 *
 * @author jeremy8551@qq.com
 * @createtime 2011-05-24
 */
public class ClassUtils {

    /** JDK日志输出接口 */
    private final static Logger log = Logger.getLogger(ClassUtils.class.getName());

    /** 设置脚本引擎默认 classpath 绝对路径 */
    public final static String PROPERTY_CLASSPATH = ClassUtils.class.getPackage().getName().split("\\.")[0] + "." + ClassUtils.class.getPackage().getName().split("\\.")[1] + ".classpath";

    /** 当前JAVA虚拟机的默认类路径 */
    public static String CLASSPATH = System.getProperty(PROPERTY_CLASSPATH);

    public ClassUtils() {
    }

    /**
     * 确定对象是否是JAVA的基本类型：<br>
     * String<br>
     * int<br>
     * byte<br>
     * short<br>
     * long<br>
     * float<br>
     * double<br>
     * char<br>
     * boolean<br>
     *
     * @param obj 对象
     * @return 返回true表示参数是JAVA的基本类型
     */
    public static boolean isPrimitiveTypes(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            return true;
        }
        if (obj instanceof Integer) {
            return true;
        }
        if (obj instanceof Byte) {
            return true;
        }
        if (obj instanceof Short) {
            return true;
        }
        if (obj instanceof Long) {
            return true;
        }
        if (obj instanceof Float) {
            return true;
        }
        if (obj instanceof Double) {
            return true;
        }
        if (obj instanceof Character) {
            return true;
        }
        if (obj instanceof Boolean) {
            return true;
        }
        return false;
    }

    /**
     * 判断类信息参数 cls 是否在类信息集合参数 c 范围内（使用类信息全名是否相等来判断类信息是否相等）
     *
     * @param cls 类信息
     * @param c   类信息集合
     * @return 返回 true 表示类信息在集合范围内
     */
    public static boolean inCollection(Class<?> cls, Collection<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }

        if (cls == null) {
            for (Iterator<?> it = c.iterator(); it.hasNext(); ) {
                if (it.next() == null) {
                    return true;
                }
            }
        } else {
            for (Iterator<?> it = c.iterator(); it.hasNext(); ) {
                Object obj = it.next();
                if (obj == null) {
                    continue;
                } else {
                    Class<?> cs = (Class<?>) (obj instanceof Class ? obj : obj.getClass());
                    if (cs.getName().equals(cls.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断类信息参数 cls 是否在类信息数组参数 array 范围内（使用类信息全名是否相等来判断类信息是否相等）
     *
     * @param cls   类信息
     * @param array 类信息数组
     * @return 返回 true 表示类信息在数组范围内，返回 false 表示类信息不再数组范围内
     */
    public static boolean inArray(Class<?> cls, Class<?>... array) {
        if (cls == null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] == null) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < array.length; i++) {
                Class<?> cs = array[i];
                if (cs != null && cs.getName().equals(cls.getName())) { // 判断类信息全名是否相等
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 返回对象或类参数 obj 类信息中的属性名
     *
     * @param obj  类信息
     * @param name 属性名
     * @return 格式：类全名.属性名
     * @throws RuntimeException 如果属性不存在则抛出异常
     */
    public static String toFieldName(Object obj, String name) {
        Field f = null;
        Class<?> cls = (obj instanceof Class) ? ((Class<?>) obj) : obj.getClass();
        try {
            f = cls.getDeclaredField(name);
        } catch (Throwable e) {
            Field[] array = cls.getDeclaredFields();
            for (Field field : array) {
                if (field.getName().equals(name)) {
                    f = field;
                    break;
                }
            }
        }

        if (f == null) {
            throw new RuntimeException(name);
        } else {
            return cls.getName() + "." + f.getName();
        }
    }

    /**
     * 返回对象或类参数 obj 上的方法
     *
     * @param obj   类信息
     * @param name  方法名
     * @param types 方法上所有参数的类型
     * @return 格式：方法名(参数类1，参数类2，参数类3 ..)
     * @throws RuntimeException 如果方法不存在则抛出异常
     */
    public static String toMethodName(Object obj, String name, Class<?>... types) {
        Method method = ClassUtils.getMethod(obj, name, types);
        if (method == null) {
            throw new RuntimeException(name);
        }

        StringBuilder buf = new StringBuilder();
        buf.append(method.getName());
        buf.append("(");
        Class<?>[] array = method.getParameterTypes(); // 返回所有参数类型
        for (int i = 0; i < array.length; ) {
            String className = array[i].getSimpleName();
            buf.append(className);
            if (++i < array.length) {
                buf.append(", ");
            }
        }
        buf.append(")");
        return buf.toString();
    }

    /**
     * 调用java对象 obj 中的方法 name
     *
     * @param obj  JAVA对象
     * @param name 方法名
     * @param args 方法参数值
     * @return 方法返回值
     */
    public static Object executeMethod(Object obj, String name, Object... args) {
        Method method = ClassUtils.getMethod(obj, name);
        try {
            return method.invoke(obj, args);
        } catch (Throwable e) {
            throw new RuntimeException(obj.getClass().getSimpleName() + "." + name, e);
        }
    }

    /**
     * 在java对象中查找方法 name
     *
     * @param obj            JAVA对象
     * @param name           方法名
     * @param parameterTypes 方法的输入参数类型
     * @return 方法对象
     */
    public static Method getMethod(Object obj, String name, Class<?>... parameterTypes) {
        Class<?> cls = (obj instanceof Class) ? ((Class<?>) obj) : obj.getClass();
        try {
            Method method = cls.getMethod(name, parameterTypes);
            return method;
        } catch (Throwable e) {
            Method[] methods = cls.getDeclaredMethods();
            List<Method> list = new ArrayList<Method>(methods.length);
            for (Method method : methods) {
                if (method.getName().equals(name)) { // 判断方法名是否相等
                    list.add(method);
                }
            }

            if (list.isEmpty()) {
                return null;
            } else if (list.size() == 1) {
                return list.get(0);
            } else {
                for (Method method : list) {
                    Class<?>[] types = method.getParameterTypes();
                    if (types.length == parameterTypes.length) {
                        boolean b = true;
                        for (int i = 0; i < types.length; i++) {
                            if (!types[i].equals(parameterTypes[i])) { // 判断参数类型是否相等
                                b = false;
                                break;
                            }
                        }
                        if (b) {
                            return method;
                        }
                    }
                }
            }
            return null;
        }
    }

    /**
     * 判断 java 类中是否有指定方法
     *
     * @param cls            类信息
     * @param name           方法名
     * @param parameterTypes 方法参数
     * @return
     */
    public static boolean containsMethod(Class<?> cls, String name, Class<?>... parameterTypes) {
        try {
            Class<?>[] parameter = (parameterTypes == null) ? ((Class<?>[]) null) : parameterTypes;
            return cls.getMethod(name, parameter) != null;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * 读取并解析 java.class.path 类路径参数 <br>
     * <br>
     * /Users/etl/git/repository-atom/atom/target/classes <br>
     * /Users/etl/git/repository-atom/atom/lib/db2java.jar <br>
     * /Users/etl/git/repository-atom/atom/lib/db2jcc_license_cisuz.jar <br>
     * /Users/etl/git/repository-atom/atom/lib/db2jcc_license_cu.jar <br>
     * /Users/etl/git/repository-atom/atom/lib/db2jcc.jar <br>
     * /Users/etl/.m2/repository/javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar
     *
     * @return 类路径数组（数组中没有空值）
     */
    public static String[] getJavaClassPath() {
        String delimiter = System.getProperty("path.separator"); // 路径分隔符
        String classpath = System.getProperty("java.class.path");
        String[] array = StringUtils.removeBlank(StringUtils.split(classpath, delimiter));
        for (int i = 0; i < array.length; i++) {
            array[i] = StringUtils.decodeJvmUtf8HexString(array[i]);
        }
        return array;
    }

    /**
     * 返回 JAVA 类信息所在的 classpath 路径，如果类信息在 jar 文件中则返回 jar 文件绝对路径。
     *
     * @param cls 类信息
     * @return classpath目录绝对路径或jar文件绝对路径
     */
    public static String getClasspath(Class<?> cls) {
        if (cls == null) {
            throw new NullPointerException();
        }

        // 优先检查用户自定义的 CLASSPATH
        if (ClassUtils.CLASSPATH != null) {
            if (log.isLoggable(Level.CONFIG)) {
                log.log(Level.CONFIG, ResourcesUtils.getCommonMessage(8, cls.getName(), ClassUtils.CLASSPATH));
            }

            if (ClassUtils.isClasspath0(ClassUtils.CLASSPATH, cls)) {
                return ClassUtils.CLASSPATH;
            }
        }

        // 查询根路径下的 CLASSPATH, 使用场景如: WebContainer
        String classpath0 = StringUtils.decodeJvmUtf8HexString(cls.getResource("/").getFile());
        if (classpath0 != null) {
            if (log.isLoggable(Level.CONFIG)) {
                log.log(Level.CONFIG, ResourcesUtils.getCommonMessage(9, cls.getName(), classpath0));
            }

            if (ClassUtils.isClasspath0(classpath0, cls)) {
                return ClassUtils.getClasspath(classpath0);
            }
        }

        // 查询类信息当前路径下的 CLASSPATH, 使用场景如: WebSphere
        String classpath1 = StringUtils.decodeJvmUtf8HexString(cls.getResource("").getPath());
        if (classpath1 != null) {
            if (log.isLoggable(Level.CONFIG)) {
                log.log(Level.CONFIG, ResourcesUtils.getCommonMessage(10, cls.getName(), classpath1));
            }

            if (ClassUtils.isClasspath1(classpath1)) {
                // 截取 classpath 中右侧的JAVA包文件路径, 如: D:\...\classes\cn\com\baidu\webs （删右侧的 cn\com\baidu\webs 得到 classpath 路径）
                String classPackName = cls.getPackage().getName().replace('.', File.separatorChar);
                classpath1 = FileUtils.replaceFolderSeparator(StringUtils.rtrim(classpath1, '/', '\\'));
                if (classpath1.endsWith(classPackName)) {
                    classpath1 = classpath1.substring(0, classpath1.length() - classPackName.length());
                }
                return ClassUtils.getClasspath(classpath1);
            }
        }

        // 从环境变量 CLASSPATH 中读取类所在类目录
        List<String> classpaths = new ArrayList<String>();
        String[] array = ClassUtils.getJavaClassPath();
        for (int i = 0; i < array.length; i++) {
            File file = new File(array[i]);
            if (file.exists() && file.isFile() && "jar".equalsIgnoreCase(FileUtils.getFilenameExt(file.getName()))) {
                // 忽略 jar 文件
                continue;
            } else if (file.exists() && file.isDirectory()) {
                classpaths.add(file.getAbsolutePath());
                String classPackageName = cls.getPackage().getName().replace('.', File.separatorChar);
                String classfilepath = FileUtils.joinFilepath(file.getAbsolutePath(), classPackageName);
                if (log.isLoggable(Level.CONFIG)) {
                    log.log(Level.CONFIG, ResourcesUtils.getCommonMessage(12, cls.getName(), classfilepath));
                }

                if (ClassUtils.isClasspath1(classfilepath)) {
                    return file.getAbsolutePath();
                }
            } else {
                if (log.isLoggable(Level.WARNING)) {
                    log.log(Level.WARNING, ResourcesUtils.getCommonMessage(13, cls.getName(), file.getAbsolutePath())); // 类路径不合法
                }
            }
        }

        // 查询类信息所在 jar 文件的绝对路径
        String jarfilepath = ClassUtils.getJarPath(cls);
        if (jarfilepath != null) {
            if (log.isLoggable(Level.CONFIG)) {
                log.log(Level.CONFIG, ResourcesUtils.getCommonMessage(11, cls.getName(), jarfilepath)); // 类路径不合法
            }

            return new File(jarfilepath).getAbsolutePath();
        }

        // 默认从环境变量中选一个类路径目录作为返回值
        if (classpaths.isEmpty()) {
            return null;
        } else {
            Comparator<String> c = new Comparator<String>() {
                public int compare(String o1, String o2) {
                    String[] a1 = StringUtils.split(o1, File.separatorChar);
                    String[] a2 = StringUtils.split(o2, File.separatorChar);

                    // 路径中存在 classes 或 bin 优先级越高
                    if (java.util.Arrays.binarySearch(a1, "classes") != -1 || java.util.Arrays.binarySearch(a1, "bin") != -1) {
                        return 1;
                    } else if (java.util.Arrays.binarySearch(a2, "classes") != -1 || java.util.Arrays.binarySearch(a2, "bin") != -1) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };

            Collections.sort(classpaths, c);
            return classpaths.get(classpaths.size() - 1); // 默认返回环境变量 CLASSPATH 中最后一个类目录
        }
    }

    /**
     * 判断类路径下是否存在 WEB-INF/classes 目录
     *
     * @param classpath 类路径
     * @return
     */
    private static String getClasspath(String classpath) {
        String prefix = "WEB-INF";
        int index = -1;
        if ((index = classpath.indexOf(prefix)) != -1) {
            String webinf = classpath.substring(0, index + prefix.length()); // /opt/IBM/.../WEB-INF
            String classes = FileUtils.joinFilepath(webinf, "classes");
            File file = new File(classes);
            if (file.exists()) {
                return classes;
            }

            try {
                return new URL(classes).getPath();
            } catch (MalformedURLException e) {
                return classpath;
            }
        } else {
            return new File(classpath).getAbsolutePath();
        }
    }

    /**
     * 校验 classpath 是否正确
     *
     * @param classpath 类路径
     * @param cls       类信息
     * @return
     */
    private static boolean isClasspath0(String classpath, Class<?> cls) {
        if (classpath == null || classpath.length() == 0 || StringUtils.inArray(classpath, "/", "\\")) {
            return false;
        } else {
            String className = cls.getName().replace('.', File.separatorChar);
            String classfilepath = FileUtils.joinFilepath(classpath, className) + ".class";
            return FileUtils.isFile(classfilepath);
        }
    }

    /**
     * 校验 classpath 是否正确
     *
     * @param classpath 类路径
     * @return
     */
    private static boolean isClasspath1(String classpath) {
        return classpath != null //
                && classpath.length() > 0 //
                && !StringUtils.inArray(classpath, "/", "\\") //
                && new File(classpath).exists() //
                ;
    }

    /**
     * 返回类文件（*.class）所在jar文件的绝对路径, 如果是多层jar包嵌套，则返回第一层jar所在路径 <br>
     *
     * @param cls 类信息
     * @return 如果类信息参数不在 jar 包中时返回 null
     */
    public static String getJarPath(Class<?> cls) {
        if (cls == null) {
            return null;
        }

        ProtectionDomain domain = cls.getProtectionDomain();
        if (domain == null) {
            throw new RuntimeException(cls.getName());
        }

        CodeSource codeSource = domain.getCodeSource();
        if (codeSource == null) {
            throw new RuntimeException(cls.getName());
        }

        URL url = codeSource.getLocation();
        if (url == null) {
            throw new RuntimeException(cls.getName());
        }

        String filepath = StringUtils.decodeJvmUtf8HexString(url.getFile()); // 解压文件路径中的非ascii字符
        if (filepath == null) {
            return null;
        }

        int index = StringUtils.indexOf(filepath, ".jar", 0, true);
        if (index == -1) {
            return null;
        } else {
            return filepath.substring(0, index + 4);
        }
    }

    /**
     * 查找资源
     *
     * @param name  给定资源名称, <br>
     *              例如: /jdbc.properties <br>
     *              /images/show.gif <br>
     * @param array 参数对象数组，最多只能设置一个元素
     * @return
     */
    public static InputStream getResourceAsStream(String name, Object... array) {
        Object obj = Ensure.onlyone(array);
        if (obj == null) {
            obj = new ClassUtils();
        }

        InputStream in = ClassUtils.getInputStream(obj, name);
        if (in == null) {
            Class<? extends Object> cls = (obj instanceof Class) ? ((Class<?>) obj) : obj.getClass();
            String classpath = ClassUtils.getClasspath(cls);
            String filepath = StringUtils.decodeJvmUtf8HexString(classpath);
            int index = StringUtils.indexOf(filepath, ".jar", 0, true);
            if (index != -1) {
                String str = StringUtils.replaceAll(filepath.substring(index + 4), "!/", "/");
                String uri = NetUtils.joinUri(str, name);
                return ClassUtils.getInputStream(obj, uri);
            } else {
                String all = "";
                String[] parts = StringUtils.split(filepath, "/");
                for (int i = parts.length - 1; i >= 0; i--) {
                    String prefix = NetUtils.joinUri(parts[i], all);
                    String uri = NetUtils.joinUri("", prefix, name);
                    in = ClassUtils.getInputStream(obj, uri);
                    if (in == null) {
                        all = prefix;
                    } else {
                        return in;
                    }
                }
                return null;
            }
        } else {
            return in;
        }
    }

    /**
     * 将资源通过输入流返回
     *
     * @param obj  类信息或对象信息
     * @param name 资源定位符
     * @return
     */
    private static InputStream getInputStream(Object obj, String name) {
        if (obj instanceof Class) {
            return ((Class<?>) obj).getResourceAsStream(name);
        } else {
            return obj.getClass().getResourceAsStream(name);
        }
    }

    /**
     * 返回 Class 信息的部分包名
     *
     * @param cls   类信息
     * @param level 显示包名的级别，从1（表示包的根名）开始
     *              比如类信息是 java.lang.String，level参数值是2，返回值就是 java.lang
     * @return
     */
    public static String getPackageName(Class<?> cls, int level) {
        if (level <= 0) {
            throw new IllegalArgumentException(String.valueOf(level));
        }

        if (cls == null) {
            return null;
        } else {
            String packageName = cls.getPackage().getName();
            String[] array = StringUtils.split(packageName, '.');
            StringBuilder buf = new StringBuilder(packageName.length());
            for (int i = 0, length = Math.min(level, array.length); i < length; ) {
                buf.append(array[i]);
                if (++i < length) {
                    buf.append('.');
                }
            }
            return buf.toString();
        }
    }

    /**
     * 判断字符串参数className对应的Java类是否存在
     *
     * @param className java类全名
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E> Class<E> forName(String className) {
        try {
            return (Class<E>) Class.forName(className);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 判断字符串参数 className 对应的Java类是否存在 <br>
     * 不存在时会返回 null
     *
     * @param <E>        类信息
     * @param className  类名
     * @param initialize 是否初始化
     * @param loader     类加载器
     * @return 类信息
     */
    @SuppressWarnings("unchecked")
    public static <E> Class<E> forName(String className, boolean initialize, ClassLoader loader) {
        try {
            return (Class<E>) Class.forName(className, initialize, loader);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 判断字符串参数className对应的Java类是否存在
     *
     * @param className java类全名
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E> Class<E> loadClass(String className) {
        try {
            return (Class<E>) Class.forName(className);
        } catch (Throwable e) {
            throw new RuntimeException(className, e);
        }
    }

    /**
     * 生成一个类的实例对象
     *
     * @param obj 类名
     * @param <E> 类信息
     * @return 实例对象
     */
    public static <E> E newInstance(Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }

        Class<?> cls = null;
        if (obj instanceof String) {
            String classname = (String) obj;
            cls = forName(classname);
            if (cls == null) {
                throw new IllegalArgumentException(ResourcesUtils.getClassMessage(12, classname));
            }
        } else if (obj instanceof Class) {
            cls = (Class) obj;
        } else {
            throw new UnsupportedOperationException(obj.getClass().getName());
        }

        try {
            return (E) cls.newInstance();
        } catch (Throwable e) {
            throw new IllegalArgumentException(ResourcesUtils.getClassMessage(12, cls.getName()), e);
        }
    }

    /**
     * 返回默认的 ClassLoader 对象
     *
     * @return 类加载器
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader loader = null;
        try {
            loader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable e) {
        }

        if (loader == null) {
            loader = ClassUtils.class.getClassLoader();
        }

        if (loader == null) {
            try {
                loader = ClassLoader.getSystemClassLoader();
            } catch (Throwable e) {
            }
        }
        return loader;
    }

    /**
     * 将对象中的 get 方法和 to 方法名和返回值转为字符串表格，用于调试打印对象内容
     *
     * @param obj          对象
     * @param deep         true 表示打印输出对象中的方法; false表示不打印输出对象的方法
     * @param ignoreCase   true表示忽略英文字母大小写
     * @param methodPrefix 方法名前缀数组
     * @return
     */
    public static String toString(Object obj, boolean deep, boolean ignoreCase, String... methodPrefix) {
        if (obj == null) {
            return "";
        }

        String prefix = "obj.";
        CharTable table = new CharTable();
        table.addTitle(CharTable.ALIGN_LEFT, "FUNCTION_NAME");
        table.addTitle(CharTable.ALIGN_LEFT, "RETURN");

        ArrayList<String> list = ArrayUtils.asList(methodPrefix);
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            if (Modifier.isAbstract(method.getModifiers())) { // 过滤抽象方法
                continue;
            }

            String functionName = method.getName();
            if (StringUtils.inArray(functionName, "getClass", "hashCode", "clone", "toString")) {
                continue;
            }

            Type[] types = method.getGenericParameterTypes();
            if (types == null || types.length == 0) {
                Class<?> cls = method.getReturnType();
                if (cls.equals(Void.class)) {
                    continue;
                }

                if (StringUtils.startsWith(functionName, list, ignoreCase)) {
                    try {
                        Object value = method.invoke(obj);
                        table.addValue(prefix + functionName);

                        if (value == null || value.getClass().getName().startsWith("java.") || !deep) {
                            table.addValue(value == null ? "" : StringUtils.toString(value));
                        } else {
                            table.addValue(toString(value, false, ignoreCase, methodPrefix));
                        }
                    } catch (Throwable e) {
                        table.addValue(prefix + functionName);
                        table.addValue(StringUtils.toString(e));
                    }
                }
            }
        }

        table.removeLeftBlank();
        return table.toStandardShape();
    }

    /**
     * 将Class数组转为类名集合
     *
     * @param array Class类信息
     * @return 类名集合
     */
    public static List<String> asNameList(Class<?>[] array) {
        if (array == null) {
            return new ArrayList<String>(0);
        }

        List<String> list = new ArrayList<String>(array.length);
        for (Class<?> cls : array) {
            list.add(cls.getName());
        }
        return list;
    }

    /**
     * 查询 JNDI 资源
     *
     * @param <E>
     * @param jndiName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E> E lookup(String jndiName) {
        try {
            Context context = null;
            if (StringUtils.startsWith(jndiName, "java:", 0, true, true)) {
                context = new InitialContext();
            } else {
                context = (Context) new InitialContext().lookup("java:comp/env");
            }
            return (E) context.lookup(jndiName);
        } catch (Throwable e) {
            throw new RuntimeException(jndiName, e);
        }
    }

}