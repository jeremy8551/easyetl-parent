package icu.etl.ioc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import icu.etl.log.Log;
import icu.etl.util.Settings;
import icu.etl.util.StringUtils;

/**
 * 组件上下文信息装载器
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/5
 */
public class BeanInfoScanner {

    /**
     * 初始化
     */
    public BeanInfoScanner() {
    }

    /**
     * 扫描指定包下的类
     *
     * @param context 上下文信息
     */
    public void load(EasyetlContext context) {
        String[] args = context.getArgument();
        List<String> list = new ArrayList<String>(args.length + 10);
        for (String str : args) {
            String[] array = StringUtils.removeBlank(StringUtils.split(str, ','));
            for (String element : array) {
                if (element.indexOf(':') != -1) {
                    String[] logarr = StringUtils.split(element, ':');
                    if (logarr.length > 2) {
                        throw new IllegalArgumentException(element);
                    }

                    if (StringUtils.inArrayIgnoreCase("sout", logarr)) {
                        System.setProperty(Log.PROPERTY_LOGGERSOUT, element);
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

        String[] array = new String[list.size()];
        list.toArray(array);
        this.load(context, array);
    }

    /**
     * 扫描指定包下的类信息
     *
     * @param context      上下文信息
     * @param packageNames 包名表达式, 例如: java.lang.String,org.test,!org.test.String
     *                     在包名前面使用符号 ! 表示扫描类时会排除掉这个包名下的所有类
     */
    protected void load(EasyetlContext context, String[] packageNames) {
        if (packageNames.length == 0) { // 取默认值
            String value = System.getProperty(ClassScanner.PROPERTY_SCANNPKG, "");
            packageNames = StringUtils.removeBlank(StringUtils.split(value, ','));
        }

        List<String> includePackageNames = new ArrayList<String>(packageNames.length);
        List<String> excludePackageNames = new ArrayList<String>(packageNames.length);

        char[] prefix = new char[]{'^', '!', '！', StringUtils.toFullWidthChar('^')};
        List<String> list = Arrays.asList(String.valueOf(prefix[0]), String.valueOf(prefix[1]), String.valueOf(prefix[2]), String.valueOf(prefix[3]));

        for (String str : packageNames) {
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

        this.load(context, includePackageNames, excludePackageNames);
    }

    /**
     * 扫描指定包下的类信息
     *
     * @param context             上下文信息
     * @param includePackageNames 需要扫描的JAVA包名: java.lang, 为null或空白字符串时，表示扫描所有JAVA包下的类信息
     * @param excludepackageNames 扫描时需要排除的包名
     */
    protected void load(EasyetlContext context, List<String> includePackageNames, List<String> excludepackageNames) {
        String groupId = Settings.getGroupID();
        includePackageNames.remove(groupId);
        includePackageNames.add(0, groupId); // 保证首先扫描本工程中的包

        // 加载类扫描规则集合
        List<ClassScanRule> processors = new ArrayList<ClassScanRule>();
        ServiceLoader<ClassScanRule> sl = ServiceLoader.load(ClassScanRule.class);
        for (ClassScanRule rule : sl) {
            processors.add(rule);
        }

        ClassScanner scanner = new ClassScanner(context.getClassLoader(), includePackageNames, excludepackageNames, processors);
        scanner.load(context);
    }

}
