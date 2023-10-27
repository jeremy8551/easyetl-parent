package icu.etl.script.internal;

import java.lang.reflect.Method;
import java.util.List;

import icu.etl.annotation.ScriptCommand;
import icu.etl.ioc.BeanInfo;
import icu.etl.ioc.EasyetlContext;
import icu.etl.log.Log;
import icu.etl.script.UniversalCommandCompiler;
import icu.etl.script.UniversalCommandRepository;
import icu.etl.script.UniversalScriptCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptContextAware;
import icu.etl.script.UniversalScriptEngineFactory;
import icu.etl.script.UniversalScriptException;
import icu.etl.script.command.BreakCommandCompiler;
import icu.etl.script.command.ByeCommandCompiler;
import icu.etl.script.command.CallProcudureCommandCompiler;
import icu.etl.script.command.CallbackCommandCompiler;
import icu.etl.script.command.CatCommandCompiler;
import icu.etl.script.command.CdCommandCompiler;
import icu.etl.script.command.CommitCommandCompiler;
import icu.etl.script.command.ContainerCommandCompiler;
import icu.etl.script.command.ContinueCommandCompiler;
import icu.etl.script.command.CursorCommandCompiler;
import icu.etl.script.command.DBConnectCommandCompiler;
import icu.etl.script.command.DBExportCommandCompiler;
import icu.etl.script.command.DBLoadCommandCompiler;
import icu.etl.script.command.DaemonCommandCompiler;
import icu.etl.script.command.DateCommandCompiler;
import icu.etl.script.command.DeclareCatalogCommandCompiler;
import icu.etl.script.command.DeclareCursorCommandCompiler;
import icu.etl.script.command.DeclareHandlerCommandCompiler;
import icu.etl.script.command.DeclareProgressCommandCompiler;
import icu.etl.script.command.DeclareSSHClientCommandCompiler;
import icu.etl.script.command.DeclareSSHTunnelCommandCompiler;
import icu.etl.script.command.DeclareStatementCommandCompiler;
import icu.etl.script.command.DefaultCommandCompiler;
import icu.etl.script.command.DfCommandCompiler;
import icu.etl.script.command.Dos2UnixCommandCompiler;
import icu.etl.script.command.EchoCommandCompiler;
import icu.etl.script.command.ErrorCommandCompiler;
import icu.etl.script.command.ExecuteFileCommandCompiler;
import icu.etl.script.command.ExecuteFunctionCommandCompiler;
import icu.etl.script.command.ExecuteOSCommandCompiler;
import icu.etl.script.command.ExistsCommandCompiler;
import icu.etl.script.command.ExitCommandCompiler;
import icu.etl.script.command.ExportCommandCompiler;
import icu.etl.script.command.FetchCursorCommandCompiler;
import icu.etl.script.command.FetchStatementCommandCompiler;
import icu.etl.script.command.FindCommandCompiler;
import icu.etl.script.command.ForCommandCompiler;
import icu.etl.script.command.FtpCommandCompiler;
import icu.etl.script.command.FunctionCommandCompiler;
import icu.etl.script.command.GetCommandCompiler;
import icu.etl.script.command.GrepCommandCompiler;
import icu.etl.script.command.GunzipCommandCompiler;
import icu.etl.script.command.GzipCommandCompiler;
import icu.etl.script.command.HandlerCommandCompiler;
import icu.etl.script.command.HeadCommandCompiler;
import icu.etl.script.command.HelpCommandCompiler;
import icu.etl.script.command.IfCommandCompiler;
import icu.etl.script.command.IncrementCommandCompiler;
import icu.etl.script.command.IsDirectoryCommandCompiler;
import icu.etl.script.command.IsFileCommandCompiler;
import icu.etl.script.command.JavaCommandCompiler;
import icu.etl.script.command.JumpCommandCompiler;
import icu.etl.script.command.LengthCommandCompiler;
import icu.etl.script.command.LsCommandCompiler;
import icu.etl.script.command.MD5CommandCompiler;
import icu.etl.script.command.MkdirCommandCompiler;
import icu.etl.script.command.NohupCommandCompiler;
import icu.etl.script.command.PSCommandCompiler;
import icu.etl.script.command.PipeCommandCompiler;
import icu.etl.script.command.ProgressCommandCompiler;
import icu.etl.script.command.PutCommandCompiler;
import icu.etl.script.command.PwdCommandCompiler;
import icu.etl.script.command.QuietCommandCompiler;
import icu.etl.script.command.ReadCommandCompiler;
import icu.etl.script.command.ReturnCommandCompiler;
import icu.etl.script.command.RmCommandCompiler;
import icu.etl.script.command.RollbackCommandCompiler;
import icu.etl.script.command.SQLCommandCompiler;
import icu.etl.script.command.SSH2CommandCompiler;
import icu.etl.script.command.SetCommandCompiler;
import icu.etl.script.command.SftpCommandCompiler;
import icu.etl.script.command.SleepCommandCompiler;
import icu.etl.script.command.StacktraceCommandCompiler;
import icu.etl.script.command.StepCommandCompiler;
import icu.etl.script.command.SubCommandCompiler;
import icu.etl.script.command.TailCommandCompiler;
import icu.etl.script.command.TarCommandCompiler;
import icu.etl.script.command.TerminateCommandCompiler;
import icu.etl.script.command.UUIDCommandCompiler;
import icu.etl.script.command.UndeclareCatalogCommandCompiler;
import icu.etl.script.command.UndeclareCursorCommandCompiler;
import icu.etl.script.command.UndeclareHandlerCommandCompiler;
import icu.etl.script.command.UndeclareSSHCommandCompiler;
import icu.etl.script.command.UndeclareStatementCommandCompiler;
import icu.etl.script.command.UnrarCommandCompiler;
import icu.etl.script.command.UnzipCommandCompiler;
import icu.etl.script.command.VariableMethodCommandCompiler;
import icu.etl.script.command.WaitCommandCompiler;
import icu.etl.script.command.WcCommandCompiler;
import icu.etl.script.command.WhileCommandCompiler;
import icu.etl.script.command.ZipCommandCompiler;
import icu.etl.util.ClassUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎命令类扫描器 <br>
 * 用于扫描当前JVM 中所有可用的脚本引擎命令类信息
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-09
 */
public class CommandScanner {

    /** 日志输出接口 */
    private Log log;

    /** 脚本引擎命令仓库 */
    private UniversalCommandRepository repository;

    /** 脚本引擎工厂 */
    private UniversalScriptEngineFactory factory;

    /** 脚本引擎上下文信息 */
    private UniversalScriptContext context;

    /**
     * 初始化
     *
     * @param context    脚本引擎上下文信息
     * @param repository 命令仓库
     */
    public CommandScanner(UniversalScriptContext context, UniversalCommandRepository repository) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (repository == null) {
            throw new NullPointerException();
        }

        this.context = context;
        this.factory = context.getFactory();
        this.repository = repository;
        this.log = this.factory.getStdoutLog();

        // 显示所有已加载的脚本引擎命令
        EasyetlContext cxt = context.getFactory().getContext();
        List<BeanInfo> beanList = cxt.getBeanInfoList(UniversalCommandCompiler.class);
        for (BeanInfo beanInfo : beanList) {
            Class<? extends UniversalCommandCompiler> cls = beanInfo.getType();
            try {
                this.loadScriptCommand(cls);
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getClassMessage(18, cls.getName()), e);
                }
            }
        }

        // 如果类扫描器未扫描到脚本引擎默认命令，则自动加载所有命令
        if (beanList.isEmpty()) {
            this.loadCommandBuilder();
        }
    }

    /**
     * 加载脚本命令
     *
     * @param cls 脚本引擎命令的Class信息
     */
    public void loadScriptCommand(Class<? extends UniversalCommandCompiler> cls) {
        if (this.repository.contains(cls)) {
            return;
        }

        if (cls.isAnnotationPresent(ScriptCommand.class)) {
            ScriptCommand anno = cls.getAnnotation(ScriptCommand.class);
            String[] names = StringUtils.trimBlank(anno.name());
            String[] words = StringUtils.trimBlank(anno.keywords());

            if (StringUtils.isBlank(names)) {
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getScriptStderrMessage(48, cls.getName(), ScriptCommand.class.getName(), "name"));
                }
                return;
            }

            UniversalCommandCompiler compiler = null;
            try {
                compiler = ClassUtils.newInstance(cls);
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn(ResourcesUtils.getClassMessage(12, cls.getName()), e);
                }
                return;
            }

            // 向编译器中注入脚本引擎上下文信息
            if (compiler instanceof UniversalScriptContextAware) {
                ((UniversalScriptContextAware) compiler).setContext(this.context);
            } else {
                this.invoke(compiler, this.context);
            }

            this.repository.add(names, compiler);
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getScriptStdoutMessage(42, cls.getName()));
            }

            // 关键字
            for (String key : words) {
                this.factory.getKeywords().add(key);
            }
        } else {
            if (log.isDebugEnabled()) { // 只有调试模式才会打印警告
                log.warn(ResourcesUtils.getScriptStderrMessage(52, cls.getName(), UniversalScriptCommand.class.getName(), ScriptCommand.class.getName()));
            }
        }
    }

    /**
     * 向编译器中注入脚本引擎上下文信息，如果编译器类中存在 void set(UniversalScriptContext) 方法时，自动调用方法并注入上下文信息
     *
     * @param compiler 编译器
     * @param context  上下文信息
     */
    private void invoke(UniversalCommandCompiler compiler, UniversalScriptContext context) {
        Method[] methods = compiler.getClass().getDeclaredMethods();
        for (Method method : methods) {
            Class<?>[] types = method.getParameterTypes();
            if (method.getName().startsWith("set") // 方法名
                    && "void".equalsIgnoreCase(method.getReturnType().getName()) // 无返回值
                    && types != null && types.length == 1 && types[0].equals(UniversalScriptContext.class) // 只有一个参数
            ) {
                try {
                    method.invoke(compiler, new Object[]{context});
                } catch (Throwable e) {
                    throw new UniversalScriptException(compiler.getClass().getName() + "." + StringUtils.toString(method), e);
                }
            }
        }
    }

    /**
     * 加载基础脚本命令
     */
    public void loadCommandBuilder() {
        this.loadScriptCommand(BreakCommandCompiler.class);
        this.loadScriptCommand(ByeCommandCompiler.class);
        this.loadScriptCommand(CallProcudureCommandCompiler.class);
        this.loadScriptCommand(CallbackCommandCompiler.class);
        this.loadScriptCommand(CatCommandCompiler.class);
        this.loadScriptCommand(CdCommandCompiler.class);
        this.loadScriptCommand(CommitCommandCompiler.class);
        this.loadScriptCommand(ContainerCommandCompiler.class);
        this.loadScriptCommand(ContinueCommandCompiler.class);
        this.loadScriptCommand(CursorCommandCompiler.class);
        this.loadScriptCommand(DBConnectCommandCompiler.class);
        this.loadScriptCommand(DBExportCommandCompiler.class);
        this.loadScriptCommand(DBLoadCommandCompiler.class);
        this.loadScriptCommand(DaemonCommandCompiler.class);
        this.loadScriptCommand(DateCommandCompiler.class);
        this.loadScriptCommand(DeclareCatalogCommandCompiler.class);
        this.loadScriptCommand(DeclareCursorCommandCompiler.class);
        this.loadScriptCommand(DeclareHandlerCommandCompiler.class);
        this.loadScriptCommand(DeclareProgressCommandCompiler.class);
        this.loadScriptCommand(DeclareSSHClientCommandCompiler.class);
        this.loadScriptCommand(DeclareSSHTunnelCommandCompiler.class);
        this.loadScriptCommand(DeclareStatementCommandCompiler.class);
        this.loadScriptCommand(DefaultCommandCompiler.class);
        this.loadScriptCommand(DfCommandCompiler.class);
        this.loadScriptCommand(Dos2UnixCommandCompiler.class);
        this.loadScriptCommand(EchoCommandCompiler.class);
        this.loadScriptCommand(ErrorCommandCompiler.class);
        this.loadScriptCommand(ExecuteFileCommandCompiler.class);
        this.loadScriptCommand(ExecuteFunctionCommandCompiler.class);
        this.loadScriptCommand(ExecuteOSCommandCompiler.class);
        this.loadScriptCommand(ExistsCommandCompiler.class);
        this.loadScriptCommand(ExitCommandCompiler.class);
        this.loadScriptCommand(ExportCommandCompiler.class);
        this.loadScriptCommand(FetchCursorCommandCompiler.class);
        this.loadScriptCommand(FetchStatementCommandCompiler.class);
        this.loadScriptCommand(FindCommandCompiler.class);
        this.loadScriptCommand(ForCommandCompiler.class);
        this.loadScriptCommand(FtpCommandCompiler.class);
        this.loadScriptCommand(FunctionCommandCompiler.class);
        this.loadScriptCommand(GetCommandCompiler.class);
        this.loadScriptCommand(GrepCommandCompiler.class);
        this.loadScriptCommand(GunzipCommandCompiler.class);
        this.loadScriptCommand(GzipCommandCompiler.class);
        this.loadScriptCommand(HandlerCommandCompiler.class);
        this.loadScriptCommand(HeadCommandCompiler.class);
        this.loadScriptCommand(HelpCommandCompiler.class);
        this.loadScriptCommand(IfCommandCompiler.class);
        this.loadScriptCommand(IncrementCommandCompiler.class);
        this.loadScriptCommand(IsDirectoryCommandCompiler.class);
        this.loadScriptCommand(IsFileCommandCompiler.class);
        this.loadScriptCommand(JavaCommandCompiler.class);
        this.loadScriptCommand(JumpCommandCompiler.class);
        this.loadScriptCommand(LengthCommandCompiler.class);
        this.loadScriptCommand(LsCommandCompiler.class);
        this.loadScriptCommand(MD5CommandCompiler.class);
        this.loadScriptCommand(MkdirCommandCompiler.class);
        this.loadScriptCommand(NohupCommandCompiler.class);
        this.loadScriptCommand(PSCommandCompiler.class);
        this.loadScriptCommand(PipeCommandCompiler.class);
        this.loadScriptCommand(ProgressCommandCompiler.class);
        this.loadScriptCommand(PutCommandCompiler.class);
        this.loadScriptCommand(PwdCommandCompiler.class);
        this.loadScriptCommand(QuietCommandCompiler.class);
        this.loadScriptCommand(ReadCommandCompiler.class);
        this.loadScriptCommand(ReturnCommandCompiler.class);
        this.loadScriptCommand(RmCommandCompiler.class);
        this.loadScriptCommand(RollbackCommandCompiler.class);
        this.loadScriptCommand(SQLCommandCompiler.class);
        this.loadScriptCommand(SSH2CommandCompiler.class);
        this.loadScriptCommand(SetCommandCompiler.class);
        this.loadScriptCommand(SftpCommandCompiler.class);
        this.loadScriptCommand(SleepCommandCompiler.class);
        this.loadScriptCommand(StacktraceCommandCompiler.class);
        this.loadScriptCommand(StepCommandCompiler.class);
        this.loadScriptCommand(SubCommandCompiler.class);
        this.loadScriptCommand(TailCommandCompiler.class);
        this.loadScriptCommand(TarCommandCompiler.class);
        this.loadScriptCommand(TerminateCommandCompiler.class);
        this.loadScriptCommand(UUIDCommandCompiler.class);
        this.loadScriptCommand(UndeclareCatalogCommandCompiler.class);
        this.loadScriptCommand(UndeclareCursorCommandCompiler.class);
        this.loadScriptCommand(UndeclareHandlerCommandCompiler.class);
        this.loadScriptCommand(UndeclareSSHCommandCompiler.class);
        this.loadScriptCommand(UndeclareStatementCommandCompiler.class);
        this.loadScriptCommand(UnrarCommandCompiler.class);
        this.loadScriptCommand(UnzipCommandCompiler.class);
        this.loadScriptCommand(VariableMethodCommandCompiler.class);
        this.loadScriptCommand(WaitCommandCompiler.class);
        this.loadScriptCommand(WcCommandCompiler.class);
        this.loadScriptCommand(WhileCommandCompiler.class);
        this.loadScriptCommand(ZipCommandCompiler.class);
    }

}
