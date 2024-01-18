package icu.etl.os.ssh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import icu.etl.annotation.EasyBean;
import icu.etl.expression.GPatternExpression;
import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.os.OSException;
import icu.etl.os.OSFile;
import icu.etl.os.OSFileCommandException;
import icu.etl.os.OSFileFilter;
import icu.etl.os.OSFtpCommand;
import icu.etl.os.internal.OSFileImpl;
import icu.etl.os.linux.LinuxLocalOS;
import icu.etl.os.linux.Linuxs;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.NetUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * SFTP 协议接口实现
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-08-10
 */
@EasyBean(name = "sftp", description = "jsch-0.1.51")
public class SftpCommand implements OSFtpCommand {
    private final static Log log = LogFactory.getLog(SftpCommand.class);

    /** username@host:port */
    protected String remoteServerName;

    /** JSch */
    protected JSch jsch = new JSch();

    /** 会话连接 */
    private Session session;

    /** 当前已打开的信道 */
    protected JschChannel channel;

    /** 参数集合 */
    protected Properties params;

    /** 字符集 */
    protected String charsetName;

    /**
     * 初始化
     */
    public SftpCommand() {
        this.params = new Properties();
    }

    protected Session getSession() {
        return session;
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    public boolean connect(String host, int port, String username, String password) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg013", "SFTP", username + "@" + host + ":" + port + "?password=" + password));
        }

        try {
            if (this.session != null && this.session.isConnected()) {
                this.session.disconnect();
                this.session = null;
            }

            this.session = this.jsch.getSession(username, host, port);
            this.session.setPassword(password);

            this.setParameters();
            this.session.setConfig(this.params);

            this.session.connect();
            this.openChannelSftp();
            this.remoteServerName = username + "@" + host + ":" + port;
            return true;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("sftp " + username + "@" + host + ":" + port + "?password=" + password, e);
            }
            return false;
        }
    }

    public boolean isConnected() {
        return this.session != null && this.session.isConnected();
    }

    public void setRemoteServerName(String name) {
        this.remoteServerName = name;
    }

    /**
     * 打开信道
     */
    public void openChannelSftp() {
        this.channel = new JschChannel(this.createChannelSftp(), false);
    }

    /**
     * 判断是否已打开信道
     *
     * @return 返回true表示已打开信道 false表示未打开信道
     */
    protected boolean isChannelConnected() {
        try {
            return this.channel != null && this.channel.isConnected();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getLocalizedMessage(), e);
            }
            return false;
        }
    }

    /**
     * 关闭信道
     */
    public void closeChannelSftp() {
        if (this.channel != null) {
            this.channel.closeSftp();
            this.channel = null;
        }
    }

    /**
     * 返回sftp信道
     *
     * @return 信道
     */
    public JschChannel getChannelSftp() {
        if (this.channel == null) {
            return new JschChannel(this.createChannelSftp(), true);
        }
        return this.channel;
    }

    /**
     * 创建一个信道
     *
     * @return 信道
     */
    protected Channel createChannelSftp() {
        try {
            Channel channel = this.session.openChannel("sftp");
            channel.connect();
            return channel;
        } catch (Exception e) {
            throw new OSFileCommandException(e.getLocalizedMessage(), e);
        }
    }

//	public void set(String key, String value) {
//		if (!PARAMS_NAME_SET.contains(key) && !key.startsWith("userauth.")) {
//			throw new UnsupportedOperationException(key + " = " + value);
//		}
//		this.params.put(key, value);
//	}

    /**
     * 设置参数
     */
    protected void setParameters() {
        if (!this.params.containsKey("StrictHostKeyChecking")) {
            this.params.put("StrictHostKeyChecking", "no");
        }
    }

    /**
     * 返回远程服务器上文件信息
     *
     * @param filepath 文件路径
     * @return 文件信息
     */
    protected OSFile toOSFile(String filepath) {
        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            return this.toOSFile(sftp, filepath);
        } finally {
            channel.closeTempChannel();
        }
    }

    protected String toFilepath(String filepath) {
        return StringUtils.defaultString(FileUtils.rtrimFolderSeparator(filepath), "/");
    }

    protected OSFile toOSFile(ChannelSftp sftp, String filepath) {
        try {
            SftpATTRS stat = sftp.stat(this.toFilepath(filepath));
            if (stat == null) {
                return null; // 文件不存在
            } else {
                return this.toOSFile(FileUtils.getFilename(filepath), FileUtils.getParent(filepath), stat, null);
            }
        } catch (SftpException e) {
            if (this.isNoSuchFileError(e)) {
                return null; // 文件不存在
            } else {
                throw new OSException(filepath, e);
            }
        }
    }

    /**
     * 返回远程服务器上文件信息
     *
     * @param filename 文件名
     * @param parent   文件所在目录
     * @param attr     属性
     * @param longname 文件表达式, 如: drwxr-xr-x  50 user  staff   1.6K 12-11_16:30 .
     * @return 文件信息
     */
    private OSFile toOSFile(String filename, String parent, SftpATTRS attr, String longname) {
        OSFileImpl file = new OSFileImpl();
        file.setName(filename);
        file.setParent(parent);
        file.setCreateTime(Dates.parse(attr.getMtimeString()));
        file.setModifyTime(Dates.parse(attr.getAtimeString()));
        file.setDirectory(attr.isDir());
        file.setLink(attr.isLink());
        file.setLength(attr.getSize());
        file.setFile(!attr.isDir() && !attr.isLink());

        String permission = attr.getPermissionsString();
        file.setCanRead(permission.charAt(1) == 'r');
        file.setCanWrite(permission.charAt(2) == 'w');
        file.setCanExecute(permission.charAt(3) == 'x');
        if (StringUtils.isBlank(longname)) {
            file.setLongname(permission + " " + attr.getUId() + " " + attr.getGId() + " " + attr.getSize() + " " + Linuxs.toFileDateFormat(file.getModifyDate()) + " " + file.getName());
        } else {
            file.setLongname(longname);
        }
        return file;
    }

    public void terminate() {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "terminate"));
        }

        if (this.channel != null) {
            this.channel.closeSftp();
            this.channel = null;
        }
    }

    public boolean exists(String filepath) {
        return this.toOSFile(filepath) != null;
    }

    public boolean isFile(String filepath) {
        OSFile file = this.toOSFile(filepath);
        return file != null && file.isFile();
    }

    public boolean isDirectory(String filepath) {
        OSFile file = this.toOSFile(filepath);
        return file != null && file.isDirectory();
    }

    public boolean mkdir(String filepath) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "mkdir " + filepath));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            return this.mkdir(sftp, filepath);
        } catch (Exception e) {
            throw new OSFileCommandException("mkdir " + filepath, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    protected boolean mkdir(ChannelSftp sftp, String filepath) throws SftpException {
        OSFile file = this.toOSFile(sftp, filepath);
        if (file == null) {
            sftp.mkdir(filepath);
            file = this.toOSFile(sftp, filepath);
            return file != null && file.isDirectory();
        } else {
            return file.isDirectory();
        }
    }

    public boolean cd(String filepath) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "cd " + filepath));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            sftp.cd(filepath);
            return true;
        } catch (SftpException e) {
            throw new OSFileCommandException("cd " + filepath, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    public boolean rm(String filepath) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "rm " + filepath));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            return this.rmfile(sftp, filepath);
        } catch (Exception e) {
            throw new OSFileCommandException("rm " + filepath, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    /**
     * 删除远程服务器上的文件
     *
     * @param sftp     SFTP信道
     * @param filepath 文件路径
     * @throws SftpException SFTP错误
     */
    protected boolean rmfile(ChannelSftp sftp, String filepath) throws SftpException {
        OSFile file = this.toOSFile(sftp, filepath);
        if (file == null) {
            return true;
        }

        if (!file.isDirectory()) {
            sftp.rm(filepath);
            return this.toOSFile(sftp, filepath) == null;
        } else {
            List<OSFile> list = this.ls(sftp, filepath);
            for (OSFile osfile : list) {
                if (osfile.isDirectory()) {
                    this.rmfile(sftp, osfile.getAbsolutePath());
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("rm " + osfile.getAbsolutePath());
                    }
                    sftp.rm(osfile.getAbsolutePath());
                }
            }

            sftp.rmdir(filepath);
            return true;
        }
    }

    public String pwd() {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "pwd"));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            return channel.getSftp(this.charsetName).pwd();
        } catch (Exception e) {
            throw new OSFileCommandException("pwd", e);
        } finally {
            channel.closeTempChannel();
        }
    }

    public List<OSFile> ls(String filepath) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "ls " + filepath));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            return this.ls(channel.getSftp(this.charsetName), filepath);
        } catch (Exception e) {
            throw new OSFileCommandException("ls " + filepath, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    protected List<OSFile> ls(ChannelSftp sftp, String filepath) {
        List<OSFile> list = new ArrayList<OSFile>();
        try {
            SftpATTRS stat = sftp.stat(filepath);
            if (stat == null) {
                return list;
            }

            if (stat.isDir()) {
                Vector<?> filelist = sftp.ls(filepath);
                for (Object obj : filelist) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) obj;
                    OSFile file = this.toOSFile(entry.getFilename(), filepath, entry.getAttrs(), entry.getLongname());
                    if (!LinuxLocalOS.KEY_FILENAMES.contains(file.getName())) {
                        list.add(file);
                    }
                }
            } else {
                filepath = this.toFilepath(filepath);
                OSFile file = this.toOSFile(FileUtils.getFilename(filepath), FileUtils.getParent(filepath), stat, null);
                if (!LinuxLocalOS.KEY_FILENAMES.contains(file.getName())) {
                    list.add(file);
                }
            }
            return list;
        } catch (SftpException e) {
            if (this.isNoSuchFileError(e)) {
                return list;
            } else {
                throw new OSFileCommandException("ls " + filepath, e);
            }
        }
    }

    protected boolean isNoSuchFileError(SftpException e) {
        return e.getMessage().contains("No such file");
    }

    public synchronized boolean upload(InputStream in, String remote) {
        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            sftp.put(in, remote, null, ChannelSftp.OVERWRITE);
            return true;
        } catch (Exception e) {
            throw new OSFileCommandException("upload " + in + " " + remote, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    public synchronized boolean download(String remote, OutputStream out) {
        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            sftp.get(remote, out);
            return true;
        } catch (Exception e) {
            throw new OSFileCommandException("download " + remote + " " + out, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    public boolean upload(File localFile, String remoteDir) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "upload " + localFile + " " + remoteDir));
        }

        if (!FileUtils.exists(localFile)) {
            return false;
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            return this.uploadfile(sftp, localFile, remoteDir, null, ChannelSftp.OVERWRITE);
        } catch (Exception e) {
            throw new OSFileCommandException("upload " + localFile + " " + remoteDir, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    /**
     * 上传文件/目录
     *
     * @param sftp      信道
     * @param localfile 本地文件/目录
     * @param filepath  远程服务器上文件路径
     * @param monitor   监视器
     * @param mode      {@linkplain ChannelSftp#RESUME} Recovery mode, if the file has been partially interrupted, the next time the same file is transferred, it will resume from the place where the last interruption <br>
     *                  {@linkplain ChannelSftp#APPEND} Append mode, if the target file already exists, the transferred file will be appended after the target file <br>
     *                  {@linkplain ChannelSftp#OVERWRITE} Full overwrite mode, if the file already exists, the transfer file will completely overwrite the target file <br>
     * @throws SftpException SFTP错误
     */
    protected boolean uploadfile(ChannelSftp sftp, File localfile, String filepath, SftpProgressMonitor monitor, int mode) throws SftpException {
        if (localfile.isDirectory()) {
            String remotedir = FileUtils.rtrimFolderSeparator(filepath) + "/" + localfile.getName();
            OSFile file = this.toOSFile(sftp, remotedir);
            if (file == null) {
                sftp.mkdir(remotedir);
            } else if (!file.isDirectory()) {
                sftp.rm(remotedir);
                sftp.mkdir(remotedir);
            }

            // 上传目录中的文件
            boolean success = true;
            File[] files = FileUtils.array(localfile.listFiles());
            for (File cfile : files) {
                if (log.isDebugEnabled()) {
                    log.debug("uploadfile " + cfile + " " + remotedir + " ..");
                }

                if (!this.uploadfile(sftp, cfile, remotedir, monitor, mode)) {
                    success = false;
                }
            }
            return success;
        } else {
            sftp.put(localfile.getAbsolutePath(), filepath, monitor, mode);
            return true;
        }
    }

    public boolean rename(String filepath, String dest) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "rename " + filepath + " " + dest));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            if (this.toOSFile(sftp, filepath) == null || this.toOSFile(sftp, dest) != null) {
                return false;
            } else {
                sftp.rename(filepath, dest);
                return this.toOSFile(sftp, filepath) == null && this.toOSFile(sftp, dest) != null;
            }
        } catch (Exception e) {
            throw new OSFileCommandException("rename " + filepath + " " + dest, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    public File download(String filepath, File localDir) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "download " + filepath + " " + localDir));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            return this.downfile(sftp, filepath, localDir);
        } catch (Exception e) {
            throw new OSFileCommandException("download " + filepath + " " + localDir, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    protected File downfile(ChannelSftp sftp, String filepath, File localDir) throws SftpException, IOException {
        OSFile file = this.toOSFile(sftp, filepath);
        if (file == null) {
            return null;
        }

        if (file.isDirectory()) {
            if (!FileUtils.createDirectory(localDir)) {
                return null;
            }

            File downfile = new File(localDir, file.getName());
            if (!FileUtils.createDirectory(downfile)) {
                return null;
            }

            boolean success = true;
            List<OSFile> filelist = this.ls(sftp, filepath);
            for (OSFile osfile : filelist) {
                if (osfile.isDirectory()) {
                    if (this.downfile(sftp, osfile.getAbsolutePath(), downfile) == null) {
                        success = false;
                    }
                } else {
                    this.download(sftp, osfile.getAbsolutePath(), new File(downfile, osfile.getName()));
                }
            }
            return success ? downfile : null;
        } else {
            if (!localDir.exists()) {
                FileUtils.createDirectory(localDir); // 无需判断目录是否创建成功
            }

            File localfile = localDir.isDirectory() ? new File(localDir, file.getName()) : localDir;
            this.download(sftp, filepath, localfile);
            return localfile;
        }
    }

    /**
     * 从SFTP信道下载一个文件
     *
     * @param sftp           sftp信道
     * @param remotefilepath 远程文件路径
     * @param localfile      本地文件
     * @throws SftpException SFTP错误
     * @throws IOException   访问文件错误
     */
    protected void download(ChannelSftp sftp, String remotefilepath, File localfile) throws SftpException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("downfile " + remotefilepath + " " + localfile.getAbsolutePath());
        }

        InputStream in = sftp.get(remotefilepath);
        FileOutputStream out = new FileOutputStream(localfile, false);
        IO.write(in, out);
    }

    public String read(String filepath, String charsetName, int lineno) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "read " + filepath + " " + charsetName + " " + lineno));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            OSFile osfile = this.toOSFile(sftp, filepath);
            if (osfile == null || osfile.isDirectory()) {
                return null;
            }

            File downfile = this.downfile(sftp, filepath, FileUtils.getTempDir("sftp", "download", Dates.format17()));
            if (!FileUtils.isFile(downfile)) {
                return null;
            }

            try {
                return FileUtils.readline(downfile, charsetName, lineno);
            } finally {
                FileUtils.delete(downfile);
            }
        } catch (Exception e) {
            throw new OSFileCommandException("read " + filepath + " " + charsetName + " " + lineno, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    public boolean write(String filepath, String charsetName, boolean append, CharSequence content) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "write " + filepath + " " + append + " " + content));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            if (append) {
                File downfile = this.downfile(sftp, filepath, FileUtils.getTempDir("sftp", "download", Dates.format17()));
                if (!FileUtils.isFile(downfile)) {
                    return false;
                }

                try {
                    if (FileUtils.write(downfile, charsetName, append, content)) {
                        return this.uploadfile(sftp, downfile, FileUtils.getParent(filepath), null, ChannelSftp.OVERWRITE);
                    }
                } finally {
                    FileUtils.delete(downfile);
                }

                return false;
            } else {
                File parent = FileUtils.getTempDir("sftp", "localfile", Dates.format17());
                File file = FileUtils.createNewFile(parent, FileUtils.getFilename(filepath));
                return FileUtils.write(file, charsetName, append, content) && this.uploadfile(sftp, file, FileUtils.getParent(filepath), null, ChannelSftp.OVERWRITE);
            }
        } catch (Exception e) {
            throw new OSFileCommandException("write " + filepath + " " + append + " " + content, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    public boolean copy(String filepath, String directory) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "copy " + filepath + " " + directory));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            OSFile file = this.toOSFile(sftp, directory);
            if (file == null) {
                if (!this.mkdir(sftp, directory)) {
                    return false;
                }
            } else if (!file.isDirectory()) {
                return false;
            }

            File downfile = this.downfile(sftp, filepath, FileUtils.getTempDir("sftp", "download", Dates.format17()));
            if (downfile == null) {
                return false;
            }

            try {
                return this.uploadfile(sftp, downfile, directory, null, ChannelSftp.OVERWRITE);
            } finally {
                FileUtils.delete(downfile);
            }
        } catch (Exception e) {
            throw new OSFileCommandException("copy " + filepath + " " + directory, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    public List<OSFile> find(String filepath, String name, char type, OSFileFilter filter) {
        if (log.isDebugEnabled()) {
            log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "find " + filepath + " " + name + " " + type + " " + filter));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            return this.find(sftp, filepath, name, type, filter);
        } catch (Exception e) {
            throw new OSFileCommandException("find " + filepath + " " + name + " " + type + " " + filter, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    protected List<OSFile> find(ChannelSftp sftp, String filepath, String name, char type, OSFileFilter filter) throws SftpException {
        List<OSFile> list = new ArrayList<OSFile>();
        if (this.isDirectory(filepath)) {
            List<OSFile> files = this.ls(sftp, filepath);
            for (OSFile file : files) {
                if (log.isDebugEnabled()) {
                    log.debug("find " + file.getParent() + "/" + file.getName() + " -> " + name);
                }

                if (file.isDirectory()) {
                    if (type == 'd' && GPatternExpression.match(file.getName(), name)) {
                        if (filter == null || filter.accept(file)) {
                            list.add(file);
                        }
                    }

                    String dirctory = NetUtils.joinUri(file.getParent(), file.getName());
                    list.addAll(this.find(sftp, dirctory, name, type, filter));
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
                OSFile file = this.toOSFile(sftp, filepath);
                if (filter == null || filter.accept(file)) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public String getCharsetName() {
        return this.charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public void closeSession() {
        if (this.session != null) {
            if (this.session.isConnected()) {
                this.session.disconnect();
            }
            this.session = null;
        }
    }

    public void close() {
        try {
            if (this.channel != null) {
                if (log.isDebugEnabled()) {
                    log.debug(ResourcesUtils.getMessage("ssh2.jsch.standard.output.msg012", "bye"));
                }
                this.channel.closeSftp();
            }
            this.channel = null;
            this.remoteServerName = null;
        } finally {
            this.closeSession();
        }
    }
}
