/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package script;

import java.util.List;
import javax.script.ScriptEngine;

/**
 * 脚本引擎工厂接口
 *
 * @since 1.6
 */
public interface ScriptEngineFactory {

    public String getEngineName();

    public String getEngineVersion();

    public List<String> getExtensions();

    public List<String> getMimeTypes();

    public List<String> getNames();

    public String getLanguageName();

    public String getLanguageVersion();

    public Object getParameter(String key);

    public String getMethodCallSyntax(String obj, String m, String... args);

    public String getOutputStatement(String toDisplay);

    public String getProgram(String... statements);

    public ScriptEngine getScriptEngine();

}
