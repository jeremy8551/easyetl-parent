package icu.etl.script.command.feature;

/**
 * 实现本接口的命令不可以在循环体语句中使用
 *
 * @author jeremy8551@qq.com
 */
public interface LoopCommandSupported {

    /**
     * 返回 true 表示不可以在循环体语句中使用
     *
     * @return
     */
    boolean enableLoop();

}
