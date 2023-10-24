package icu.etl.database.export;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.database.export.inernal.ExtractFileWriter;
import icu.etl.database.export.inernal.FtpFileWriter;
import icu.etl.database.export.inernal.HttpRequestWriter;
import icu.etl.database.export.inernal.SftpFileWriter;
import icu.etl.expression.LoginExpression;
import icu.etl.expression.StandardAnalysis;
import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.EasyetlContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.ClassUtils;
import icu.etl.util.StringUtils;

/**
 * 输出流的工厂类
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-18
 */
public class ExtractWriterBuilder implements BeanBuilder<ExtractWriter> {

    public ExtractWriter build(EasyetlContext context, Object... array) throws IOException, SQLException {
        return this.create(context, array);
    }

    private ExtractWriter create(EasyetlContext context, Object[] array) throws IOException, SQLException {
        ExtracterContext cxt = ArrayUtils.indexOf(array, ExtracterContext.class, 0);
        ExtractMessage message = ArrayUtils.indexOf(array, ExtractMessage.class, 0);
        String target = StringUtils.trimBlank(cxt.getTarget());

        // bean://kind/mode/major/minor
        if (StringUtils.startsWith(target, "bean://", 0, true, true)) {
            Object[] list = StringUtils.split(target.substring("bean://".length()), '/');
            Class<ExtractWriter> cls = context.getImplement(ExtractWriter.class, list);
            return ClassUtils.newInstance(cls);
        }

        // http://download/filename
        if (StringUtils.startsWith(target, "http://", 0, true, true)) {
            String[] list = StringUtils.split(target.substring("http://".length()), '/');
            String filename = ArrayUtils.lastElement(list);
            return new HttpRequestWriter(cxt.getHttpServletRequest(), cxt.getHttpServletResponse(), filename, cxt.getFormat(), message);
        }

        // sftp://name@host:port?password=/filepath
        if (StringUtils.startsWith(target, "sftp://", 0, true, true)) {
            String[] list = StringUtils.split(target.substring("sftp://".length()), '/');
            LoginExpression login = new LoginExpression(new StandardAnalysis(), "sftp " + list[0]);
            String host = login.getLoginHost();
            String port = login.getLoginPort();
            String username = login.getLoginUsername();
            String password = login.getLoginPassword();
            return new SftpFileWriter(cxt, message, host, port, username, password, list[1]);
        }

        // ftp://name@host:port?password=/filepath
        if (StringUtils.startsWith(target, "ftp://", 0, true, true)) {
            String[] list = StringUtils.split(target.substring("ftp://".length()), '/');
            LoginExpression cmd = new LoginExpression(new StandardAnalysis(), "ftp " + list[0]);
            String host = cmd.getLoginHost();
            String port = cmd.getLoginPort();
            String username = cmd.getLoginUsername();
            String password = cmd.getLoginPassword();
            return new FtpFileWriter(cxt, message, host, port, username, password, list[1]);
        }

        // 文件绝对路径
        return new ExtractFileWriter(cxt, message);
    }

}
