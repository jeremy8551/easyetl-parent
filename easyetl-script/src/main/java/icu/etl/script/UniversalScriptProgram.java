package icu.etl.script;

import java.io.IOException;

import icu.etl.script.internal.ScriptProgramClone;

/**
 * 用自定义的可扩展的程序逻辑
 *
 * @author jeremy8551@qq.com
 */
public interface UniversalScriptProgram {

    /**
     * 深度复制并返回一个程序副本，程序副本中包含一个关键字和一个程序
     *
     * @return
     */
    ScriptProgramClone deepClone();

    /**
     * 释放数据库连接，SSH连接等资源（可根据需要实现）
     *
     * @throws Exception
     */
    void close() throws Exception;

}
