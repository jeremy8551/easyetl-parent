package icu.etl.database.export;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.concurrent.Executor;
import icu.etl.ioc.EasyContext;
import icu.etl.util.ResourcesUtils;

/**
 * 数据抽取工具
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-11-12
 */
public class Extracter extends Executor {

    /** 上下文信息 */
    protected ExtracterContext context;

    /** 监听器 */
    protected ExtractListener listener;

    /** 消息信息 */
    protected ExtractMessage message;

    /** 容器上下文信息 */
    protected EasyContext ioccxt;

    /**
     * 初始化
     */
    public Extracter(EasyContext ioccxt) {
        super();
        this.ioccxt = ioccxt;
        this.context = new ExtracterContext(this);
        this.listener = new ExtractListener(this.context);
    }

    /**
     * 返回上下文信息
     *
     * @return
     */
    public ExtracterContext getContext() {
        return this.context;
    }

    public void execute() throws Exception {
        new ExtracterValidator().check(this.context);
        this.setName(ResourcesUtils.getExtractMessage(1, " " + this.context.getTarget()));
        this.listener.setListener(this.context.getListener());
        this.message = new ExtractMessage(this.context.getMessagefile(), this.context.getFormat().getCharsetName());
        this.listener.before();
        try {
            this.message.start();
            this.message.store();
            this.execute(this.context);
            this.message.finish();
            this.listener.after();
        } catch (Throwable e) {
            this.message.terminate();
            this.listener.catchError(e);
        } finally {
            this.message.store();
        }

        if (this.message.getMessagefile() == null) {
            this.log.info(this.message.toString());
        } else {
            this.log.info(ResourcesUtils.getExtractMessage(2, this.message.getRows(), this.message.getBytes(), this.watch.useTime(), this.message.getMessagefile()));
        }

        this.listener.destory();
    }

    /**
     * 执行数据卸载
     *
     * @param context 卸数引擎上下文
     * @throws SQLException
     * @throws IOException
     */
    protected void execute(ExtracterContext context) throws SQLException, IOException {
        this.message.setEncoding(context.getFormat().getCharsetName());
        this.message.setLineSeparator(context.getFormat().getLineSeparator());
        this.message.setDelimiter(context.getFormat().getDelimiter());
        this.message.setCharDelimiter(context.getFormat().getCharDelimiter());
        this.message.setSource(context.getSource());
        this.message.setColumn(context.getFormat().getColumn());
        this.message.setRows(0); // 在输出流中设置
        this.message.setBytes(0); // 在输出流中设置
        this.message.setTarget(""); // 在输出流中设置

        ExtractReader in = this.ioccxt.getBean(ExtractReader.class, context);
        try {
            ExtractWriter out = this.ioccxt.getBean(ExtractWriter.class, context, this.message);
            try {
                while (in.hasLine()) {
                    if (this.terminate) {
                        this.message.terminate();
                        break;
                    } else {
                        out.write(in);
                    }
                    out.rewrite();
                }
                out.flush();
            } finally {
                out.close();
            }

            this.message.setTime(this.watch.useTime());
            if (this.terminate) {
                this.message.terminate();
            }
        } finally {
            in.close();
        }
    }

    public int getPRI() {
        return 0;
    }

    /**
     * 返回监听器
     *
     * @return
     */
    public ExtractListener getListener() {
        return listener;
    }

}
