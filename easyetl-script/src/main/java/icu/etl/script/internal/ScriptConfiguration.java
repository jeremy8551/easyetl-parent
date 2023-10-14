package icu.etl.script.internal;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.script.ScriptEngine;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.collection.CaseSensitivSet;
import icu.etl.script.UniversalScriptConfiguration;
import icu.etl.util.Ensure;
import icu.etl.util.StringUtils;

@EasyBeanClass(type = UniversalScriptConfiguration.class)
public class ScriptConfiguration implements UniversalScriptConfiguration {
    public final static Object[] EMPTY = new Object[0];

    /**
     * 配置信息
     */
    private ResourceBundle config;

    /**
     * 初始化
     */
    public ScriptConfiguration() {
        this.config = ResourceBundle.getBundle("script", Locale.getDefault());
    }

    /**
     * 返回脚本引擎默认命令的语句
     *
     * @return
     */
    public String getDefaultCommand() {
        return Ensure.notBlank(this.getProperty("javax.script.command.default"));
    }

    public String getMimeTypes() {
        return this.getProperty("javax.script.mimetypes");
    }

    public String getExtensions() {
        return this.getProperty("javax.script.extensions");
    }

    public String getNames() {
        return this.getProperty("javax.script.names");
    }

    public String getCompiler() {
        return this.getProperty("javax.script.compiler");
    }

    public String getSessionFactory() {
        return this.getProperty("javax.script.session");
    }

    public String getConverter() {
        return this.getProperty("javax.script.converter");
    }

    public String getChecker() {
        return this.getProperty("javax.script.checker");
    }

    public String getEngineName() {
        return this.getProperty(ScriptEngine.ENGINE);
    }

    public String getEngineVersion() {
        return this.getProperty(ScriptEngine.ENGINE_VERSION);
    }

    public String getLanguageName() {
        return this.getProperty(ScriptEngine.LANGUAGE);
    }

    public String getLanguageVersion() {
        return this.getProperty(ScriptEngine.LANGUAGE_VERSION);
    }

    public Set<String> getKeywords() {
        String keywords = this.getProperty("javax.script.keywords");
        String[] array = StringUtils.removeBlank(StringUtils.split(keywords, ','));
        Set<String> set = new CaseSensitivSet();
        for (String str : array) {
            set.add(str);
        }
        return set;
    }

    public String getProperty(String name) {
        // 优先从 JavaUtils 属性中读取参数
        String value = System.getProperty(name);
        if (StringUtils.isNotBlank(value)) { // 属性不能是空
            return value;
        }

        // 加载配置文件中的属性值
        try {
            return MessageFormat.format(this.config.getString(name), EMPTY);
        } catch (MissingResourceException e) {
            return null;
        }
    }

}
