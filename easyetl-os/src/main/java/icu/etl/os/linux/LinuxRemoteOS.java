package icu.etl.os.linux;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import icu.etl.annotation.EasyBean;
import icu.etl.collection.CaseSensitivMap;
import icu.etl.expression.DataUnitExpression;
import icu.etl.io.BufferedLineReader;
import icu.etl.ioc.EasyetlContext;
import icu.etl.ioc.EasyetlContextAware;
import icu.etl.os.OS;
import icu.etl.os.OSCommand;
import icu.etl.os.OSCommandStdouts;
import icu.etl.os.OSCpu;
import icu.etl.os.OSDateCommand;
import icu.etl.os.OSDisk;
import icu.etl.os.OSFile;
import icu.etl.os.OSFileCommand;
import icu.etl.os.OSFileFilter;
import icu.etl.os.OSFtpCommand;
import icu.etl.os.OSMemory;
import icu.etl.os.OSNetwork;
import icu.etl.os.OSNetworkCard;
import icu.etl.os.OSProcess;
import icu.etl.os.OSSecureShellCommand;
import icu.etl.os.OSService;
import icu.etl.os.OSUser;
import icu.etl.os.OSUserGroup;
import icu.etl.os.internal.OSCommandUtils;
import icu.etl.os.internal.OSDiskImpl;
import icu.etl.os.internal.OSMemoryImpl;
import icu.etl.os.internal.OSNetworkCardImpl;
import icu.etl.os.internal.OSProcessorImpl;
import icu.etl.util.ArrayUtils;
import icu.etl.util.CollectionUtils;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.NetUtils;
import icu.etl.util.StringUtils;

/**
 * 远程 linux 操作系统的接口实现类
 */
@EasyBean(name = "linux")
public class LinuxRemoteOS implements OS, OSFileCommand, OSDateCommand, OSNetwork, EasyetlContextAware {

    protected OSSecureShellCommand cmd;
    protected OSFtpCommand sftp;
    protected String host;
    protected int port;
    protected String username;
    protected String password;
    protected String name;
    protected String release;
    protected String kernel;
    protected LinuxUser currentUser;

    protected List<LinuxUser> users = new ArrayList<LinuxUser>();
    protected List<OSUserGroup> groups = new ArrayList<OSUserGroup>();
    protected List<LinuxEtcService> services = new ArrayList<LinuxEtcService>();

    protected EasyetlContext context;

    /**
     * 初始化
     *
     * @param host
     * @param port
     * @param username
     * @param password
     */
    public LinuxRemoteOS(String host, int port, String username, String password) {
        Ensure.isTrue(this.connect(host, port, username, password), host, port, username, password);
    }

    public void setContext(EasyetlContext context) {
        this.context = context;
    }

    /**
     * 登录服务器
     *
     * @param host
     * @param port
     * @param username
     * @param password
     * @return
     */
    public boolean connect(String host, int port, String username, String password) {
        this.cmd = this.context.getBean(OSSecureShellCommand.class, "linux", "ssh");
        if (this.cmd.connect(host, port, username, password)) {
            try {
                this.host = host;
                this.port = port;
                this.username = username;
                this.password = password;
                this.init();
                return true;
            } finally {
                this.close();
            }
        } else {
            return false;
        }
    }

    /**
     * 查询当前用户名，所有用户信息，用户组信息
     */
    @SuppressWarnings("unchecked")
    protected synchronized void init() {
        this.users.clear();
        this.groups.clear();
        this.currentUser = null;
        this.name = null;
        this.kernel = null;
        this.release = null;
        this.currentUser = null;

        OSCommandStdouts stdouts = this.cmd.execute( //
                "whoami", "whoami", // 查看当前用户名
                "/etc/passwd", "cat /etc/passwd", // 显示所有用户信息
                "/etc/group", "cat /etc/group", // 显示用户组信息
                "uname", "uname -a", // 内核版本信息
                "os-release", "cat /etc/*-release" // 发行版本号
        );

        String username = OSCommandUtils.join(stdouts.get("whoami"));
        String[] unames = StringUtils.splitByBlank(OSCommandUtils.join(stdouts.get("uname")));
        this.name = unames[0]; // 操作系统名
        this.kernel = unames[2]; // 内核版本
        this.users.addAll(Linuxs.parseEtcPasswd(stdouts.get("/etc/passwd"))); // 用户信息
        this.groups.addAll(Linuxs.parseEtcGroup(stdouts.get("/etc/group"))); // 用户组
        this.release = Linuxs.parseEtcRelease(stdouts.get("os-release")); // 发行版本号
        this.currentUser = this.getUser(username); // 当前登录用户信息
        if (this.currentUser == null) {
            throw new NullPointerException(username);
        }

        this.currentUser.setPassword(this.password); // 保存密码
        this.currentUser.setProfiles((List<String>) this.cmd.getAttribute("profile"));

        if (this.currentUser.isRoot()) {
            // 查询用户环境变量
            List<String> commandList = new ArrayList<String>();
            List<LinuxUser> users = new ArrayList<LinuxUser>();
            for (LinuxUser user : this.users) {
                if (!user.getName().equals(this.currentUser.getName())) { // 当前用户不需要查询环境
                    commandList.add(user.getName());
                    commandList.add("ls -a " + user.getHome() + " | grep bash ; ls -a " + user.getHome() + " | grep profile");
                    users.add(user);
                }
            }

            // 查看用户环境文件
            OSCommandStdouts lsmap = this.cmd.execute(commandList);
            for (LinuxUser user : users) {
                ArrayList<String> list = new ArrayList<String>();
                List<String> files = lsmap.get(user.getName());
                for (String line : files) {
                    String filename = StringUtils.trimBlank(line);
                    if (filename.equals(".bash_profile")) {
                        list.add(NetUtils.joinUri(user.getHome(), "/" + filename));
                    } else if (filename.equals(".bashrc")) {
                        list.add(NetUtils.joinUri(user.getHome(), "/" + filename));
                    } else if (filename.equals(".bash_login")) {
                        list.add(NetUtils.joinUri(user.getHome(), "/" + filename));
                    } else if (filename.equals(".profile")) {
                        list.add(NetUtils.joinUri(user.getHome(), "/" + filename));
                    }
                }
                user.setProfiles(list);
            }
        }
    }

    public boolean supportOSCommand() {
        return true;
    }

    public OSCommand getOSCommand() {
        return this.cmd;
    }

    public synchronized boolean enableOSCommand() {
        if (this.cmd == null) {
            this.cmd = this.context.getBean(OSSecureShellCommand.class);
        }

        if (this.cmd.isConnected()) {
            return true;
        } else if (this.cmd.connect(this.host, this.port, this.username, this.password)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isEnableOSCommand() {
        return this.cmd != null && this.cmd.isConnected();
    }

    /**
     * 返回 true 表示需要关闭命令接口
     *
     * @return
     */
    public boolean needDisableOSCommand() {
        if (this.cmd == null) {
            this.cmd = this.context.getBean(OSSecureShellCommand.class);
        }

        if (this.cmd.isConnected()) {
            return false;
        } else {
            Ensure.isTrue(this.cmd.connect(this.host, this.port, this.username, this.password), this.host, this.port, this.username, this.password);
            return true;
        }
    }

    public synchronized void disableOSCommand() {
        IO.close(this.cmd);
    }

    public synchronized boolean hasUser(String username) {
        return this.getUser(username) != null;
    }

    public synchronized List<OSUserGroup> getGroups() {
        return Collections.unmodifiableList(this.groups);
    }

    public synchronized List<OSUser> getUsers() {
        return new ArrayList<OSUser>(this.users);
    }

    public synchronized OSUser getUser() {
        return this.currentUser;
    }

    public synchronized LinuxUser getUser(String username) {
        for (LinuxUser user : this.users) {
            if (user.getName().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public synchronized boolean addUser(String username, String password, String group, String home, String shell) {
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException(username);
        }
        if (password == null) {
            throw new IllegalArgumentException(password);
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append("useradd");
        if (StringUtils.isNotBlank(group)) {
            cmd.append(" -g ").append(group);
        }
        if (StringUtils.isNotBlank(home)) {
            cmd.append(" -d ").append(home);
        }
        if (StringUtils.isNotBlank(shell)) {
            cmd.append(" -s ").append(shell);
        }
        cmd.append(" -m ").append(username);

        boolean need = this.needDisableOSCommand();
        try {
            if (this.cmd.execute(cmd.toString()) == 0) {
                this.init();
                return this.changePassword(username, password);
            } else {
                return false;
            }
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public synchronized boolean delUser(String username) {
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException(username);
        }

        boolean need = this.needDisableOSCommand();
        try {
            if (this.cmd.execute("userdel -r " + username) == 0) {
                this.init();
                return true;
            } else {
                return false;
            }
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public synchronized boolean changePassword(String username, String password) {
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException(username);
        }
        if (password == null) {
            throw new IllegalArgumentException(password);
        }

        boolean need = this.needDisableOSCommand();
        try {
            if (this.cmd.execute("echo \"" + password + "\" | passwd " + username + " --stdin > /dev/null 2>&1") == 0) {
                OSUser user = this.getUser(username);
                if (user != null) {
                    user.setPassword(password);
                }
                return true;
            } else {
                return false;
            }
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public String getKernelVersion() {
        return this.kernel;
    }

    public String getReleaseVersion() {
        return this.release;
    }

    public List<OSService> getOSServices() {
        if (this.services.size() <= 20) {
            boolean need = this.needDisableOSCommand();
            try {
                this.cmd.execute("cat /etc/services");
                this.services.clear();
                BufferedLineReader in = new BufferedLineReader(this.cmd.getStdout());
                try {
                    while (in.hasNext()) {
                        LinuxEtcService service = LinuxEtcService.newInstance(in.next());
                        if (service != null) {
                            this.services.add(service);
                        }
                    }
                } finally {
                    IO.close(in);
                }
            } finally {
                if (need) {
                    this.disableOSCommand();
                }
            }
        }
        return new ArrayList<OSService>(this.services);
    }

    public OSService getOSService(int port) {
        for (OSService obj : this.services) {
            if (obj.getPort() == port) {
                return obj;
            }
        }

        boolean need = this.needDisableOSCommand();
        try {
            this.cmd.execute("cat /etc/services | grep " + port + "/");
            BufferedLineReader in = new BufferedLineReader(this.cmd.getStdout());
            try {
                while (in.hasNext()) {
                    String line = in.next();
                    LinuxEtcService obj = LinuxEtcService.newInstance(line);
                    if (obj != null) {
                        this.services.add(obj);
                        return obj;
                    }
                }
                return null;
            } finally {
                IO.close(in);
            }
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public List<OSService> getOSService(String name) {
        boolean need = this.needDisableOSCommand();
        try {
            List<OSService> list = new ArrayList<OSService>();
            this.cmd.execute("cat /etc/services | grep " + name);
            BufferedLineReader in = new BufferedLineReader(this.cmd.getStdout());
            try {
                while (in.hasNext()) {
                    String line = in.next();
                    LinuxEtcService obj = LinuxEtcService.newInstance(line);
                    if (obj != null) {
                        this.services.add(obj);
                        list.add(obj);
                    }
                }
                return list;
            } finally {
                IO.close(in);
            }
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public OSFileCommand getOSFileCommand() {
        return this;
    }

    public boolean supportOSFileCommand() {
        return true;
    }

    public boolean enableOSFileCommand() {
        if (this.sftp == null) {
            this.sftp = this.context.getBean(OSFtpCommand.class, "sftp");
        }

        if (this.sftp.isConnected()) {
            return true;
        } else if (this.sftp.connect(this.host, this.port, this.username, this.password)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isEnableOSFileCommand() {
        return this.sftp != null && this.sftp.isConnected();
    }

    /**
     * 返回 true 表示关闭文件操作接口
     *
     * @return
     */
    protected boolean needDisableOSFileCommand() {
        if (this.sftp == null) {
            this.sftp = this.context.getBean(OSFtpCommand.class, "sftp");
        }

        if (this.sftp.isConnected()) {
            return false;
        } else {
            Ensure.isTrue(this.sftp.connect(this.host, this.port, this.username, this.password), this.host, this.port, this.username, this.password);
            return true;
        }
    }

    public void disableOSFileCommand() {
        IO.close(this.sftp);
    }

    public boolean cd(String filepath) {
        return this.sftp.cd(filepath);
    }

    public String pwd() {
        return this.sftp.pwd();
    }

    public synchronized boolean exists(String filepath) {
        return this.sftp.exists(filepath);
    }

    public synchronized boolean isFile(String filepath) {
        return this.sftp.isFile(filepath);
    }

    public synchronized boolean isDirectory(String filepath) {
        return this.sftp.isDirectory(filepath);
    }

    public synchronized boolean mkdir(String filepath) throws IOException {
        return this.sftp.mkdir(filepath);
    }

    public synchronized boolean rm(String filepath) throws IOException {
        return this.sftp.rm(filepath);
    }

    public synchronized List<OSFile> ls(String filepath) throws IOException {
        return this.sftp.ls(filepath);
    }

    public synchronized boolean rename(String filepath, String newfilepath) throws IOException {
        return this.sftp.rename(filepath, newfilepath);
    }

    public List<OSFile> find(String directory, String name, char type, OSFileFilter filter) throws IOException {
        boolean need = this.needDisableOSCommand();
        try {
            List<OSFile> list = new ArrayList<OSFile>();
            this.cmd.execute("find " + directory + " -name " + name + " -type " + type);
            BufferedLineReader in = new BufferedLineReader(this.cmd.getStdout());
            try {
                while (in.hasNext()) {
                    String filepath = StringUtils.trimBlank(in.next());
                    if (StringUtils.isNotBlank(filepath) && !filepath.startsWith("find:")) {
                        List<OSFile> fileList = this.sftp.ls(filepath);
                        for (OSFile file : fileList) {
                            if (filter == null || filter.accept(file)) {
                                list.add(file);
                            }
                        }
                    }
                }
                return list;
            } finally {
                IO.close(in);
            }
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public synchronized String read(String filepath, String charsetName, int lineno) throws IOException {
        return this.sftp.read(filepath, charsetName, lineno);
    }

    public synchronized boolean write(String filepath, String charsetName, boolean append, CharSequence content) throws IOException {
        return this.sftp.write(filepath, charsetName, append, content);
    }

    public boolean copy(String filepath, String directory) {
        boolean need = this.needDisableOSCommand();
        try {
            if (this.sftp.isDirectory(filepath)) {
                return this.cmd.execute("cp -rf " + filepath + " " + directory) == 0;
            } else {
                return this.cmd.execute("cp -f " + filepath + " " + directory) == 0;
            }
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public boolean upload(InputStream in, String remote) throws IOException {
        return this.sftp.upload(in, remote);
    }

    public boolean download(String remote, OutputStream out) throws IOException {
        return this.sftp.download(remote, out);
    }

    public boolean upload(File localFile, String filepath) throws IOException {
        return this.sftp.upload(localFile, filepath);
    }

    public File download(String filepath, File localDir) throws IOException {
        return this.sftp.download(filepath, localDir);
    }

    public String getCharsetName() {
        return this.sftp.getCharsetName();
    }

    public void setCharsetName(String charsetName) {
        this.sftp.setCharsetName(charsetName);
    }

    public String getLineSeparator() {
        return FileUtils.lineSeparatorUnix;
    }

    public char getFolderSeparator() {
        return '/';
    }

    public List<OSProcess> getOSProgressList(String findStr) {
        boolean need = this.needDisableOSCommand();
        try {
            String command = "ps -efl ";
            if (StringUtils.isNotBlank(findStr)) {
                command = "ps -efl | head -n 1; ps -efl | grep -v grep | grep " + findStr;
            }

            this.cmd.execute(command);
            List<Map<String, String>> listmap = OSCommandUtils.splitPSCmdStdout(this.cmd.getStdout());
            List<OSProcess> list = new ArrayList<OSProcess>();
            for (Map<String, String> map : listmap) {
                LinuxProgress obj = new LinuxProgress(this);
                obj.setCpu(map.get("C"));
                obj.setMemory(StringUtils.isLong(map.get("SZ")) ? Long.parseLong(map.get("SZ")) : 0);
                obj.setPid(map.get("pid"));
                obj.setPpid(map.get("ppid"));
                obj.setCmd(map.get("cmd"));
                list.add(obj);
            }
            return list;
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public OSProcess getOSProgress(String pid) {
        if (StringUtils.isBlank(pid)) {
            return null;
        }

        boolean need = this.needDisableOSCommand();
        try {
            this.cmd.execute("ps -p " + pid + " -fl ");
            List<Map<String, String>> listmap = OSCommandUtils.splitPSCmdStdout(this.cmd.getStdout());
            Map<String, String> map = CollectionUtils.onlyOne(listmap);
            if (map == null) {
                return null;
            }

            LinuxProgress p = new LinuxProgress(this);
            p.cpu = map.get("C");
            p.memory = (StringUtils.isLong(map.get("SZ")) ? Long.parseLong(map.get("SZ")) : 0);
            p.pid = map.get("pid");
            p.ppid = map.get("ppid");
            p.cmd = map.get("cmd");
            return p;
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public void close() {
        IO.close(this.cmd, this.sftp);
    }

    public boolean supportOSDateCommand() {
        return true;
    }

    public OSDateCommand getOSDateCommand() {
        return this;
    }

    public Date getDate() {
        boolean need = this.needDisableOSCommand();
        try {
            this.cmd.execute("date +%F\\ %T");
            return Dates.parse(this.cmd.getStdout());
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public synchronized boolean setDate(Date date) {
        if (date == null) {
            return false;
        }

        boolean need = this.needDisableOSCommand();
        try {
            return this.cmd.execute("date -s " + StringUtils.quotes(Dates.format19(date))) == 0;
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public boolean supportOSNetwork() {
        return true;
    }

    public OSNetwork getOSNetwork() {
        return this;
    }

    public List<OSNetworkCard> getOSNetworkCards() {
        boolean need = this.needDisableOSCommand();
        try {
            List<OSNetworkCard> list = new ArrayList<OSNetworkCard>();
            OSCommandStdouts stdouts = this.cmd.execute("key0", "ls -lt /etc/sysconfig/network-scripts/ifcfg-*", "cat dns", "cat /etc/resolv.conf", "ip route", "ip route show", "ifconfig", "ifconfig");
            String[] dns = Linuxs.parseEtcResolv(stdouts.get("cat dns"));
            String gateway = Linuxs.parseIpRouteShow(stdouts.get("ip route"));
            List<Map<String, String>> devices = Linuxs.parseIfConfig(stdouts.get("ifconfig"));

            // 遍历所有网络设备
            List<String> files = stdouts.get("key0");
            for (int i = 0; i < files.size(); i++) {
                String eth = files.get(i);
                if (StringUtils.isBlank(eth)) {
                    continue;
                }

                String ethfile = ArrayUtils.lastElement(StringUtils.splitByBlank(eth));
                if (!ethfile.startsWith("/etc")) {
                    continue;
                }

                this.cmd.execute("TEST2021=`ls -lt /etc/sysconfig/network-scripts/ifcfg-*|sed -n '" + (i + 1) + "p'|awk -F' ' '{print $NF}'`; cat $TEST2021");
                OSNetworkCardImpl card = Linuxs.parseEtcNetwork(this.cmd.getStdout());

                if ("127.0.0.1".equals(card.getIPAddress())) { // 过滤
                    continue;
                }

                card.setDns1(dns[0]);
                card.setDns2(dns[1]);
                if (StringUtils.isBlank(card.getGateway())) {
                    card.setGateway(gateway);
                }

                // 根据网卡信息搜索 ifconfig 中的网络设备信息
                Map<String, String> device = null;
                for (Map<String, String> obj : devices) {
                    // 如果设备编号相同
                    if (obj.containsKey("name") && StringUtils.isNotBlank(card.getName()) && obj.get("name").equalsIgnoreCase(card.getName())) {
                        device = obj;
                        break;
                    }

                    // 如果IP地址相同
                    if (obj.containsKey("ip") && card.getIPAddress().equalsIgnoreCase(obj.get("ip"))) {
                        device = obj;
                        break;
                    }

                    // 如果IPv6地址相同
                    if (obj.containsKey("ipv6") && StringUtils.inArrayIgnoreCase(obj.get("ipv6"), card.getIPAddress(), card.getIP6Address())) {
                        device = obj;
                        break;
                    }
                }

                if (device != null) {
                    if (device.containsKey("mask")) {
                        card.setMask(device.get("mask"));
                    }
                    if (StringUtils.isBlank(card.getIP6Address()) && device.containsKey("ipv6")) {
                        card.setIp6Address(device.get("ipv6"));
                    }
                    if (StringUtils.isNotBlank(device.get("name"))) {
                        card.setName(device.get("name"));
                    }

                    if (device.containsKey("mac")) {
                        card.setMacAddress(device.get("mac"));
                    }

                    // 设置mac地址
                    if (StringUtils.isBlank(card.getMacAddress())) {
                        this.cmd.execute("cat /sys/class/net/" + device.get("name") + "/address");
                        String str = StringUtils.trimBlank(this.cmd.getStdout());
                        if (NetUtils.isMacAddress(str)) {
                            card.setMacAddress("");
                        }
                    }
                }

                list.add(card);
            }

            // 对网卡排序
            Collections.sort(list, new Comparator<OSNetworkCard>() {

                public int compare(OSNetworkCard o1, OSNetworkCard o2) {
                    if ("127.0.0.1".equals(o1.getIPAddress()) || !o1.isEnabled()) {
                        return 1;
                    } else if ("127.0.0.1".equalsIgnoreCase(o2.getIPAddress()) || !o2.isEnabled()) {
                        return -1;
                    } else {
                        return o1.getType() - o2.getType();
                    }
                }
            });

            return list;
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public List<OSCpu> getOSCpus() {
        boolean need = this.needDisableOSCommand();
        try {
            List<OSCpu> list = new ArrayList<OSCpu>();
            Map<String, String> map = new CaseSensitivMap<String>();
            this.cmd.execute("cat /proc/cpuinfo");
            BufferedLineReader in = new BufferedLineReader(this.cmd.getStdout());
            try {
                while (in.hasNext()) {
                    map.clear();

                    do {
                        String line = in.next();
                        String[] array = StringUtils.trimBlank(StringUtils.split(line, ':'));
                        if (StringUtils.isBlank(line) || array.length != 2) {
                            break;
                        } else {
                            map.put(StringUtils.trimBlank(array[0]), array[1]);
                        }
                    } while (in.hasNext());

                    if (map.size() > 0) {
                        OSProcessorImpl obj = new OSProcessorImpl();
                        obj.setId(map.get("processor"));
                        obj.setModeName(map.get("model name"));
                        obj.setCoreId(map.get("core id"));
                        obj.setCacheSize(DataUnitExpression.parse(StringUtils.defaultString(map.get("cache size"), "0")));
                        obj.setCores(StringUtils.parseInt(map.get("cpu cores"), 0));
                        obj.setPhysicalId(map.get("physical id"));
                        obj.setSiblings(StringUtils.parseInt(map.get("siblings"), 0));
                        list.add(obj);
                    }
                }
                return list;
            } finally {
                IO.close(in);
            }
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public List<OSDisk> getOSDisk() {
        boolean need = this.needDisableOSCommand();
        try {
            List<OSDisk> list = new ArrayList<OSDisk>();
            this.cmd.execute("LANG=zh_CN." + this.cmd.getCharsetName() + " && df -Tk");
            BufferedLineReader in = new BufferedLineReader(StringUtils.trimBlank(this.cmd.getStdout(this.cmd.getCharsetName())));
            try {
                String[] titles = StringUtils.splitByBlank(StringUtils.trimBlank(StringUtils.encodeGBKtoUTF8(in.next())));
                int length = 7;
                while (in.hasNext()) {
                    String line = StringUtils.trimBlank(in.next());
                    String[] array = StringUtils.splitByBlank(line);
                    if (array.length < length) {
                        line = line + in.next();
                        array = CollectionUtils.toArray(StringUtils.splitByBlank(line, length));
                    } else if (array.length > length) {
                        array = CollectionUtils.toArray(StringUtils.splitByBlank(line, length));
                    }

                    Ensure.isTrue(array.length == length, line, titles);

                    OSDiskImpl obj = new OSDiskImpl();
                    obj.setId(array[0]);
                    obj.setType(array[1]);
                    obj.setTotal(DataUnitExpression.parse(array[2] + "kb"));
                    obj.setUsed(DataUnitExpression.parse(array[3] + "kb"));
                    obj.setFree(DataUnitExpression.parse(array[4] + "kb"));
                    obj.setAmount(array[6]);
                    list.add(obj);
                }
                return list;
            } finally {
                IO.close(in);
            }
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public OSMemory getOSMemory() {
        boolean need = this.needDisableOSCommand();
        try {
            OSMemoryImpl obj = new OSMemoryImpl();
            this.cmd.execute("cat /proc/meminfo");
            BufferedLineReader in = new BufferedLineReader(this.cmd.getStdout());
            try {
                while (in.hasNext()) {
                    String line = in.next();
                    String[] array = StringUtils.trimBlank(StringUtils.split(line, ':'));

                    if (array != null && array.length == 2) {
                        String name = array[0];

                        if (name.equalsIgnoreCase("MemTotal")) {
                            obj.setTotal(DataUnitExpression.parse(array[1]));
                        } else if (name.equalsIgnoreCase("MemFree")) {
                            obj.setFree(DataUnitExpression.parse(array[1]));
                        } else if (name.equalsIgnoreCase("Active")) {
                            obj.setActive(DataUnitExpression.parse(array[1]));
                        }
                    }
                }
                return obj;
            } finally {
                IO.close(in);
            }
        } finally {
            if (need) {
                this.disableOSCommand();
            }
        }
    }

    public String getHost() {
        return this.host;
    }

}
