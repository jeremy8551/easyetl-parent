package icu.etl.database.export.inernal;

import java.io.IOException;

import icu.etl.annotation.EasyBean;
import icu.etl.database.export.ExtractMessage;
import icu.etl.database.export.ExtractWriter;
import icu.etl.database.export.ExtracterContext;
import icu.etl.ioc.EasyetlContext;
import icu.etl.ioc.EasyetlContextAware;
import icu.etl.os.OSFtpCommand;
import icu.etl.util.Ensure;

@EasyBean(kind = "ftp", mode = "", major = "", minor = "", description = "卸载数据到远程ftp服务器")
public class FtpFileWriter extends SftpFileWriter implements ExtractWriter, EasyetlContextAware {

    protected EasyetlContext context;

    public void setContext(EasyetlContext context) {
        this.context = context;
    }

    public FtpFileWriter(ExtracterContext context, ExtractMessage message, String host, String port, String username, String password, String remotepath) throws IOException {
        super(context, message, host, port, username, password, remotepath);
    }

    protected void open(String host, String port, String username, String password, String remotepath) {
        this.ftp = this.context.get(OSFtpCommand.class, "ftp");
        Ensure.isTrue(this.ftp.connect(host, Integer.parseInt(port), username, password), host, port, username, password);
        this.target = "ftp://" + username + "@" + host + ":" + port + "?password=" + password;
    }

}
