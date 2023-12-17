package icu.etl.ioc.scan;

import java.io.File;
import java.io.FileOutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import icu.etl.ProjectPom;
import icu.etl.ioc.EasyBeanRegister;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.util.ClassUtils;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 类扫描器
 *
 * @author jeremy8551@qq.com
 */
public class ClassScanner {
    private final static Log log = LogFactory.getLog(ClassScanner.class);

    /** 扫描的 JAVA 包名与 jar 文件名 */
    public final static String PROPERTY_SCANNPKG = ProjectPom.getGroupID() + ".scanRule";

    /** 类加载器 */
    private ClassLoader classLoader;

    /** 需要扫描的JAVA包名 */
    private LinkedHashSet<String> includePackageNames;

    /** 扫描时，需要排除的包名 */
    private HashSet<String> excludePackageNames;

    /** 过滤器数组 */
    private List<ClassScanRule> rules;

    /** 防止重复扫描相同 jar 文件建立的集合 */
    private HashSet<File> jarfiles;

    /** 注册组件 */
    private EasyBeanRegister register;

    /**
     * 扫描所有类信息
     */
    protected ClassScanner() {
        this.includePackageNames = new LinkedHashSet<String>();
        this.excludePackageNames = new HashSet<String>();
        this.jarfiles = new HashSet<File>();
        this.rules = new ArrayList<ClassScanRule>();
    }

    /**
     * 扫描指定JAVA包名下满足过滤条件的类信息
     *
     * @param loader              类加载器, 为 null 时使用默认类加载器
     * @param includePackageNames 需要扫描的JAVA包名: java.lang, 为null或空白字符串时，表示扫描所有JAVA包下的类信息
     * @param excludepackageNames 扫描时需要排除的包名
     * @param rules               类过滤器集合
     */
    public ClassScanner(ClassLoader loader, List<String> includePackageNames, List<String> excludepackageNames, List<ClassScanRule> rules) {
        this();

        if (loader == null) {
            this.classLoader = ClassUtils.getDefaultClassLoader();
        } else {
            this.classLoader = loader;
        }

        if (includePackageNames == null || includePackageNames.isEmpty()) {
            throw new IllegalArgumentException(StringUtils.toString(includePackageNames));
        } else {
            this.includePackageNames.addAll(includePackageNames);
        }

        if (excludepackageNames != null) {
            this.excludePackageNames.addAll(excludepackageNames);
        }

        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException(StringUtils.toString(rules));
        } else {
            this.rules.clear();
            this.rules.addAll(rules);
        }
    }

    /**
     * 扫描类
     *
     * @param register 组件注册接口
     * @return 返回已加载类信息的个数
     */
    public synchronized int load(EasyBeanRegister register) {
        this.register = Ensure.notNull(register);
        int count = 0;
        this.jarfiles.clear();
        try {
            for (String packageName : this.includePackageNames) {
                count += this.scanPackage(this.classLoader, packageName);
            }
        } finally {
            this.jarfiles.clear();
        }

        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getIocMessage(6, this.includePackageNames.isEmpty() ? "" : " " + this.includePackageNames + " ", count));
        }
        return count;
    }

    /**
     * 使用 ClassLoader 扫描包名下的所有类信息
     *
     * @param classLoader 类加载器
     * @param packageName java包名
     * @return 扫描到复合条件类的总数
     */
    protected int scanPackage(ClassLoader classLoader, String packageName) {
        String uri = packageName.replace(".", "/"); // 格式: xxx/xxx
        if (StringUtils.isNotBlank(uri) && !uri.endsWith("/")) {
            uri += "/"; // 右端需要有分隔符
        }

        Enumeration<URL> enu = null;
        try {
            enu = classLoader.getResources(uri);
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getClassMessage(7, uri), e);
            }
            return 0;
        }

        int count = 0;
        while (enu.hasMoreElements()) {
            URL url = enu.nextElement();
            if (url == null) {
                continue;
            }

            String urlpath = StringUtils.decodeJvmUtf8HexString(url.getPath());
            String lowderPath = urlpath.toLowerCase();
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getClassMessage(1, urlpath, packageName));
            }

            // 加载类文件
            String protocol = url.getProtocol();
            if (protocol.toLowerCase().contains("file")) {
                try {
                    count += this.scanPackage(classLoader, packageName, urlpath);
                } catch (Throwable e) {
                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getClassMessage(10, packageName, urlpath), e);
                    }
                }
            }

            // 加载 jar 文件
            else if (protocol.toLowerCase().contains("jar") //
                    || lowderPath.endsWith(".jar") //
                    || lowderPath.contains(".jar!") //
                    || lowderPath.contains(".jar/") //
                    || lowderPath.endsWith(".jar/") //
            ) {
                JarFile jarfile = this.loadJarFile(url);
                if (jarfile == null) {
                    continue;
                } else {
                    count += this.scanJarfile(classLoader, jarfile, jarfile.getName());
                }
            }

            // 不能识别的资源信息
            else {
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getClassMessage(17, protocol, urlpath));
                }
            }

            if (log.isDebugEnabled()) {
                log.debug(""); // 添加一个换行，区分多个类路径的扫描日志
            }
        }

        return count;
    }

    /**
     * 扫描指定包名下的class文件，是否是目标类的子类
     *
     * @param loader      类加载器
     * @param packageName 限制扫描范围的JAVA包名
     * @param packagePath 包名所在路径
     * @return 加载类的个数
     */
    private int scanPackage(ClassLoader loader, String packageName, String packagePath) {
        if (this.isExclude(packageName, true)) {
            return 0;
        }

        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getClassMessage(6, packageName.length() == 0 ? "" : packageName, packagePath));
        }

        int count = 0;
        File packagefile = new File(packagePath);
        if (!packagefile.exists()) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getClassMessage(27, packageName, packagePath));
            }
            return count;
        }

        File[] files;
        if (packagefile.isDirectory()) {
            files = packagefile.listFiles();
            if (files == null) {
                files = new File[0];
            }
        } else {
            files = new File[]{packagefile};
        }

        for (File file : files) {
            String filename = file.getName();
            String filepath = StringUtils.decodeJvmUtf8HexString(file.getAbsolutePath());
            String lowderpath = filepath.toLowerCase();
            String extname = FileUtils.getFilenameExt(filename).toLowerCase();

            if (log.isTraceEnabled()) {
                if (file.isDirectory()) {
                    log.trace(ResourcesUtils.getClassMessage(24, packageName, filename, FileUtils.joinPath(StringUtils.decodeJvmUtf8HexString(packagefile.getAbsolutePath()), filename)));
//                } else { 与下面日志重复，需要注释掉
//                    log.trace(ResourcesUtils.getClassMessage(7, StringUtils.decodeJvmUtf8HexString(packagefile.getAbsolutePath()), filename, packageName));
                }
            }

            // scan jar file
            if (lowderpath.endsWith(".jar") //
                    || lowderpath.contains(".jar!") //
                    || lowderpath.contains(".jar/") //
                    || lowderpath.endsWith(".jar/") //
            ) { // 判断文件是否是 jar 包
                JarFile jarfile = this.loadJarFile(file);
                if (jarfile == null) {
                    continue;
                } else {
                    count += this.scanJarfile(loader, jarfile, jarfile.getName());
                }
            }

            // scan class file
            else if (file.isFile()) {
                if ("class".equalsIgnoreCase(extname)) {
                    String className = filename.substring(0, filename.lastIndexOf("."));
                    if (StringUtils.isNotBlank(packageName)) {
                        className = packageName + "." + className;
                    }

                    if (this.notInclude(className) || this.isExclude(className, false)) {
                        continue;
                    }

                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getClassMessage(4, className, filepath));
                    }

                    Class<?> cls = this.forName(className, false, loader);
                    if (this.load(cls)) {
                        count++;
                    }
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace(ResourcesUtils.getClassMessage(8, file.getAbsolutePath()));
                    }
                }
            }

            // directory or jar
            else {
                String subPackagePath = filename;
                if (StringUtils.isNotBlank(packagePath)) {
                    subPackagePath = StringUtils.rtrimBlank(packagePath, '/') + "/" + StringUtils.ltrimBlank(subPackagePath, '/');
                }

                String subPackageName = filename;
                if (StringUtils.isNotBlank(packageName)) {
                    subPackageName = packageName + "." + subPackageName;
                }

                count += this.scanPackage(loader, subPackageName, subPackagePath);
            }
        }

        return count;
    }

    /**
     * 加载类
     *
     * @param className  类名
     * @param initialize 是否初始化
     * @param loader     类加载器
     * @return 类信息
     */
    protected Class<?> forName(String className, boolean initialize, ClassLoader loader) {
        try {
            return Class.forName(className, initialize, loader);
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getClassMessage(5, className), e);
            }
            return null;
        }
    }

    /**
     * 加载 jar 文件
     *
     * @param obj 资源对象
     * @return jar文件
     */
    private JarFile loadJarFile(Object obj) {
        // url 路径
        if (obj instanceof URL) {
            URL url = (URL) obj;
            String urlpath = StringUtils.decodeJvmUtf8HexString(url.getPath());

            JarFile jarfile = null;
            try {
                JarURLConnection conn = (JarURLConnection) url.openConnection();
                jarfile = conn.getJarFile();
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getClassMessage(28, urlpath));
                }
                return null;
            }

            File file = new File(StringUtils.decodeJvmUtf8HexString(jarfile.getName()));
            if (this.jarfiles.contains(file)) { // 判断是否已扫描
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getClassMessage(16, jarfile.getName()));
                }
                return null;
            } else {
                this.jarfiles.add(file);
                return jarfile;
            }
        }

        // jar文件
        else if (obj instanceof File) {
            File file = (File) obj;
            if (this.jarfiles.contains(file)) { // 判断是否已扫描jar
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getClassMessage(16, file));
                }
                return null;
            }

            try {
                JarFile jarfile = new JarFile(file);
                this.jarfiles.add(file);
                return jarfile;
            } catch (Throwable e) {
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getClassMessage(29, file.getAbsolutePath()));
                }
                return null;
            }
        }

        throw new UnsupportedOperationException(StringUtils.toString(obj));
    }

    /**
     * 扫描 jar 文件中的 class 文件
     *
     * @param loader  类加载器
     * @param jarfile jar文件
     * @param name    jar文件描述信息
     * @return 返回加载类的个数
     */
    private int scanJarfile(ClassLoader loader, JarFile jarfile, String name) {
        int count = 0;
        if (jarfile == null) {
            return count;
        }

        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getClassMessage(3, StringUtils.decodeJvmUtf8HexString(name)));
        }

        // 读取类路径的上级目录
        Set<String> prefixs = new HashSet<String>();
        try {
            Manifest mf = jarfile.getManifest();
            if (mf != null) {
                Attributes mainAttributes = mf.getMainAttributes();
                Set<Entry<Object, Object>> entrySet = mainAttributes.entrySet();
                if (!entrySet.isEmpty()) {
                    if (log.isTraceEnabled()) {
                        log.trace(jarfile.getName() + "!/META-INF/MANIFEST.MF");
                    }

                    for (Entry<Object, Object> entry : entrySet) {
                        String value = StringUtils.trimBlank(entry.getValue(), '/', '\\');
                        if (StringUtils.isNotBlank(value) && (value.indexOf('/') != -1 || value.indexOf('\\') != -1)) {
                            String classpathPrefix = value.replace('/', '.').replace('\\', '.') + ".";
                            prefixs.add(classpathPrefix);

                            if (log.isTraceEnabled()) {
                                log.trace(entry.getKey() + " = " + value + " *");
                            }
                        } else {
                            if (log.isTraceEnabled()) {
                                log.trace(entry.getKey() + " = " + value);
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug(jarfile.getName() + "!/META-INF/MANIFEST.MF", e);
            }
        }

        Enumeration<JarEntry> it = jarfile.entries();
        while (it.hasMoreElements()) {
            JarEntry entry = it.nextElement();
            String filename = entry.getName();
            String extname = FileUtils.getFilenameExt(filename);

            // 扫描类文件
            if ("class".equalsIgnoreCase(extname)) {
                String className = filename.substring(0, filename.lastIndexOf(".")).replace("/", ".").replace('\\', '.');

                for (String classpathPrefix : prefixs) {
                    if (className.startsWith(classpathPrefix)) {
                        className = className.substring(classpathPrefix.length()); // 删除前缀 BOOT-INF/classes
                    }
                }

                if (this.notInclude(className) || this.isExclude(className, false)) {
                    continue;
                }

                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getClassMessage(4, className));
                }

                Class<?> cls = this.forName(className, false, loader);
                if (this.load(cls)) {
                    count++;
                }
            }

            // 扫描 jar 中嵌套的 jar 文件
            else if ("jar".equalsIgnoreCase(extname) && !entry.isDirectory()) {
                File tempDir = FileUtils.getTempDir(ClassScanner.class.getSimpleName(), "jar");
                File unzipJarfile = FileUtils.allocate(tempDir, filename); // 解压后的jar文件

                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getClassMessage(14, jarfile.getName(), filename, unzipJarfile.getParentFile()));
                }

                FileUtils.assertCreateFile(unzipJarfile);
                JarFile newJarfile = null;
                try {
                    IO.write(jarfile.getInputStream(entry), new FileOutputStream(unzipJarfile)); // 解压jar包中的jar文件
                    newJarfile = new JarFile(unzipJarfile);
                } catch (Throwable e) {
                    if (log.isDebugEnabled()) {
                        log.debug(ResourcesUtils.getClassMessage(30, jarfile.getName(), entry.getName()), e);
                    }
                    continue;
                }

                // 扫描解压后的jar文件
                count += this.scanJarfile(loader, newJarfile, jarfile.getName() + "!/" + filename); // 扫描 jar 文件
            }
        }
        return count;
    }

    /**
     * 使用过滤器对类信息进行筛选
     *
     * @param cls 类信息
     * @return true表示加载 false表示过滤
     */
    public boolean load(Class<?> cls) {
        if (cls == null) {
            return false;
        }

        boolean load = false;
        for (ClassScanRule rule : this.rules) {
            if (rule != null && rule.process(cls, this.register)) {
                load = true;
            }
        }
        return load;
    }

    /**
     * 判断是一个类名是否满足扫描规则
     *
     * @param className 类名
     * @return 返回true表示类不符合扫描规则
     */
    private boolean notInclude(String className) {
        for (String name : this.includePackageNames) {
            if (className.startsWith(name)) {
                return false;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getClassMessage(22, className, this.includePackageNames.toString()));
        }
        return true;
    }

    /**
     * 判断类名是否匹配
     *
     * @param className 类名
     * @param pkgOrCls  true表示包名 false表示类名
     * @return 返回true表示不需要扫描类
     */
    protected boolean isExclude(String className, boolean pkgOrCls) {
        for (String name : this.excludePackageNames) {
            if (className.startsWith(name)) {
                if (log.isDebugEnabled()) {
                    if (pkgOrCls) {
                        log.debug(ResourcesUtils.getClassMessage(25, className, name));
                    } else {
                        log.debug(ResourcesUtils.getClassMessage(23, className, name));
                    }
                }
                return true;
            }
        }
        return false;
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
     * 需要扫描的包名前缀集合
     *
     * @return 包名前缀集合
     */
    public Set<String> getIncludePackageNames() {
        return Collections.unmodifiableSet(this.includePackageNames);
    }

    /**
     * 需要排除的包名前缀
     *
     * @return 包名前缀集合
     */
    public Set<String> getExcludePackageNames() {
        return Collections.unmodifiableSet(this.excludePackageNames);
    }

    /**
     * 返回类过滤器集合
     *
     * @return 类过滤器集合
     */
    public List<ClassScanRule> getRules() {
        return Collections.unmodifiableList(this.rules);
    }

}
