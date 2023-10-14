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
import java.util.HashSet;
import java.util.List;

import icu.apache.net.ftp.FTPClient;
import icu.apache.net.ftp.parser.FTPFileEntryParserFactory;
import icu.etl.annotation.EasyBeanClass;
import icu.etl.expression.GPatternExpression;
import icu.etl.io.BufferedLineReader;
import icu.etl.log.STD;
import icu.etl.os.OSFile;
import icu.etl.os.OSFileCommandException;
import icu.etl.os.OSFileFilter;
import icu.etl.os.OSFtpCommand;
import icu.etl.os.internal.OSFileImpl;
import icu.etl.os.linux.LinuxLocalOS;
import icu.etl.util.ArrayUtils;
import icu.etl.util.ClassUtils;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.NetUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * FTP协议的实现类
 */
@EasyBeanClass(kind = "ftp", mode = "", major = "2", minor = "2", type = OSFtpCommand.class)
public class FtpCommand implements OSFtpCommand {

    /** parameters name */
    public final static HashSet<String> PARAM_NAME_LIST = new HashSet<String>(ArrayUtils.asList("DataTimeout", "ControlEncoding", "BufferSize", "FileType", "ParserFactory", "FileStructure", "FileTransferMode", "RemoteVerificationEnabled", "RestartOffset"));

    /** ftp 客户端组件 */
    private FTPClient client;

    /** ftp 命令参数集合 */
    protected HashMap<String, String> params;

    /** 远程服务器上文件路径分隔符 */
    protected char folderSeperator;

    /** username@host:port */
    protected String remoteServerName;

    public FtpCommand() {
        this.client = new FTPClient();
        this.params = new HashMap<String, String>();
        this.folderSeperator = '/';
    }

    public synchronized boolean connect(String host, int port, String username, String password) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(1, username + "@" + host + ":" + port + "?password=" + password));
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
            if (STD.out.isErrorEnabled()) {
                STD.out.error("ftp " + username + "@" + host + ":" + port + "?password=" + password + " fail!", e);
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
            this.client.setParserFactory((FTPFileEntryParserFactory) ClassUtils.newInstance(this.params.get("ParserFactory")));
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
     * Return remote path information
     *
     * @param filepath
     * @return
     * @throws IOException
     */
    protected synchronized ApacheFtpFile toFtpFile(String filepath) throws IOException {
        String status = this.client.getStatus(filepath);
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(status);
        }

        BufferedLineReader in = new BufferedLineReader(status);
        try {
            String line = in.next();
            if (line != null && !line.endsWith("Status follows:")) {
                throw new OSFileCommandException(in.toString());
            }

            String parent = FileUtils.getParent(filepath);
            int count = 0;
            boolean isDir = false;
            List<OSFile> list = new ArrayList<OSFile>();
            String errorline = null;
            int errorlineno = -1;
            while ((line = in.next()) != null && !line.endsWith("End of status")) {
                count++;
                errorline = line;
                String[] array = StringUtils.splitByBlank(line);
                if (array.length == 1) {
                    errorline = line;
                    errorlineno = count;
                    continue;
                }

                OSFileImpl file = this.toFtpFile(line, array);
                if (LinuxLocalOS.KEY_FILENAMES.contains(file.getName())) {
                    isDir = true;
                    continue;
                }
                file.setParent(parent);
                file.setLongname(line);
                list.add(file);
                if (STD.out.isDebugEnabled()) {
                    STD.out.debug(file.toString());
                }
            }

            if (errorlineno != -1 && errorlineno != count) {
                throw new IOException("lineno: " + errorlineno + ", content: " + errorline);
            }

            if (count == 0) {
                return null;
            }

            if (isDir) {
                for (OSFile file : list) {
                    ((OSFileImpl) file).setParent(filepath);
                }
            }

            String[] array = StringUtils.split(filepath, FileUtils.pathSeparators, false);
            int index = StringUtils.lastIndexOfNotBlank(array);
            String name = (index == -1 ? "" : array[index]);
            return new ApacheFtpFile(name, isDir, list);
        } finally {
            in.close();
        }
    }

    protected OSFileImpl toFtpFile(String line, String[] array) {
        OSFileImpl file = new OSFileImpl();
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
     * Format time
     *
     * @param array
     * @return
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
                buf.append(time + ":00");
            }
        } else {
            buf.append(Dates.getYear(new Date()));
        }
        return Dates.parse(buf);
    }

    public void terminate() {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "terminate"));
        }

        try {
            this.client.abort();
        } catch (Exception e) {
            throw new OSFileCommandException("terminate", e);
        }
    }

    public synchronized boolean exists(String filepath) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "exists " + filepath));
        }

        try {
            return this.toFtpFile(filepath) != null;
        } catch (Exception e) {
            throw new OSFileCommandException("exists " + filepath, e);
        }
    }

    public synchronized boolean isFile(String filepath) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "isFile " + filepath));
        }

        try {
            ApacheFtpFile file = this.toFtpFile(filepath);
            return file != null && !file.isDirectory();
        } catch (Exception e) {
            throw new OSFileCommandException("isFile " + filepath, e);
        }
    }

    public synchronized boolean isDirectory(String filepath) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "isDirectory " + filepath));
        }

        try {
            ApacheFtpFile file = this.toFtpFile(filepath);
            return file != null && file.isDirectory();
        } catch (Exception e) {
            throw new OSFileCommandException("isDirectory " + filepath, e);
        }
    }

    public synchronized boolean mkdir(String filepath) throws IOException {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "mkdir " + filepath));
        }

        return this.client.makeDirectory(filepath);
    }

    public synchronized boolean cd(String filepath) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "cd " + filepath));
        }

        try {
            return this.client.changeWorkingDirectory(filepath);
        } catch (Exception e) {
            throw new OSFileCommandException("cd " + filepath, e);
        }
    }

    public synchronized boolean rm(String filepath) throws IOException {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "rm " + filepath));
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
            if (STD.out.isDebugEnabled()) {
                STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "delete remote directory " + remoteDir + " -> " + success));
            }
            return success;
        } else {
            boolean success = this.client.deleteFile(filepath);
            if (STD.out.isDebugEnabled()) {
                STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "delete remote file " + filepath + " -> " + success));
            }
            return success;
        }

    }

    public synchronized String pwd() {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "pwd"));
        }

        try {
            return this.client.printWorkingDirectory();
        } catch (Exception e) {
            throw new OSFileCommandException("pwd", e);
        }
    }

    public synchronized List<OSFile> ls(String filepath) throws IOException {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "ls " + filepath));
        }

        ApacheFtpFile file = this.toFtpFile(filepath);
        return file == null ? new ArrayList<OSFile>(0) : file.listFiles();
    }

    public boolean copy(String filepath, String directory) {
        Ensure.isTrue(!filepath.equals(directory), filepath, directory);
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "copy " + filepath + " " + directory));
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

            File localfile = this.downfile(filepath, FileUtils.getTempDir(FtpCommand.class));
            try {
                return this.uploadfile(localfile, directory);
            } finally {
                localfile.delete();
            }
        } catch (Exception e) {
            throw new OSFileCommandException("copy " + filepath + " " + directory, e);
        }
    }

    public synchronized boolean upload(File localFile, String remoteDir) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "put " + localFile.getAbsolutePath() + " " + remoteDir));
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
     * Upload localFile to the remote server directory
     *
     * @param localFile
     * @param remoteDir
     * @return
     * @throws IOException
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
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "get " + filepath + " " + localFile.getAbsolutePath()));
        }

        try {
            return this.downfile(filepath, localFile);
        } catch (Exception e) {
            throw new OSFileCommandException("get " + filepath + " " + localFile, e);
        }
    }

    protected synchronized File downfile(String filepath, File localFile) throws IOException {
        filepath = FileUtils.rtrimFolderSeparator(filepath);
        ApacheFtpFile remotefile = this.toFtpFile(filepath);
        if (remotefile == null) {
            if (STD.out.isDebugEnabled()) {
                STD.out.debug("downfile " + filepath + " fail: file not exists!");
            }
            return null;
        } else if (remotefile.isDirectory()) {
            String newfilepath = FileUtils.rtrimFolderSeparator(filepath);
            File localfile = new File(localFile, FileUtils.getFilename(newfilepath));
            if (!FileUtils.createDirectory(localfile)) {
                if (STD.out.isDebugEnabled()) {
                    STD.out.debug("downfile " + filepath + " fail: can not create dir " + localfile);
                }
                return null;
            }

            List<OSFile> listFiles = remotefile.listFiles();
            for (OSFile file : listFiles) {
                if (file.isDirectory()) {
                    if (this.downfile(newfilepath + this.folderSeperator + file.getName(), localfile) == null) {
                        return null;
                    }
                } else {
                    if (this.writefile(newfilepath + this.folderSeperator + file.getName(), localfile) == null) {
                        return null;
                    }
                }
            }
            return localfile;
        } else {
            return this.writefile(filepath, localFile);
        }
    }

    protected synchronized File writefile(String filepath, File localfile) throws IOException {
        File file = new File(localfile, FileUtils.getFilename(filepath));
        FileOutputStream fos = new FileOutputStream(file, false);
        try {
            if (this.client.retrieveFile(filepath, fos)) {
                return file;
            } else {
                return null;
            }
        } finally {
            fos.close();
        }
    }

    public boolean rename(String filepath, String newfilepath) throws IOException {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "rename " + filepath + " " + newfilepath));
        }

        return this.client.rename(filepath, newfilepath);
    }

    public String read(String filepath, String charsetName, int lineno) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "read " + filepath + " " + charsetName + " " + lineno));
        }

        try {
            File file = this.downfile(filepath, FileUtils.getTempDir(FtpCommand.class));
            if (file == null || !file.exists() || !file.isFile()) {
                return null;
            } else {
                return FileUtils.readline(file, charsetName, lineno);
            }
        } catch (Exception e) {
            throw new OSFileCommandException("read " + filepath + " " + charsetName + " " + lineno, e);
        }
    }

    public boolean write(String filepath, String charsetName, boolean append, CharSequence content) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "write " + filepath + " " + append + " " + content));
        }

        try {
            ApacheFtpFile ftpfile = this.toFtpFile(filepath);
            if (ftpfile.isDirectory()) {
                return false;
            }

            if (append) {
                File file = this.downfile(filepath, FileUtils.getTempDir(FtpCommand.class));
                if (file == null || !file.exists() || !file.isFile()) {
                    return false;
                } else if (FileUtils.write(file, charsetName, append, content)) {
                    return this.uploadfile(file, FileUtils.getParent(filepath));
                } else {
                    return false;
                }
            } else {
                File file = new File(FileUtils.getTempDir(FtpCommand.class), FileUtils.getFilename(filepath));
                return FileUtils.write(file, charsetName, append, content) && this.uploadfile(file, FileUtils.getParent(filepath));
            }
        } catch (Exception e) {
            throw new OSFileCommandException("write " + filepath + " " + append + " " + content, e);
        }
    }

    public List<OSFile> find(String filepath, String name, char type, OSFileFilter filter) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "find " + filepath + " -name " + name + " -type " + type));
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
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getFtpApacheMessage(2, this.remoteServerName, "bye"));
        }

        this.params.clear();

        if (this.client != null && this.client.isConnected()) {
            try {
                this.client.logout();
                this.client.disconnect();
            } catch (Exception e) {
                STD.out.error(StringUtils.toString(e));
            }
        }
    }

}

class ApacheFtpFile {
    private String path;
    private List<OSFile> list;
    private boolean isDir;

    public ApacheFtpFile(String path, boolean isDir, List<OSFile> list) {
        this.path = path;
        this.list = list;
        this.isDir = isDir;
    }

    public List<OSFile> listFiles() {
        return list;
    }

    public boolean isDirectory() {
        return this.isDir;
    }

    public String getPath() {
        return path;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(path);
        buf.append(" ");
        buf.append(StringUtils.toString(list));
        buf.append("}");
        return super.toString();
    }
}
