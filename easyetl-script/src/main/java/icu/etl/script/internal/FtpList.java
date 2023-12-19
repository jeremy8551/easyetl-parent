package icu.etl.script.internal;

import icu.etl.os.OSFtpCommand;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptProgram;

public class FtpList implements UniversalScriptProgram {

    public final static String key = "FtpList";

    public static FtpList get(UniversalScriptContext context, boolean... array) {
        boolean global = array.length == 0 ? false : array[0];
        FtpList obj = context.getProgram(key, global);
        if (obj == null) {
            obj = new FtpList();
            context.addProgram(key, obj, global);
        }
        return obj;
    }

    /** FTP 客户端接口 */
    private OSFtpCommand ftp;

    /**
     * 初始化
     */
    public FtpList() {
    }

    /**
     * 添加 FTP 客户端到脚本引擎上下文中
     *
     * @param ftp FTP客户端
     */
    public void add(OSFtpCommand ftp) {
        if (this.ftp != null) {
            this.ftp.close();
        }
        this.ftp = ftp;
    }

    /**
     * 返回 FTP 客户端
     *
     * @return
     */
    public OSFtpCommand getFTPClient() {
        return ftp;
    }

    public ScriptProgramClone deepClone() {
        FtpList obj = new FtpList();
        obj.ftp = this.ftp;
        return new ScriptProgramClone(key, obj);
    }

    public void close() {
        if (this.ftp != null) {
            this.ftp.close();
            this.ftp = null;
        }
    }

}
