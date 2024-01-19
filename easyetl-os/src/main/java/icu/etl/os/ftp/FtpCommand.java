package icu.etl.os.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import icu.apache.net.ftp.FTPClient;
import icu.apache.net.ftp.parser.FTPFileEntryParserFactory;
import icu.etl.annotation.EasyBean;
import icu.etl.expression.GPatternExpression;
import icu.etl.ioc.EasyContext;
import icu.etl.ioc.EasyContextAware;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.os.OSFile;
import icu.etl.os.OSFileCommandException;
import icu.etl.os.OSFileFilter;
import icu.etl.os.OSFtpCommand;
import icu.etl.os.internal.OSFileImpl;
import icu.etl.os.linux.LinuxLocalOS;
import icu.etl.util.ClassUtils;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.NetUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

@EasyBean(name = "ftp", description = "FTP协议的实现类")
public class FtpCommand implements OSFtpCommand, EasyContextAware {
    private final static Log log = LogFactory.getLog(FtpCommand.class);

    /** ftp 客户端组件 */
    private FTPClient client;

    /** ftp 命令参数集合 */
    protected HashMap<String, String> params;

    /** 远程服务器上文件路径分隔符 */
    protected char folderSeperator;

    /** username@host:port */
    protected String remoteServerName;

    /** 容器上下文信息 */
    protected EasyContext context;

    public FtpCommand() {
        this.client = new FTPClient();
        this.params = new HashMap<String, String>();
        this.folderSeperator = '/';
    }

    public void setContext(EasyContext context) {
        this.context = context;
    }

    public synchronized boolean connect(String host, int port, String username, String password) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg001", username + "@" + host + ":" + port + "?password=" + password));
        }

        try {
            this.setPreParams();
            this.client.connect(host, port);
            if (this.client.login(username, password)) {
                this.folderSeperator = this.client.getSystemType().toLowerCase().indexOf("windows") == -1 ? '/' : '\\';
                this.remoteServerName = username + "@" + host + ":" + port;
                this.setLstParams();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("ftp " + username + "@" + host + ":" + port + "?password=" + password + " fail!", e);
            }
            this.close();
            return false;
        }
    }

    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    // public synchronized void set(String key, String value) {
    // if (!ApacheFtpClient.PARAM_NAME_LIST.contains(key)) {
    // throw new FTPCommandException(key + " = " + value);
    // }
    //
    // this.params.put(key, value);
    // }

    protected synchronized void setPreParams() {
        this.client.setControlEncoding(this.params.containsKey("ControlEncoding") ? this.params.get("ControlEncoding") : StringUtils.CHARSET);
        this.client.setBufferSize(this.params.containsKey("BufferSize") ? Integer.parseInt(this.params.get("BufferSize")) : 1024);
        this.client.setRemoteVerificationEnabled(this.params.containsKey("RemoteVerificationEnabled") ? Boolean.parseBoolean(this.params.get("RemoteVerificationEnabled")) : false);

        if (this.params.containsKey("DataTimeout")) {
            this.client.setDataTimeout(Integer.parseInt(this.params.get("DataTimeout")));
        }

        if (this.params.containsKey("ParserFactory")) {
            this.client.setParserFactory((FTPFileEntryParserFactory) ClassUtils.newInstance(this.params.get("ParserFactory"), this.context.getClassLoader()));
        }

        if (this.params.containsKey("RestartOffset")) {
            this.client.setRestartOffset(Long.parseLong(this.params.get("RestartOffset")));
        }
    }

    protected synchronized void setLstParams() throws IOException {
        try {
            this.client.setFileType(this.params.containsKey("FileType") ? Integer.parseInt(this.params.get("FileType")) : FTPClient.BINARY_FILE_TYPE);
        } catch (Exception e) {
            throw new IllegalArgumentException("setFileType(" + this.params.get("FileType") + ")");
        }

        if (this.params.containsKey("FileStructure")) {
            try {
                this.client.setFileStructure(Integer.parseInt(this.params.get("FileStructure")));
            } catch (Exception e) {
                throw new IllegalArgumentException("setFileStructure(" + this.params.get("FileStructure") + ")");
            }
        }

        if (this.params.containsKey("FileTransferMode")) {
            try {
                this.client.setFileTransferMode(Integer.parseInt(this.params.get("FileTransferMode")));
            } catch (Exception e) {
                throw new IllegalArgumentException("setFileTransferMode(" + this.params.get("FileTransferMode") + ")");
            }
        }

        if ("RemotePassiveMode".equalsIgnoreCase(this.params.get("RemotePassiveMode"))) {
            this.client.enterRemotePassiveMode();
        } else {
            this.client.enterLocalPassiveMode(); // set local passive mode
        }
    }

    /**
     * 返回远程文件信息
     *
     * @param filepath 文件路径
     * @return 文件信息
     * @throws IOException 访问文件错误
     */
    protected synchronized ApacheFtpFile toFtpFile(String filepath) throws IOException {
        String status = this.client.getStatus(filepath);
        if (log.isDebugEnabled()) {
            log.debug(status);
        }

        String parent = FileUtils.getParent(filepath); // 父目录
        String filename = StringUtils.defaultString(FileUtils.getFilename(filepath), ""); // 文件名
        boolean isDir = this.isDir(parent, filepath, filename);
        List<String> lines = this.parse(status);

        List<OSFile> list = new ArrayList<OSFile>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] array = StringUtils.splitByBlank(StringUtils.trimBlank(line));
            if (array.length < 9) {
                throw new IOException(status + "\nlineno: " + (i + 1) + ", content: " + line);
            }

            OSFileImpl file = this.toFtpFile(line, array, parent);
            if (LinuxLocalOS.KEY_FILENAMES.contains(file.getName())) {
                isDir = true;
                continue;
            } else {
                list.add(file);
            }

            if (log.isDebugEnabled()) {
                log.debug(file.toString());
            }
        }

        if (isDir) {
            for (OSFile file : list) {
                ((OSFileImpl) file).setParent(filepath);
            }
        }

        return new ApacheFtpFile(filename, isDir, list);
    }

    private List<String> parse(String status) {
        List<String> list = new ArrayList<String>();
        Iterator<CharSequence> it = StringUtils.splitLines(status, new ArrayList<CharSequence>()).iterator();

        // 第一行不可用
        String line = it.hasNext() ? it.next().toString() : null;
        if (StringUtils.splitByBlank(StringUtils.trimBlank(line)).length >= 9) {
            throw new OSFileCommandException(status);
        }

        while (it.hasNext()) {
            line = it.next().toString();

            // 如果是最后一行则退出
            if (!it.hasNext() && (line.endsWith("End of status.") || StringUtils.splitByBlank(StringUtils.trimBlank(line)).length < 9)) {
                break;
            } else {
                list.add(line);
            }
        }
        return list;
    }

    private boolean isDir(String parent, String filepath, String filename) throws IOException {
        if (filepath.equals("/")) {
            return true;
        }
        if (parent == null || parent.length() == 0) {
            parent = "/";
        }

        String status = this.client.getStatus(parent);
        if (log.isDebugEnabled()) {
            log.debug(parent + " status: \n" + status);
        }

        List<String> list = this.parse(status);
        for (String line : list) {
            String[] array = StringUtils.splitByBlank(StringUtils.trimBlank(line));
            OSFileImpl file = this.toFtpFile(line, array, "null");
            if (filename.equals(file.getName())) {
                if (file.isDirectory() || file.isLink()) {
                    return true;
                }
            }
        }
        throw new OSFileCommandException(status);
    }

    protected OSFileImpl toFtpFile(String line, String[] array, String parent) {
        OSFileImpl file = new OSFileImpl();
        file.setLongname(line);
        file.setParent(parent);
        switch (array[0].charAt(0)) {
            case '-':
                file.setFile(true);
                break;
            case 'd':
                file.setDirectory(true);
                break;
            case 'l':
                file.setLink(true);
                break;
            case 'b':
                file.setBlockDevice(true);
                break;
            case 'c':
                file.setCharDevice(true);
                break;
            case 's':
                file.setSock(true);
                break;
            case 'p':
                file.setPipe(true);
                break;
            default:
                throw new OSFileCommandException(array[0] + ", " + line);
        }

        file.setCanRead(array[0].charAt(1) == 'r');
        file.setCanWrite(array[0].charAt(2) == 'w');
        file.setCanExecute(array[0].charAt(3) == 'x');
        file.setLength(Long.parseLong(array[4]));
        file.setCreateTime(null);
        file.setModifyTime(this.formatDate(array));
        file.setName(array[8]);

        if (file.isLink()) {
            if (!array[9].equals("->") || array.length != 11) {
                throw new OSFileCommandException(StringUtils.toString(array));
            }
            file.setLink(array[10]);
        }
        return file;
    }

    /**
     * 格式化时间
     *
     * @param array 字符串数组
     * @return 时间
     */
    protected Date formatDate(String[] array) {
        StringBuilder buf = new StringBuilder();
        buf.append(array[6]);
        buf.append(' ');
        buf.append(array[5]);
        buf.append(' ');

        if (array.length >= 9) {
            String time = array[7];
            if (time.indexOf(':') == -1) { // 年份
                buf.append(time);
            } else {
                buf.append(Dates.getYear(new Date()));
                buf.append(" at ");
                buf.append(time).append(":00");
            }
        } else {
            buf.append(Dates.getYear(new Date()));
        }
        return Dates.parse(buf);
    }

    public void terminate() {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "terminate"));
        }

        try {
            this.client.abort();
        } catch (Exception e) {
            throw new OSFileCommandException("terminate", e);
        }
    }

    public synchronized boolean exists(String filepath) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "exists " + filepath));
        }

        try {
            return this.toFtpFile(filepath) != null;
        } catch (Exception e) {
            throw new OSFileCommandException("exists " + filepath, e);
        }
    }

    public synchronized boolean isFile(String filepath) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "isFile " + filepath));
        }

        try {
            ApacheFtpFile file = this.toFtpFile(filepath);
            return file != null && !file.isDirectory();
        } catch (Exception e) {
            throw new OSFileCommandException("isFile " + filepath, e);
        }
    }

    public synchronized boolean isDirectory(String filepath) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "isDirectory " + filepath));
        }

        try {
            ApacheFtpFile file = this.toFtpFile(filepath);
            return file != null && file.isDirectory();
        } catch (Exception e) {
            throw new OSFileCommandException("isDirectory " + filepath, e);
        }
    }

    public synchronized boolean mkdir(String filepath) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "mkdir " + filepath));
        }

        return this.client.makeDirectory(filepath);
    }

    public synchronized boolean cd(String filepath) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "cd " + filepath));
        }

        try {
            return this.client.changeWorkingDirectory(filepath);
        } catch (Exception e) {
            throw new OSFileCommandException("cd " + filepath, e);
        }
    }

    public synchronized boolean rm(String filepath) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "rm " + filepath));
        }
        return this.rmfile(filepath);
    }

    protected synchronized boolean rmfile(String filepath) throws IOException {
        ApacheFtpFile file = this.toFtpFile(filepath);
        if (file == null) {
            return true;
        } else if (file.isDirectory()) {
            String remoteDir = FileUtils.rtrimFolderSeparator(filepath);
            List<OSFile> files = file.listFiles();
            for (OSFile cfile : files) {
                String childFile = remoteDir + this.folderSeperator + cfile.getName();
                if (!this.rmfile(childFile)) {
                    return false;
                }
            }

            boolean success = this.client.removeDirectory(remoteDir);
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "delete remote directory " + remoteDir + "  [" + success + "]"));
            }
            return success;
        } else {
            boolean success = this.client.deleteFile(filepath);
            if (log.isDebugEnabled()) {
                log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "delete remote file " + filepath + " [" + success + "]"));
            }
            return success;
        }

    }

    public synchronized String pwd() {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "pwd"));
        }

        try {
            return this.client.printWorkingDirectory();
        } catch (Exception e) {
            throw new OSFileCommandException("pwd", e);
        }
    }

    public synchronized List<OSFile> ls(String filepath) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "ls " + filepath));
        }

        ApacheFtpFile file = this.toFtpFile(filepath);
        return file == null ? new ArrayList<OSFile>(0) : file.listFiles();
    }

    public boolean copy(String filepath, String directory) {
        Ensure.isTrue(!filepath.equals(directory), filepath, directory);
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "copy " + filepath + " " + directory));
        }

        try {
            ApacheFtpFile file = this.toFtpFile(directory);
            if (file == null) {
                if (!this.client.makeDirectory(directory)) {
                    return false;
                }
            } else if (!file.isDirectory()) {
                return false;
            }

            File downfile = this.downfile(filepath, FileUtils.getTempDir("ftp", "download", Dates.format17()));
            if (downfile == null) {
                return false;
            }

            try {
                return this.uploadfile(downfile, directory);
            } finally {
                FileUtils.delete(downfile);
            }
        } catch (Exception e) {
            throw new OSFileCommandException("copy " + filepath + " " + directory, e);
        }
    }

    public synchronized boolean upload(File localFile, String remoteDir) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "put " + localFile.getAbsolutePath() + " " + remoteDir));
        }

        try {
            return this.uploadfile(localFile, remoteDir);
        } catch (Exception e) {
            throw new OSFileCommandException("put " + localFile.getAbsolutePath() + " to " + remoteDir, e);
        }
    }

    protected synchronized boolean uploadfile(File localFile, String remoteDir) throws IOException {
        remoteDir = FileUtils.rtrimFolderSeparator(remoteDir);
        this.createDirectory(remoteDir);
        if (localFile.isDirectory()) {
            String cdir = remoteDir + this.folderSeperator + localFile.getName();
            this.createDirectory(cdir);
            File[] listFiles = FileUtils.array(localFile.listFiles());
            for (File f : listFiles) {
                if (!this.uploadfile(f, cdir)) {
                    return false;
                }
            }
        } else {
            if (!this.putFile(localFile, remoteDir)) {
                return false;
            }
        }
        return true;
    }

    protected synchronized void createDirectory(String remotepath) throws IOException {
        String remoteDir = FileUtils.rtrimFolderSeparator(remotepath);
        ApacheFtpFile stats = this.toFtpFile(remoteDir);
        if (stats == null) {
            this.client.mkd(remoteDir);
        } else if (stats.isDirectory()) {
            return;
        } else {
            throw new IOException(remoteDir + " is not directory!");
        }
    }

    /**
     * 上传文件
     *
     * @param localFile 本地文件
     * @param remoteDir 远程目录
     * @return 返回true表示上传成功 false表示上传失败
     * @throws IOException 访问文件错误或IO错误
     */
    protected synchronized boolean putFile(File localFile, String remoteDir) throws IOException {
        FileInputStream in = new FileInputStream(localFile);
        try {
            String remotefile = FileUtils.rtrimFolderSeparator(remoteDir) + this.folderSeperator + localFile.getName();
            return this.client.storeFile(remotefile, in);
        } finally {
            in.close();
        }
    }

    public synchronized boolean upload(InputStream in, String remote) {
        try {
            return this.client.storeFile(remote, in);
        } catch (Exception e) {
            throw new OSFileCommandException("upload " + in + " to " + remote, e);
        }
    }

    public synchronized boolean download(String remote, OutputStream out) {
        try {
            return this.client.retrieveFile(remote, out);
        } catch (Exception e) {
            throw new OSFileCommandException("download " + remote + " " + out, e);
        }
    }

    public synchronized File download(String filepath, File localFile) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "get " + filepath + " " + localFile.getAbsolutePath()));
        }

        try {
            return this.downfile(filepath, localFile);
        } catch (Exception e) {
            throw new OSFileCommandException("get " + filepath + " " + localFile, e);
        }
    }

    protected synchronized File downfile(String filepath, File localDir) throws IOException {
        filepath = FileUtils.rtrimFolderSeparator(filepath);
        ApacheFtpFile remotefile = this.toFtpFile(filepath);
        if (remotefile == null) {
            return null;
        }

        if (remotefile.isDirectory()) {
            String newfilepath = FileUtils.rtrimFolderSeparator(filepath);
            File localfile = new File(localDir, FileUtils.getFilename(newfilepath));
            if (!FileUtils.createDirectory(localfile)) {
                return null;
            }

            List<OSFile> filelist = remotefile.listFiles();
            for (OSFile osfile : filelist) {
                if (osfile.isDirectory()) {
                    if (this.downfile(newfilepath + this.folderSeperator + osfile.getName(), localfile) == null) {
                        return null;
                    }
                } else {
                    if (this.writefile(newfilepath + this.folderSeperator + osfile.getName(), localfile) == null) {
                        return null;
                    }
                }
            }
            return localfile;
        } else {
            return this.writefile(filepath, localDir);
        }
    }

    protected synchronized File writefile(String filepath, File localDir) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("download {} {}", filepath, localDir.getAbsolutePath());
        }

        File file = new File(localDir, FileUtils.getFilename(filepath));
        FileOutputStream out = new FileOutputStream(file, false);
        try {
            if (this.client.retrieveFile(filepath, out)) {
                return file;
            } else {
                return null;
            }
        } finally {
            out.close();
        }
    }

    public boolean rename(String filepath, String newfilepath) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "rename " + filepath + " " + newfilepath));
        }

        return this.client.rename(filepath, newfilepath);
    }

    public String read(String filepath, String charsetName, int lineno) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "read " + filepath + " " + charsetName + " " + lineno));
        }

        try {
            File file = this.downfile(filepath, FileUtils.getTempDir("ftp", "download", Dates.format17()));
            if (!FileUtils.isFile(file)) {
                return null;
            } else {
                return FileUtils.readline(file, charsetName, lineno);
            }
        } catch (Exception e) {
            throw new OSFileCommandException("read " + filepath + " " + charsetName + " " + lineno, e);
        }
    }

    public boolean write(String filepath, String charsetName, boolean append, CharSequence content) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "write " + filepath + " " + append + " " + content));
        }

        try {
            ApacheFtpFile ftpfile = this.toFtpFile(filepath);
            if (ftpfile.isDirectory()) {
                return false;
            }

            if (append) {
                File file = this.downfile(filepath, FileUtils.getTempDir("ftp", "download", Dates.format17()));
                if (!FileUtils.isFile(file)) {
                    return false;
                } else if (FileUtils.write(file, charsetName, append, content)) {
                    return this.uploadfile(file, FileUtils.getParent(filepath));
                } else {
                    return false;
                }
            } else {
                File parent = FileUtils.getTempDir("ftp", "localfile", Dates.format17());
                File file = FileUtils.createNewFile(parent, FileUtils.getFilename(filepath));
                return FileUtils.write(file, charsetName, append, content) && this.uploadfile(file, FileUtils.getParent(filepath));
            }
        } catch (Exception e) {
            throw new OSFileCommandException("write " + filepath + " " + append + " " + content, e);
        }
    }

    public List<OSFile> find(String filepath, String name, char type, OSFileFilter filter) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "find " + filepath + " -name " + name + " -type " + type));
        }

        try {
            return this.searchfile(filepath, name, type, filter);
        } catch (Exception e) {
            throw new OSFileCommandException("find " + filepath + ", " + name + ", " + type, e);
        }
    }

    protected List<OSFile> searchfile(String filepath, String name, char type, OSFileFilter filter) throws IOException {
        List<OSFile> list = new ArrayList<OSFile>();
        ApacheFtpFile dir = this.toFtpFile(filepath);
        if (dir != null && dir.isDirectory()) {
            List<OSFile> files = this.toFtpFile(filepath).listFiles();
            for (OSFile file : files) {
                if (LinuxLocalOS.KEY_FILENAMES.contains(file.getName())) {
                    continue;
                }

                if (file.isDirectory()) {
                    if (type == 'd' && GPatternExpression.match(file.getName(), name)) {
                        if (filter == null || filter.accept(file)) {
                            list.add(file);
                        }
                    }

                    String dirctory = NetUtils.joinUri(file.getParent(), file.getName());
                    List<OSFile> clist = this.searchfile(dirctory, name, type, filter);
                    list.addAll(clist);
                    continue;
                }

                if (file.isFile()) {
                    if (type == 'd') {
                        continue;
                    } else if (type == 'f') {
                        if (GPatternExpression.match(file.getName(), name)) {
                            if (filter == null || filter.accept(file)) {
                                list.add(file);
                            }
                            continue;
                        }
                    }
                }
            }
        } else {
            if (type == 'f' && GPatternExpression.match(FileUtils.getFilename(filepath), name)) {
                OSFile file = this.toFtpFile(filepath).listFiles().get(0);
                if (filter == null || filter.accept(file)) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public String getCharsetName() {
        return this.client.getControlEncoding();
    }

    public void setCharsetName(String charsetName) {
        this.client.setControlEncoding(charsetName);
        this.params.put("ControlEncoding", charsetName);
    }

    public synchronized void close() {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ftp.apache.standard.output.msg002", this.remoteServerName, "bye"));
        }

        this.params.clear();

        if (this.client != null && this.client.isConnected()) {
            try {
                this.client.logout();
                this.client.disconnect();
            } catch (Exception e) {
                log.error(StringUtils.toString(e));
            }
        }
    }

}