package icu.etl.script.command.feature;

public interface NohupCommandSupported {

    /**
     * 判断命令是否可以在后台执行
     *
     * @return 返回 true 表示命令可以在后台执行
     */
    boolean enableNohup();

}
