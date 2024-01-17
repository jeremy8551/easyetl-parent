package icu.etl.script;

/**
 * 脚本命令编译结果
 *
 * @author jeremy8551@qq.com
 */
public enum UniversalCommandCompilerResult {

    /** 0 表示可以编译脚本语句（且编译器继续向下尝试匹配其他命令） */
    NEUTRAL,

    /** 1 表示可以编译脚本语句（且编译器停止向下匹配其他命令） */
    ACCEPT,

    /** 2 表示不能编译脚本语句（编译器会继续尝试使用其他命令编译器编译脚本语句） */
    IGNORE,

    /** 3 表示不能编译脚本语句（脚本语句会直接作为脚本引擎的默认命令执行） */
    DENY
    
}
