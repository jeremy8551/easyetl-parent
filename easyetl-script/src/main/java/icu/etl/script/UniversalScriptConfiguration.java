package icu.etl.script;

import java.util.Set;

/**
 * 脚本引擎配置信息接口
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-09
 */
public interface UniversalScriptConfiguration {

    /**
     * 查询脚本引擎属性
     *
     * @param name 属性名
     * @return 属性值
     */
    String getProperty(String name);

    /**
     * 返回脚本引擎的关键字
     */
    Set<String> getKeywords();

    /**
     * 返回脚本引擎默认的命令
     *
     * @return 默认的命令
     */
    String getDefaultCommand();

    /**
     * 返回脚本引擎支持的类型信息
     *
     * @return 脚本引擎支持的类型信息
     */
    String getMimeTypes();

    /**
     * 返回脚本引擎支持的扩展名
     *
     * @return 脚本引擎支持的扩展名
     */
    String getExtensions();

    /**
     * 返回脚本引擎支持的名字
     *
     * @return 脚本引擎支持的名字
     */
    String getNames();

    /**
     * 返回脚本引擎默认的编译器类名
     *
     * @return 脚本引擎默认的编译器类名
     */
    String getCompiler();

    /**
     * 返回脚本引擎的会话参数
     *
     * @return 脚本引擎的会话参数
     */
    String getSessionFactory();

    /**
     * 返回脚本引擎默认的类型转换器的类名
     *
     * @return 脚本引擎默认的类型转换器的类名
     */
    String getConverter();

    /**
     * 返回脚本引擎默认的校验接口实现类名
     *
     * @return 脚本引擎默认的校验接口实现类名
     */
    String getChecker();

    /**
     * 返回脚本引擎的名字
     *
     * @return 脚本引擎的名字
     */
    String getEngineName();

    /**
     * 返回脚本引擎的版本信息
     *
     * @return 脚本引擎的版本信息
     */
    String getEngineVersion();

    /**
     * 返回脚本引擎中语言名
     *
     * @return 脚本引擎中语言名
     */
    String getLanguageName();

    /**
     * 返回脚本引擎中语言版本号
     *
     * @return 脚本引擎中语言版本号
     */
    String getLanguageVersion();

}
