package icu.etl.script.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import icu.etl.script.UniversalScriptContext;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎步骤信息集合
 */
public class ScriptStep {

    public final static String key = "ScriptStep";

    public static ScriptStep get(UniversalScriptContext context, boolean... array) {
        ScriptStep list = context.getProgram(key, array.length != 0 && array[0]);
        if (list == null) {
            list = new ScriptStep();
            if (array.length == 0) {
                context.addProgram(key, list, false);
            } else {
                context.addProgram(key, list, array[0]);
            }
        }
        return list;
    }

    /** 步骤信息集合 */
    private Vector<String> steps;

    /** true 表示当前脚本引擎正在执行 jump 命令 */
    private volatile boolean jumping;

    /** jump 命令信息 */
    private String target;

    /**
     * 初始化
     */
    public ScriptStep() {
        this.steps = new Vector<String>();
        this.jumping = false;
    }

    /**
     * 判断是否已添加步骤信息
     *
     * @param step 步骤信息
     * @return true表示已添加步骤信息
     */
    public boolean containsStep(String step) {
        return this.steps.contains(step);
    }

    /**
     * 添加步骤信息
     *
     * @param step 步骤信息
     */
    public void addStep(String step) {
        this.steps.add(step);
    }

    /**
     * 返回脚本引擎当前所处步骤的名字
     *
     * @return 如果未使用step命令设置返回null
     */
    public String getStep() {
        return this.steps.isEmpty() ? "" : this.steps.get(this.steps.size() - 1);
    }

    /**
     * 删除所有步骤信息
     *
     * @return 被删除的步骤信息
     */
    public List<String> removeStep() {
        List<String> list = new ArrayList<String>(this.steps);
        this.steps.clear();
        return list;
    }

    /**
     * 返回 true 表示还未运行到目标位置
     *
     * @return
     */
    public boolean containsTarget() {
        return jumping;
    }

    /**
     * 设置目标位置信息
     *
     * @param step step 命令的参数值
     */
    public void setTarget(String step) {
        if (StringUtils.isBlank(step)) {
            throw new IllegalArgumentException(step);
        }

        this.target = step;
        this.jumping = true;
    }

    /**
     * 返回目标位置信息
     *
     * @return
     */
    public String getTarget() {
        return target;
    }

    /**
     * 删除目标位置信息
     *
     * @return 原有的目标位置
     */
    public String removeTarget() {
        String str = this.target;
        this.jumping = false;
        this.target = "";
        return str;
    }

}
