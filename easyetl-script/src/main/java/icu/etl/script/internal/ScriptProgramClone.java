package icu.etl.script.internal;

public class ScriptProgramClone {

    private String key;

    private Object value;

    public ScriptProgramClone(String key, Object value) {
        super();
        this.key = key;
        this.value = value;
    }

    /**
     * 返回程序名，用于在 {@linkplain ScriptProgram} 中唯一区分一个程序对象
     *
     * @return 程序名
     */
    public String getKey() {
        return this.key;
    }

    /**
     * 返回程序对象本身
     *
     * @return 程序对象本身
     */
    public Object getValue() {
        return this.value;
    }

}
