package icu.etl.script.method;

import icu.etl.annotation.ScriptFunction;
import icu.etl.script.UniversalCommandRepository;
import icu.etl.script.UniversalScriptAnalysis;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptSession;
import icu.etl.script.UniversalScriptStderr;
import icu.etl.script.UniversalScriptStdout;
import icu.etl.script.UniversalScriptVariableMethod;
import icu.etl.script.command.VariableMethodCommand;
import icu.etl.script.command.VariableMethodCommandCompiler;
import icu.etl.util.FileUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 变量方法的模版
 */
public abstract class AbstractMethod implements UniversalScriptVariableMethod {

    /** 变量方法的返回值 */
    protected Object value;

    public abstract int execute(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptAnalysis analysis, String name, String method) throws Exception;

    /**
     * 执行下一个变量方法
     *
     * @param session  用户会话信息
     * @param context  脚本引擎上下文信息
     * @param stdout   标准信息输出流
     * @param stderr   错误信息输出流
     * @param analysis 语句分析器
     * @param name     变量名
     * @param method   方法名: .trim()
     * @param value    上一个变量方法的返回值
     * @param next     下一个变量方法的起始位置
     * @return
     * @throws Exception
     */
    protected int executeNextMethod(UniversalScriptSession session, UniversalScriptContext context, UniversalScriptStdout stdout, UniversalScriptStderr stderr, UniversalScriptAnalysis analysis, String name, String method, Object value, int next) throws Exception {
        if (next < method.length() && !analysis.isBlankline(method.substring(next))) {
            String variableName = name + "$" + StringUtils.toRandomUUID();
            try {
                session.addMethodVariable(variableName, value);

                UniversalCommandRepository cr = session.getCompiler().getRepository();
                VariableMethodCommandCompiler compiler = cr.get(VariableMethodCommandCompiler.class);
                VariableMethodRepository repository = compiler.getRepository();

                String nextMethod = StringUtils.ltrimBlank(method.substring(next), '.');
                String methodName = VariableMethodCommand.parseName(nextMethod);
                if (methodName == null) {
                    stderr.println(ResourcesUtils.getScriptStderrMessage(125, methodName));
                    return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
                }

                UniversalScriptVariableMethod obj = repository.get(methodName);
                if (obj == null) {
                    this.value = null;
                    stderr.println(ResourcesUtils.getScriptStderrMessage(125, nextMethod));
                    return UniversalScriptCommand.VARIABLE_METHOD_ERROR;
                } else {
                    int exitcode = obj.execute(session, context, stdout, stderr, analysis, variableName, nextMethod);
                    this.value = obj.value();
                    return exitcode;
                }
            } finally {
                session.removeMethodVariable(variableName);
            }
        } else {
            this.value = value;
            return 0;
        }
    }

    public Object value() {
        return value;
    }

    /**
     * 打印使用说明
     *
     * @param out
     */
    public void usage(UniversalScriptStdout out) {
        Class<? extends UniversalScriptVariableMethod> cls = this.getClass();
        ScriptFunction anno = cls.getAnnotation(ScriptFunction.class);
        String name = StringUtils.trimBlank(anno.name()).toLowerCase();
        this.usage(out, name);
    }

    /**
     * 打印使用说明
     *
     * @param out
     * @param name
     */
    protected void usage(UniversalScriptStdout out, String name) {
        String msg = this.usage(2, //
                ResourcesUtils.getMessage("script.variable.method." + name + ".synopsis"), //
                ResourcesUtils.getMessage("script.variable.method." + name + ".descriptions"), //
                ResourcesUtils.getMessage("script.variable.method." + name + ".parameters"), //
                ResourcesUtils.getMessage("script.variable.method." + name + ".return") //
        );
        out.println(msg);
    }

    /**
     * 打印变量方法的使用说明
     *
     * @param tabsize
     * @param synopsis
     * @param descriptions
     * @param parameters
     * @param returnHandle
     * @return
     */
    private String usage(int tabsize, String synopsis, String descriptions, String parameters, String returnHandle) {
        String titles[] = StringUtils.split(ResourcesUtils.getMessage("script.engine.usage.msg003"), ',');
        String tab = StringUtils.left('\t', 3, '\t');
        StringBuilder msg = new StringBuilder();
        if (StringUtils.isNotBlank(synopsis)) {
            msg.append(titles[0]).append(FileUtils.lineSeparator);
            String[] list = StringUtils.split(synopsis, '\n');
            for (String str : list) {
                msg.append(tab).append(str).append(FileUtils.lineSeparator);
            }
        }

        if (StringUtils.isNotBlank(descriptions)) {
            msg.append(titles[1]).append(FileUtils.lineSeparator);
            String[] list = StringUtils.split(descriptions, '\n');
            for (String str : list) {
                msg.append(tab).append(str).append(FileUtils.lineSeparator);
            }
        }

        if (StringUtils.isNotBlank(parameters)) {
            msg.append(titles[2]).append(FileUtils.lineSeparator);
            String[] list = StringUtils.split(parameters, '\n');
            for (String str : list) {
                msg.append(tab).append(str).append(FileUtils.lineSeparator);
            }
        }

        if (StringUtils.isNotBlank(returnHandle)) {
            msg.append(titles[3]).append(FileUtils.lineSeparator);
            String[] list = StringUtils.split(returnHandle, '\n');
            for (String str : list) {
                msg.append(tab).append(str).append(FileUtils.lineSeparator);
            }
        }

        return msg.toString();
    }

}
