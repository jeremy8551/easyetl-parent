package icu.etl.ioc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import icu.etl.log.ConsoleLoggerBuilder;
import icu.etl.log.DefaultLoggerBuilder;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.log.Slf4jLoggerBuilder;
import icu.etl.util.ClassUtils;
import icu.etl.util.Settings;
import icu.etl.util.StringUtils;

/**
 * 组件上下文信息装载器
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/5
 */
public class EasyContextInit {

    /**
     * 扫描参数
     * <p>
     * 例如: java.lang.String,org.test,!org.test.String
     * <p>
     * 在包名前面使用符号 ! 表示扫描类时会排除掉这个包名下的所有类
     */
    private String[] packageArgs;

    private ClassLoader classLoader;

    private String[] args;

    /**
     * 初始化
     */
    public EasyContextInit(ClassLoader classLoader, String[] args) {
        this.classLoader = (classLoader == null) ? ClassUtils.getDefaultClassLoader() : classLoader;
        this.args = args;
        this.initLog(args);
    }

    /**
     * 解析参数，首先初始化日志参数
     *
     * @param args 外部输入的参数数组
     */
    private void initLog(String[] args) {
        List<String> list = new ArrayList<String>(args.length + 10);
        for (String str : args) {
            String[] array = StringUtils.removeBlank(StringUtils.split(str, ','));
            for (String element : array) {
                if (element.indexOf(':') != -1) {
                    String[] logarr = StringUtils.split(element, ':');
                    if (logarr.length > 2) {
                        throw new IllegalArgumentException(element);
                    }

                    // 使用控制台输出
                    if (StringUtils.inArrayIgnoreCase("sout", logarr)) {
                        LogFactory.setbuilder(new ConsoleLoggerBuilder());
                    }

                    // 使用slf4j输出
                    if (StringUtils.inArrayIgnoreCase("slf4j", logarr)) {
                        LogFactory.setbuilder(new Slf4jLoggerBuilder());
                    }

                    // 使用标准输出
                    if (StringUtils.inArrayIgnoreCase("default", logarr)) {
                        LogFactory.setbuilder(new DefaultLoggerBuilder());
                    }

                    if (StringUtils.inArrayIgnoreCase(logarr[0], Log.LEVEL)) {
                        System.setProperty(Log.PROPERTY_LOGGER, logarr[0]);
                    }

                    if (StringUtils.inArrayIgnoreCase(logarr[1], Log.LEVEL)) {
                        System.setProperty(Log.PROPERTY_LOGGER, logarr[1]);
                    }
                } else {
                    list.add(element);
                }
            }
        }

        this.packageArgs = new String[list.size()];
        list.toArray(this.packageArgs);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public String[] getArgument() {
        String[] array = new String[this.args.length];
        System.arraycopy(this.args, 0, array, 0, this.args.length);
        return array;
    }

    public void setArgument(String... args) {
        this.args = args;
        this.initLog(args);
    }

    /**
     * 扫描指定包下的类信息
     *
     * @param context 上下文信息
     */
    public void scann(BeanRegister context) {
        if (packageArgs.length == 0) { // 取默认值
            String value = System.getProperty(ClassScanner.PROPERTY_SCANNPKG, "");
            packageArgs = StringUtils.removeBlank(StringUtils.split(value, ','));
        }

        List<String> includePackageNames = new ArrayList<String>(packageArgs.length);
        List<String> excludePackageNames = new ArrayList<String>(packageArgs.length);

        char[] prefix = new char[]{'^', '!', '！', StringUtils.toFullWidthChar('^')};
        List<String> list = Arrays.asList(String.valueOf(prefix[0]), String.valueOf(prefix[1]), String.valueOf(prefix[2]), String.valueOf(prefix[3]));

        for (String str : packageArgs) {
            String[] array = StringUtils.split(str, ',');
            for (String part : array) {
                String packageName = StringUtils.rtrimBlank(StringUtils.ltrimBlank(part, prefix), '.'); // 包名
                if (StringUtils.isNotBlank(packageName)) {
                    if (StringUtils.startsWith(part, list, 0, false, true)) {
                        excludePackageNames.add(packageName);
                    } else {
                        includePackageNames.add(part);
                    }
                }
            }
        }

        this.scann(context, includePackageNames, excludePackageNames);
    }

    /**
     * 扫描指定包下的类信息
     *
     * @param context             上下文信息
     * @param includePackageNames 需要扫描的JAVA包名: java.lang, 为null或空白字符串时，表示扫描所有JAVA包下的类信息
     * @param excludepackageNames 扫描时需要排除的包名
     */
    private void scann(BeanRegister context, List<String> includePackageNames, List<String> excludepackageNames) {
        String groupId = Settings.getGroupID();
        includePackageNames.remove(groupId);
        includePackageNames.add(0, groupId); // 保证首先扫描本工程中的包

        // 加载类扫描规则集合
        List<ClassScanRule> processors = new ArrayList<ClassScanRule>();
        ServiceLoader<ClassScanRule> serviceLoader = ServiceLoader.load(ClassScanRule.class, this.getClassLoader());
        for (ClassScanRule rule : serviceLoader) {
            processors.add(rule);
        }

        ClassScanner scanner = new ClassScanner(this.getClassLoader(), includePackageNames, excludepackageNames, processors);
        scanner.load(context);
    }
}
