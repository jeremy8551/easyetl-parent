package icu.etl.script.internal;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎命令使用说明
 *
 * @author jeremy8551@qq.com
 */
public class ScriptUsage {

    /** 使用说明 */
    private String usage;

    /**
     * 初始化
     *
     * @param cls                  类信息
     * @param descriptionParameter 使用说明参数
     */
    public ScriptUsage(Class<? extends UniversalCommandCompiler> cls, Object... descriptionParameter) {
        String usage = ScriptUsage.getUsageSuffix(cls);
        this.usage = this.parse(//
                3, //
                ResourcesUtils.getMessage("script.command." + usage + ".name"), //
                ResourcesUtils.getMessage("script.command." + usage + ".synopsis"), //
                StringUtils.split(ResourcesUtils.getMessage("script.command." + usage + ".descriptions", descriptionParameter), '\n') //
        );
    }

    /**
     * 输出命令的使用说明
     *
     * @param tabsize      伸缩空白字符的个数
     * @param name
     * @param synopsis
     * @param descriptions
     * @return
     */
    private String parse(int tabsize, String name, String synopsis, String... descriptions) {
        String[] titles = StringUtils.split(ResourcesUtils.getMessage("script.engine.usage.msg002"), ',');
        String tab = StringUtils.left('\t', tabsize, '\t');
        StringBuilder buf = new StringBuilder();
        if (StringUtils.isNotBlank(name)) {
            buf.append(titles[0]).append(FileUtils.lineSeparator);
            String[] list = StringUtils.split(name, '\n');
            for (String str : list) {
                buf.append(tab).append(str).append(FileUtils.lineSeparator);
            }
        }

        if (StringUtils.isNotBlank(synopsis)) {
            buf.append(titles[1]).append(FileUtils.lineSeparator);
            String[] list = StringUtils.split(synopsis, '\n');
            for (String str : list) {
                buf.append(tab).append(str).append(FileUtils.lineSeparator);
            }
        }

        if (descriptions.length > 0 && !StringUtils.isBlank(descriptions)) {
            buf.append(titles[2]).append(FileUtils.lineSeparator);
            for (String str : descriptions) {
                buf.append(tab).append(str).append(FileUtils.lineSeparator);
            }
        }
        return buf.toString();
    }

    /**
     * 返回命令说明编号的后缀
     *
     * @param cls
     * @return
     */
    public static String getUsageSuffix(Class<? extends UniversalCommandCompiler> cls) {
        String className = cls.getSimpleName().toLowerCase();
        return className.endsWith("commandcompiler") ? className.substring(0, className.length() - "commandcompiler".length()) : className;
    }

    public String toString() {
        return this.usage == null ? "" : this.usage;
    }

}
