package icu.etl.database.export.inernal;

import java.io.IOException;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.database.export.ExtractMessage;
import icu.etl.database.export.ExtractWriter;
import icu.etl.database.export.ExtracterContext;
import icu.etl.ioc.BeanFactory;
import icu.etl.os.OSFtpCommand;
import icu.etl.util.Ensure;

@EasyBeanClass(kind = "ftp", mode = "", major = "", minor = "", description = "卸载数据到远程ftp服务器", type = ExtractWriter.class)
public class FtpFileWriter extends SftpFileWriter implements ExtractWriter {

    public FtpFileWriter(ExtracterContext context, ExtractMessage message, String host, String port, String username, String password, String remotepath) throws IOException {
        super(context, message, host, port, username, password, remotepath);
    }

    protected void open(String host, String port, String username, String password, String remotepath) {
        this.ftp = BeanFactory.get(OSFtpCommand.class, "ftp");
        Ensure.isTrue(this.ftp.connect(host, Integer.parseInt(port), username, password), host, port, username, password);
        this.target = "ftp://" + username + "@" + host + ":" + port + "?password=" + password;
    }

}
