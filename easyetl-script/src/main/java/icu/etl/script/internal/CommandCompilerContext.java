package icu.etl.script.internal;

import java.util.List;

import icu.etl.script.UniversalCommandCompiler;
import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;

/**
 * 脚本引擎命令的工厂配置信息
 *
 * @author jeremy8551@qq.com
 */
public class CommandCompilerContext {

    /** 保存脚本命令使用说明在资源文件中的先后顺序 */
    public final static List<String> scriptUsage = ResourcesUtils.getPropertyMiddleName("script.command");

    /** 命令编译器 */
    private UniversalCommandCompiler compiler;

    /** 命令编译器顺序编号 */
    private int order;

    /** 命令的使用说明前缀 */
    private String usage;

    /**
     * 初始化
     *
     * @param compiler 命令编译器
     */
    public CommandCompilerContext(UniversalCommandCompiler compiler) {
        this.compiler = Ensure.notNull(compiler);
        this.usage = ScriptUsage.getUsageSuffix(this.compiler.getClass());
        int index = scriptUsage.indexOf(this.usage);
        this.order = (index == -1 ? Integer.MAX_VALUE : index);
    }

    /**
     * 返回脚本命令对应的编译器
     *
     * @return 编译器
     */
    public UniversalCommandCompiler getCompiler() {
        return compiler;
    }

    /**
     * 返回命令的使用说明在资源文件中的顺序编号，按编号从小到大顺序排序
     *
     * @return 顺序编号
     */
    public int getInstructionOrder() {
        return order;
    }

    /**
     * 返回命令的使用说明编号
     *
     * @return 使用说明
     */
    public String getUsage() {
        return this.usage;
    }

    /**
     * 判断命令编译器类是否与类信息参数 cls 相等
     *
     * @param cls 类信息
     * @return 返回true表示编译命令与参数相等
     */
    public boolean isAssignableFrom(Class<?> cls) {
        return cls != null && cls.getName().equals(this.compiler.getClass().getName());
    }

    public boolean equals(Object obj) {
        if (obj instanceof CommandCompilerContext) { // 判断类型是否相等
            CommandCompilerContext context = (CommandCompilerContext) obj;
            UniversalCommandCompiler compiler = context.getCompiler();
            if (compiler != null && compiler.getClass().equals(this.compiler.getClass())) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return "[" + this.compiler.getClass().getName() + ", order=" + order + ", usage=" + usage + "]";
    }

}
