package icu.etl.script.command.feature;

public interface NohupCommandSupported {

    /**
     * 返回 true 表示命令可以在后台执行
     *
     * @return
     */
    boolean enableNohup();

}
