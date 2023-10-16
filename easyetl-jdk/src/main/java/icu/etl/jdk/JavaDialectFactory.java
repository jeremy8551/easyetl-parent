package icu.etl.jdk;

/**
 * JAVA 方言工厂
 *
 * @author jeremy8551@qq.com
 */
public class JavaDialectFactory {

    /** JDK方言 */
    private static JavaDialect dialect;

    /**
     * 返回 JDK 方言对象
     *
     * @return 方言接口的实现类
     */
    public static JavaDialect getDialect() {
        if (dialect == null) {
            dialect = new JavaDialectFactory().loadDialect();
        }
        return dialect;
    }

    /**
     * 加载JDK方言接口的实现类
     *
     * @return JDK方言接口的实现类
     */
    private JavaDialect loadDialect() {
        String[] version = this.getJavaVersion();
        int major = Integer.parseInt(version[0]); // JDK大版本号，如: 5, 6, 7, 8 ..
        String packageName = JavaDialect.class.getPackage().getName(); // 方言类所在包名
        String className = packageName + ".JDK" + major;

        // 查找JDK版本号对应的方言类
        String testclassName = className;
        Class<JavaDialect> cls = null;
        while ((cls = this.forName(testclassName)) == null && major >= 0) {
            testclassName = packageName + ".JDK" + --major;
        }

        if (cls == null) {
            throw new RuntimeException(className);
        } else {
            return this.create(className, cls);
        }
    }

    /**
     * JDK 版本信息 <br>
     * System.getProperty("java.version"); <br>
     * 1.8.0 <br>
     * 16.0.1 <br>
     *
     * @return 数组第一个元素表示大版本号（如:5,6,7,8..11），第二个表示小版本号
     */
    protected String[] getJavaVersion() {
        String value = System.getProperty("java.version");
        String[] version = value.split("\\.");
        if (version.length < 3) {
            throw new UnsupportedOperationException(value);
        } else if (version[0].equals("1")) {
            return new String[]{version[1], version[2]};
        } else {
            return new String[]{version[0], version[1]};
        }
    }

    /**
     * 按类名查找类
     *
     * @param className 类名
     * @return 类信息
     */
    protected Class<JavaDialect> forName(String className) {
        try {
            return (Class<JavaDialect>) Class.forName(className);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 创建数据库方言实现类
     *
     * @param className 数据库方言实现类名
     * @param cls       数据库方言实现类
     * @return 数据库方言实现类对象
     */
    protected JavaDialect create(String className, Class<JavaDialect> cls) {
        try {
            return cls.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(className, e);
        }
    }

}
