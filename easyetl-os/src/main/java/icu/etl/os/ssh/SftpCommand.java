package icu.etl.os.ssh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.expression.GPatternExpression;
import icu.etl.log.STD;
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
import icu.jsch.Channel;
import icu.jsch.ChannelExec;
import icu.jsch.ChannelSftp;
import icu.jsch.JSch;
import icu.jsch.Session;
import icu.jsch.SftpATTRS;
import icu.jsch.SftpException;
import icu.jsch.SftpProgressMonitor;

/**
 * SFTP 协议接口实现
 *
 * @author jeremy8551@qq.com
 * @createtime 2018-08-10
 */
@EasyBeanClass(kind = "sftp", mode = "java", major = "0", minor = "1", description = "jsch-0.1.51", type = OSFtpCommand.class)
public class SftpCommand implements OSFtpCommand {

    /** username@host:port */
    protected String remoteServerName;

    /** JSch component object */
    protected JSch jsch = new JSch();

    /** ssh connection transaction */
    private Session session;

    /** Currently open channel */
    protected JschChannel channel;

    /** Parameter set */
    protected Properties params = new Properties();

    protected String charsetName;

    /** Parameter name collection */
//	private final static HashSet<String> PARAMS_NAME_SET = new HashSet<String>(Arrays.toList("lang.s2c", "lang.c2s", "random", "CheckCiphers", "kex", "CheckKexes", "server_host_key", "cipher.c2s", "cipher.s2c", "mac.c2s", "mac.s2c", "compression.c2s", "compression.s2c", "compression_level", "StrictHostKeyChecking", "HashKnownHosts", "PreferredAuthentications", "MaxAuthTries", "ClearAllForwardings", "HostKeyAlias", "UserKnownHostsFile", "IdentityFile", "ServerAliveInterval"));

    /**
     * initialization
     */
    public SftpCommand() {
    }

    protected Session getSession() {
        return session;
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    public boolean connect(String host, int port, String username, String password) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(13, "SFTP", username + "@" + host + ":" + port + "?password=" + password));
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
            if (STD.out.isErrorEnabled()) {
                STD.out.error("sftp " + username + "@" + host + ":" + port + "?password=" + password, e);
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
     * Open a channel
     */
    public void openChannelSftp() {
        this.channel = new JschChannel(this.createChannelSftp(), false);
    }

    protected boolean isChannelConnected() {
        try {
            return this.channel != null && this.channel.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * close a channel
     */
    public void closeChannelSftp() {
        if (this.channel != null) {
            this.channel.closeSftp();
            this.channel = null;
        }
    }

    /**
     * Returns the current sftp channel
     *
     * @return
     */
    public JschChannel getChannelSftp() {
        if (this.channel == null) {
            return new JschChannel(this.createChannelSftp(), true);
        }
        return this.channel;
    }

    /**
     * Create and open a channel
     *
     * @return
     */
    protected Channel createChannelSftp() {
        try {
            Channel channel = this.session.openChannel("sftp");
            channel.connect();
            return channel;
        } catch (Exception e) {
            throw new OSFileCommandException("connect channel fail!", e);
        }
    }

//	public void set(String key, String value) {
//		if (!PARAMS_NAME_SET.contains(key) && !key.startsWith("userauth.")) {
//			throw new UnsupportedOperationException(key + " = " + value);
//		}
//		this.params.put(key, value);
//	}

    /**
     * Setting parameters
     */
    protected void setParameters() {
        if (!this.params.containsKey("StrictHostKeyChecking")) {
            this.params.put("StrictHostKeyChecking", "no");
        }
    }

    /**
     * Return the path information of the remote server
     *
     * @param filepath
     * @return
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
                return null;
            } else {
                return this.toOSFile(FileUtils.getFilename(filepath), FileUtils.getParent(filepath), stat, null);
            }
        } catch (SftpException e) {
            if (this.isNoSuchFileError(e)) {
                return null;
            } else {
                throw new OSException(filepath, e);
            }
        }
    }

    /**
     * Return the path information of the remote server
     *
     * @param filename
     * @param parent
     * @param attr
     * @param longname
     * @return
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
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "terminate"));
        }

        if (this.channel != null) {
            this.channel.closeSftp();
        }
        this.channel = null;
    }

    public boolean exists(String filepath) {
        try {
            return this.toOSFile(filepath) != null;
        } catch (Exception e) {
            return false;
        }
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
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "mkdir " + filepath));
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
        } else if (file.isDirectory()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean cd(String filepath) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "cd " + filepath));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            sftp.cd(filepath);
            return true;
        } catch (SftpException e) {
            if (STD.out.isErrorEnabled()) {
                STD.out.error("cd " + filepath + " fail!", e);
            }
            return false;
        } finally {
            channel.closeTempChannel();
        }
    }

    public boolean rm(String filepath) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "rm " + filepath));
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
     * Delete the entire directory
     *
     * @param sftp
     * @param filepath Remote server directory
     * @throws SftpException
     */
    protected boolean rmfile(ChannelSftp sftp, String filepath) throws SftpException {
        OSFile file = this.toOSFile(sftp, filepath);
        if (file == null) {
            return true;
        } else if (!file.isDirectory()) {
            sftp.rm(filepath);
            return this.toOSFile(sftp, filepath) == null;
        } else {
            List<OSFile> list = this.ls(sftp, filepath);
            for (OSFile cfile : list) {
                if (cfile.isDirectory()) {
                    this.rmfile(sftp, cfile.getAbsolutePath());
                } else {
                    if (STD.out.isDebugEnabled()) {
                        STD.out.debug("rm " + cfile.getAbsolutePath());
                    }
                    sftp.rm(cfile.getAbsolutePath());
                }
            }

            sftp.rmdir(filepath);
            return true;
        }
    }

    public String pwd() {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "pwd"));
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
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "ls " + filepath));
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
                for (Iterator<?> it = sftp.ls(filepath).iterator(); it.hasNext(); ) {
                    ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) it.next();
                    OSFile file = this.toOSFile(entry.getFilename(), filepath, entry.getAttrs(), entry.getLongname());
                    if (!LinuxLocalOS.KEY_FILENAMES.contains(file.getName())) {
                        if (STD.out.isDebugEnabled()) {
                            STD.out.debug("ls result: " + file.toString());
                        }
                        list.add(file);
                    }
                }
            } else {
                filepath = this.toFilepath(filepath);
                OSFile file = this.toOSFile(FileUtils.getFilename(filepath), FileUtils.getParent(filepath), stat, null);
                if (!LinuxLocalOS.KEY_FILENAMES.contains(file.getName())) {
                    if (STD.out.isDebugEnabled()) {
                        STD.out.debug("ls result: " + file.toString());
                    }
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
        return e.getMessage().indexOf("No such file") != -1;
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
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "upload " + localFile + " " + remoteDir));
        }

        if (localFile == null || !localFile.exists()) {
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
     * Upload a directory or file to the server
     *
     * @param sftp      channelSftp
     * @param localfile Upload files or directories
     * @param filepath  File or directory of remote server
     * @param monitor   Upload file progress monitor
     * @param mode      {@linkplain ChannelSftp#RESUME} Recovery mode, if the file has been partially interrupted, the next time the same file is transferred, it will resume from the place where the last interruption <br>
     *                  {@linkplain ChannelSftp#APPEND} Append mode, if the target file already exists, the transferred file will be appended after the target file <br>
     *                  {@linkplain ChannelSftp#OVERWRITE} Full overwrite mode, if the file already exists, the transfer file will completely overwrite the target file <br>
     * @throws SftpException
     */
    protected boolean uploadfile(ChannelSftp sftp, File localfile, String filepath, SftpProgressMonitor monitor, int mode) throws SftpException {
        if (localfile.isDirectory()) {
            String remotedir = FileUtils.rtrimFolderSeparator(filepath) + "/" + localfile.getName();
            OSFile file = this.toOSFile(sftp, remotedir);
            if (file == null) {
                if (STD.out.isDebugEnabled()) {
                    STD.out.debug("uploadfile & mkdir " + remotedir);
                }
                sftp.mkdir(remotedir);
            } else if (!file.isDirectory()) {
                if (STD.out.isDebugEnabled()) {
                    STD.out.debug("uploadfile & rm & mkdir " + remotedir);
                }
                sftp.rm(remotedir);
                sftp.mkdir(remotedir);
            }

            /**
             * Loop through sub-files in the directory and upload
             */
            boolean flag = true;
            File[] files = FileUtils.array(localfile.listFiles());
            for (File cfile : files) {
                if (STD.out.isDebugEnabled()) {
                    STD.out.debug("uploadfile " + cfile + " " + remotedir + " ..");
                }
                if (!this.uploadfile(sftp, cfile, remotedir, monitor, mode)) {
                    flag = false;
                }
            }
            return flag;
        } else {
            sftp.put(localfile.getAbsolutePath(), filepath, monitor, mode);
            return true;
        }
    }

    public boolean rename(String filepath, String newfilepath) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "rename " + filepath + " " + newfilepath));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            if (this.toOSFile(sftp, filepath) == null || this.toOSFile(sftp, newfilepath) != null) {
                return false;
            } else {
                sftp.rename(filepath, newfilepath);
                return this.toOSFile(sftp, filepath) == null && this.toOSFile(sftp, newfilepath) != null;
            }
        } catch (Exception e) {
            throw new OSFileCommandException("rename " + filepath + " " + newfilepath, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    public File download(String filepath, File localDir) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "download " + filepath + " " + localDir));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            return this.downfile(sftp, filepath, localDir);
        } catch (Exception e) {
            throw new OSFileCommandException("downfile " + filepath + " " + localDir, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    protected File downfile(ChannelSftp sftp, String filepath, File localDir) throws SftpException, IOException {
        OSFile file = this.toOSFile(sftp, filepath);
        if (file == null) {
            return null;
        } else if (!file.isDirectory()) {
            if (!localDir.exists()) {
                FileUtils.createDirectory(localDir);
            }
            File localfile = localDir.isDirectory() ? new File(localDir, file.getName()) : localDir;
            return this.writefile(sftp, filepath, localfile);
        } else {
            if (!localDir.exists()) {
                FileUtils.createDirectory(localDir);
            }
            if (localDir.isFile()) {
                return null;
            }

            File localfile = new File(localDir, file.getName());
            if (!FileUtils.createDirectory(localfile)) {
                return null;
            }

            List<OSFile> list = this.ls(sftp, filepath);
            for (OSFile cfile : list) {
                if (cfile.isDirectory()) {
                    if (STD.out.isDebugEnabled()) {
                        STD.out.debug("downfile " + cfile.getAbsolutePath() + " " + localfile.getAbsolutePath());
                    }

                    if (this.downfile(sftp, cfile.getAbsolutePath(), localfile) == null) {
                        return null;
                    }
                } else {
                    if (STD.out.isDebugEnabled()) {
                        STD.out.debug("downfile " + cfile.getAbsolutePath() + " " + localfile.getAbsolutePath());
                    }
                    this.writefile(sftp, cfile.getAbsolutePath(), new File(localfile, cfile.getName()));
                }
            }

            return localfile;
        }
    }

    /**
     * Download a single file from the sftp channel
     *
     * @param sftp           sftp channel
     * @param remotefilepath Remote server file path
     * @param localfile      Local file
     * @throws SftpException
     * @throws IOException
     */
    protected File writefile(ChannelSftp sftp, String remotefilepath, File localfile) throws SftpException, IOException {
        byte[] array = new byte[1024];
        InputStream in = null;
        FileOutputStream out = new FileOutputStream(localfile, false);
        try {
            in = sftp.get(remotefilepath);
            int len = -1;
            while ((len = in.read(array)) != -1) {
                out.write(array, 0, len);
            }
            out.flush();
            return localfile;
        } finally {
            IO.close(out, in);
        }
    }

    public String read(String filepath, String charsetName, int lineno) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "read " + filepath + " " + charsetName + " " + lineno));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            OSFile osFile = this.toOSFile(sftp, filepath);
            if (osFile == null || osFile.isDirectory()) {
                return null;
            }

            File file = this.downfile(sftp, filepath, FileUtils.getTempDir(SftpCommand.class));
            if (file == null || !file.exists() || !file.isFile()) {
                return null;
            } else {
                return FileUtils.readline(file, charsetName, lineno);
            }
        } catch (Exception e) {
            throw new OSFileCommandException("read " + filepath + " " + charsetName + " " + lineno, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    public boolean write(String filepath, String charsetName, boolean append, CharSequence content) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "write " + filepath + " " + append + " " + content));
        }

        JschChannel channel = this.getChannelSftp();
        try {
            ChannelSftp sftp = channel.getSftp(this.charsetName);
            if (append) {
                File file = this.downfile(sftp, filepath, FileUtils.getTempDir(SftpCommand.class));
                if (file == null || !file.exists() || !file.isFile()) {
                    return false;
                } else if (FileUtils.write(file, charsetName, append, content)) {
                    return this.uploadfile(sftp, file, FileUtils.getParent(filepath), null, ChannelSftp.OVERWRITE);
                } else {
                    return false;
                }
            } else {
                File file = new File(FileUtils.getTempDir(SftpCommand.class), FileUtils.getFilename(filepath));
                return FileUtils.write(file, charsetName, append, content) //
                        && this.uploadfile(sftp, file, FileUtils.getParent(filepath), null, ChannelSftp.OVERWRITE) //
                        ;
            }
        } catch (Exception e) {
            throw new OSFileCommandException("write " + filepath + " " + append + " " + content, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    public boolean copy(String filepath, String directory) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "copy " + filepath + " " + directory));
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

            File localfile = this.downfile(sftp, filepath, FileUtils.getTempDir(SftpCommand.class));
            try {
                return this.uploadfile(sftp, localfile, directory, null, ChannelSftp.OVERWRITE);
            } finally {
                localfile.delete();
            }
        } catch (Exception e) {
            throw new OSFileCommandException("copy " + filepath + " " + directory, e);
        } finally {
            channel.closeTempChannel();
        }
    }

    public List<OSFile> find(String filepath, String name, char type, OSFileFilter filter) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "find " + filepath + " " + name + " " + type + " " + filter));
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
                if (STD.out.isDebugEnabled()) {
                    STD.out.debug("find " + file.getParent() + "/" + file.getName() + " -> " + name);
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
        if (STD.out.isDebugEnabled()) {
            STD.out.debug("set " + SftpCommand.class.getSimpleName() + " charset = " + charsetName);
        }

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
                if (STD.out.isDebugEnabled()) {
                    STD.out.debug(ResourcesUtils.getSSH2JschMessage(12, this.remoteServerName, "bye"));
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

class JschChannel {
    protected Channel sftp;
    protected boolean isTmp;
    private String charset;

    public JschChannel(Channel sftp, boolean isTmp) {
        super();
        this.sftp = sftp;
        this.isTmp = isTmp;
    }

    public ChannelSftp getSftp(String charsetName) {
        ChannelSftp channel = (ChannelSftp) this.sftp;
        if (channel != null && StringUtils.isNotBlank(charsetName) && !charsetName.equals(this.charset)) {
            try {
                channel.setFilenameEncoding(charsetName);
                this.charset = charsetName;
            } catch (SftpException e) {
                throw new OSFileCommandException(charsetName, e);
            }
        }
        return channel;
    }

    public boolean isConnected() {
        return this.sftp != null && this.sftp.isConnected();
    }

    public ChannelExec getExec() {
        return (ChannelExec) sftp;
    }

    public void closeSftp() {
        if (this.sftp != null) {
            this.sftp.disconnect();
            this.sftp = null;
        }
    }

    /**
     * If it is a temporary channel, close the channel
     */
    public void closeTempChannel() {
        if (this.isTmp) {
            this.closeSftp();
        }
    }

}
