package icu.etl.os.linux;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.io.BufferedLineReader;
import icu.etl.jdk.JavaDialectFactory;
import icu.etl.os.OSNetworkCard;
import icu.etl.os.OSUserGroup;
import icu.etl.os.internal.OSNetworkCardImpl;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.IO;
import icu.etl.util.NetUtils;
import icu.etl.util.StringUtils;

public class Linuxs {

    /** Linux 操作系统内置账户名（用于过滤操作系统内置账户信息） */
    public final static String PROPERTY_LINUX_BUILTIN_ACCT = Linuxs.class.getPackage().getName().split("\\.")[0] + "." + Linuxs.class.getPackage().getName().split("\\.")[1] + ".linux.builtin.accounts";

    private Linuxs() {
    }

    /**
     * 将日期转为linux 上文件的日期格式
     *
     * @param date
     * @return
     */
    public static StringBuilder toFileDateFormat(Date date) {
        int month = Dates.getMonth(date);
        int dayOfMonth = Dates.getDayOfMonth(date);
        int hour = Dates.getHour(date);
        int minute = Dates.getMinute(date);
        String monthStr = "";
        switch (month) {
            case 1:
                monthStr = Dates.January;
            case 2:
                monthStr = Dates.February;
            case 3:
                monthStr = Dates.March;
            case 4:
                monthStr = Dates.April;
            case 5:
                monthStr = Dates.May;
            case 6:
                monthStr = Dates.June;
            case 7:
                monthStr = Dates.July;
            case 8:
                monthStr = Dates.August;
            case 9:
                monthStr = Dates.September;
            case 10:
                monthStr = Dates.October;
            case 11:
                monthStr = Dates.November;
            case 12:
                monthStr = Dates.December;
                break;
            default:
                throw new UnsupportedOperationException(String.valueOf(month));
        }

        StringBuilder buf = new StringBuilder();
        buf.append(StringUtils.left(monthStr, 3));
        buf.append(' ');
        buf.append(StringUtils.left(dayOfMonth, 3));
        buf.append(' ');
        buf.append(hour);
        buf.append(':');
        buf.append(minute);
        return buf;
    }

    /**
     * 文件为 unix ls 命令格式的字符串，例如: <br>
     * drwxrwxr-- 4096 Feb 19 19:01 db2inst1 <br>
     * 第一个字段是文件权限信息 <br>
     * 第二个字段是文件大小 <br>
     * 第三个字段是最后修改时间 <br>
     * 第四个字段是文件名
     *
     * @param file
     * @return
     */
    public static StringBuilder toLongname(File file) {
        Ensure.isTrue(file != null && file.exists(), file);

        StringBuilder str = new StringBuilder();
        if (file.isDirectory()) {
            str.append('d');
        } else if (file.isFile()) {
            str.append('-');
        } else {
            str.append(' ');
        }

        if (file.canRead()) {
            str.append('r');
        } else {
            str.append('-');
        }
        if (file.canWrite()) {
            str.append('w');
        } else {
            str.append('-');
        }
        if (JavaDialectFactory.getDialect().canExecute(file)) {
            str.append('x');
        } else {
            str.append('-');
        }

        str.append(JavaDialectFactory.getDialect().toLongname(file));
        str.append(StringUtils.right(file.length(), 10, ' '));
        str.append(' ');

        Date lastModifiedDate = new Date(file.lastModified());
        str.append(Linuxs.toFileDateFormat(lastModifiedDate));
        str.append(' ');
        str.append(file.getName());
        return str;
    }

    /**
     * 移除字符串中的注释，注释以 # 符号开头
     *
     * @param str  字符串
     * @param list 用于存储字符串中的注释
     * @return 移除注释后的字符串
     */
    public static String removeShellNote(String str, List<String> list) {
        if (str == null) {
            return null;
        }

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '#') {
                if (list != null) {
                    list.add(str.subSequence(i, str.length()).toString());
                }
                return str.substring(0, i);
            }
        }
        return str;
    }

    /**
     * 解析 /etc/sysconfig/network-scripts/ifcfg-* 文件内容
     *
     * @param stdout
     * @return
     */
    public static OSNetworkCardImpl parseEtcNetwork(String stdout) {
        Map<String, String> map = new CaseSensitivMap<String>();
        BufferedLineReader in = new BufferedLineReader(stdout);
        try {
            while (in.hasNext()) {
                String line = in.next();
                String[] array = StringUtils.trimBlank(StringUtils.splitProperty(Linuxs.removeShellNote(line, null)));
                if (array != null && array.length == 2) {
                    map.put(StringUtils.trimBlank(array[0]), array[1]);
                }
            }
        } finally {
            IO.close(in);
        }

        OSNetworkCardImpl card = new OSNetworkCardImpl();
        card.setName(StringUtils.defaultString(map.get("DEVICE"), map.get("NAME")));
        card.setEnabled(true);
        card.setStatic(StringUtils.inArrayIgnoreCase(map.get("BOOTPROTO"), "none", "static"));
        card.setDhcp("dhcp".equalsIgnoreCase(map.get("BOOTPROTO")));
        // card.setDns1(dns[0]);
        // card.setDns2(dns[1]);

        String type = map.get("TYPE"); // Ethernet, Wireless, InfiniBand, Bridge, Bond, Vlan, Team, TeamPort
        if ("Ethernet".equalsIgnoreCase(type)) {
            card.setType(OSNetworkCard.TYPE_ETHERNET);
        } else if ("Wireless".equalsIgnoreCase(type)) {
            card.setType(OSNetworkCard.TYPE_WIRELESS);
        } else if ("InfiniBand".equalsIgnoreCase(type)) {
            card.setType(OSNetworkCard.TYPE_INFINIBAND);
        } else if ("Bridge".equalsIgnoreCase(type)) {
            card.setType(OSNetworkCard.TYPE_BRIDGE);
        } else if ("Bond".equalsIgnoreCase(type)) {
            card.setType(OSNetworkCard.TYPE_BOND);
        } else if ("Vlan".equalsIgnoreCase(type)) {
            card.setType(OSNetworkCard.TYPE_VLAN);
        } else if ("Team".equalsIgnoreCase(type)) {
            card.setType(OSNetworkCard.TYPE_TEAM);
        } else if ("TeamPort".equalsIgnoreCase(type)) {
            card.setType(OSNetworkCard.TYPE_TEAMPORT);
        } else {
            card.setType(OSNetworkCard.TYPE_UNKNOWN);
        }

        // if (card.isStatic()) {
        card.setIpAddress(map.get("IPADDR"));
        card.setMask(map.get("NETMASK"));
        card.setGateway(map.get("NETWORK"));
        card.setMacAddress(map.get("HWADDR"));
        card.setIp6Address(map.get("IPV6ADDR"));
        card.setIp6Gateway(map.get("IPV6_DEFAULTGW"));
        // } else {
        // OSCommandStdouts maps = this.cmd.execute("show ifconfig", "ifconfig " + card.getName(), "show gateway", "route -n |grep default|awk -F\" \" '{print $2}'");
        // List<String> ifconfig = maps.get("show ifconfig");
        // if (ifconfig.size() > 1) {
        // String line1 = ifconfig.get(0);
        // String line2 = ifconfig.get(1);
        // String[] array = StringUtils.splitByBlank(StringUtils.trimBlank(line2));
        // card.setMacAddress(Arrays.lastElement(StringUtils.splitByBlank(StringUtils.trimBlank(line1))));
        // card.setMask(Arrays.lastElement(array));
        // card.setIpAddress(Arrays.lastElement(StringUtils.split(array[1], ':')));
        // }
        //
        // List<String> gateways = maps.get("show gateway");
        // if (gateways.size() > 0) {
        // card.setGateway(StringUtils.trimBlank(gateways.get(0)));
        // }
        // }
        return card;
    }

    /**
     * 解析 cat /etc/passwd 命令输出信息
     *
     * @param stdouts
     * @return 用户信息集合
     */
    public static List<LinuxUser> parseEtcPasswd(List<String> stdouts) {
        String[] excludes = getLinuxBuiltinAccount();
        List<LinuxUser> users = new ArrayList<LinuxUser>();
        for (String line : stdouts) {
            String[] array = StringUtils.split(line, ':');
            if (array.length == 7) {
                String username = StringUtils.trimBlank(array[0]);
                if (!StringUtils.inArray(username, excludes)) { // 只查询超级用户和普通用户，过滤掉系统内置用户信息
                    LinuxUser user = new LinuxUser();
                    user.setName(username);
                    user.setPassword(array[1]);
                    user.setId(array[2]);
                    user.setGroup(array[3]);
                    user.setMemo(array[4]);
                    user.setHome(array[5]);
                    user.setShell(array[6]);
                    users.add(user);
                }
            }
        }
        return users;
    }

    /**
     * 返回 Linux 内置账户数组（当远程登陆 linux 系统执行命令时，会自动过滤这些内置账户信息）
     *
     * @return
     */
    public static String[] getLinuxBuiltinAccount() {
        if (System.getProperties().containsKey(PROPERTY_LINUX_BUILTIN_ACCT)) { // 如果设置类参数
            String str = System.getProperty(PROPERTY_LINUX_BUILTIN_ACCT);
            return StringUtils.removeBlank(StringUtils.split(str, ','));
        } else {
            return new String[]{ // 内置用户数组
                    "bin", "daemon", "adm", "sync", "lp", //
                    "shutdown", "halt", "mail", "uucp", "operator", //
                    "games", "gopher", "ftp", "nobody", "dbus", //
                    "usbmuxd", "rpc", "rtkit", "avahi-autoipd", "vcsa", //
                    "abrt", "rpcuser", "nfsnobody", "haldaemon", "ntp", //
                    "apache", "saslauth", "postfix", "gdm", "pulse", //
                    "sshd", "tcpdump", "zabbix" //
            };
        }
    }

    /**
     * 解析 cat /etc/group 命令输出信息
     *
     * @param stdouts
     * @return 用户组信息集合
     */
    public static List<OSUserGroup> parseEtcGroup(List<String> stdouts) {
        List<OSUserGroup> groups = new ArrayList<OSUserGroup>();
        for (String line : stdouts) {
            String[] array = StringUtils.split(line, ':');
            if (array.length == 4) {
                LinuxGroup group = new LinuxGroup();
                group.setName(array[0]);
                group.setPassword(array[1]);
                group.setGid(array[2]);
                String[] usernames = StringUtils.split(array[3], ',');
                for (String name : usernames) {
                    if (StringUtils.isNotBlank(name)) {
                        group.addUser(name);
                    }
                }
                groups.add(group);
            }
        }
        return groups;
    }

    /**
     * 解析 cat /etc/*-release 命令输出信息
     *
     * @param stdouts
     * @return Linux 发行版本号
     */
    public static String parseEtcRelease(List<String> stdouts) {
        String release = null;
        StringBuilder buf = new StringBuilder(50);
        for (String line : stdouts) {
            String[] attributes = StringUtils.splitProperty(line); // 有可能是 key="value"
            if (attributes == null) { // 判断是 /etc/redhat-release 文件中的配置信息
                if (StringUtils.isNotBlank(line)) {
                    release = line;
                }
            } else { // 判断是 /etc/os-release 文件中的配置信息
                String key = attributes[0];
                if ("name".equalsIgnoreCase(key)) {
                    buf.append(StringUtils.unquotation(attributes[1]));
                } else if ("version".equalsIgnoreCase(key)) {
                    buf.append(" release ");
                    buf.append(StringUtils.unquotation(attributes[1]));
                }
            }
        }
        if (StringUtils.isBlank(release)) {
            if (StringUtils.isNotBlank(buf)) {
                release = buf.toString();
            }
        }
        return release;
    }

    /**
     * 解析 cat /etc/resolv.conf 命令输出信息
     *
     * @param stdouts
     * @return 数组第一位表示首选DNS，数组第二位表示备选DNS
     */
    public static String[] parseEtcResolv(List<String> stdouts) {
        String dns1 = "", dns2 = "";
        for (String line : stdouts) {
            String[] array = StringUtils.splitByBlank(StringUtils.trimBlank(Linuxs.removeShellNote(line, null)));
            if (array != null && array.length == 2) {
                if (StringUtils.isBlank(dns1)) {
                    dns1 = array[1];
                } else {
                    dns2 = array[1];
                }
            }
        }
        return new String[]{dns1, dns2};
    }

    /**
     * 解析 ip route show 命令的输出信息
     *
     * @param stdouts
     * @return 返回网关信息
     */
    public static String parseIpRouteShow(List<String> stdouts) {
        for (String line : stdouts) {
            String[] array = StringUtils.splitByBlank(StringUtils.trimBlank(line));
            if (array.length >= 3 && NetUtils.isIP(array[2])) {
                return array[2];
            }
        }
        return null;
    }

    /**
     * 解析 ifconfig 命令的输出信息
     *
     * @param stdouts ifconfig 命令的输出信息集合
     * @return 集合中每个元素代表一个网络设备信息 <br>
     * name 表示设备标示符 <br>
     * mask 表示网卡 <br>
     * ip 表示IP地址 <br>
     * ipv6 表示IPv6地址 <br>
     * mac 表示MAC地址 <br>
     */
    public static List<Map<String, String>> parseIfConfig(List<String> stdouts) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        CaseSensitivMap<String> map = null;
        for (String line : stdouts) {
            if (line.length() > 0) {

                // 解析网络设备开始位置
                if (!Character.isWhitespace(line.charAt(0))) {
                    if (map != null && !map.isEmpty()) {
                        list.add(map);
                    }

                    int index = (int) Ensure.isFromZero(StringUtils.indexOfBlank(line, 0, -1));
                    String name = StringUtils.rtrimBlank(line.substring(0, index), ':');
                    map = new CaseSensitivMap<String>();
                    map.put("name", name);
                    line = line.substring(index + 1);
                }

                String[] fields = StringUtils.splitByBlank(StringUtils.trimBlank(line));
                for (int i = 0; i < fields.length; i++) {
                    String str = fields[i].toLowerCase();
                    if (str.indexOf(':') != -1) { // 格式: inet addr:10.160.7.81 Bcast:10.160.15.255 Mask:255.255.240.0
                        String[] array = StringUtils.splitProperty(str, ':');
                        String name = array[0];
                        String value = array[1];

                        if ("inet".equalsIgnoreCase(fields[0]) && "mask".equalsIgnoreCase(name) && NetUtils.isIP(value)) {
                            map.put("mask", value);
                            continue;
                        }

                        if ("inet".equalsIgnoreCase(fields[0]) && "addr".equalsIgnoreCase(name) && NetUtils.isIP(value)) {
                            map.put("ip", value);
                            continue;
                        }
                    } else { // 格式: inet 130.1.16.83 netmask 255.255.0.0 broadcast 130.1.255.255
                        if ("inet".equalsIgnoreCase(fields[0]) && str.endsWith("mask")) {
                            int next = i + 1;
                            if (next < fields.length && NetUtils.isIP(fields[next])) {
                                map.put("mask", fields[next]);
                                i = next;
                            }
                            continue;
                        }

                        if (str.equalsIgnoreCase("inet")) {
                            int next = i + 1;
                            if (next < fields.length && NetUtils.isIP(fields[next])) {
                                map.put("ip", fields[next]);
                                i = next;
                            }
                            continue;
                        }

                        if (str.equalsIgnoreCase("inet6")) {
                            int next = i + 1;
                            if (next < fields.length && NetUtils.isIPv6(fields[next])) {
                                map.put("ipv6", fields[next]);
                                i = next;
                            }
                            continue;
                        }

                        /**
                         * 从如下字符串中截取 MAC 地址 <br>
                         * ether 52:54:c4:5c:6f:d4 txqueuelen 1000 (Ethernet) <br>
                         * Link encap:Ethernet HWaddr 00:16:3E:00:1E:51 <br>
                         */
                        if (str.equalsIgnoreCase("ether") || str.equalsIgnoreCase("HWaddr")) {
                            int next = i + 1;
                            if (next < fields.length && NetUtils.isMacAddress(fields[next])) {
                                map.put("mac", fields[next]);
                                i = next;
                            }
                            continue;
                        }
                    }
                }

            }
        }

        if (map != null && !map.isEmpty()) {
            list.add(map);
        }
        return list;
    }
}
