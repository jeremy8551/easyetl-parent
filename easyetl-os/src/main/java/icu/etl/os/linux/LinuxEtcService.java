package icu.etl.os.linux;

import java.util.ArrayList;
import java.util.List;

import icu.etl.os.OSService;
import icu.etl.util.ArrayUtils;
import icu.etl.util.Ensure;
import icu.etl.util.StringUtils;

/**
 * Linux 上端口服务的接口实现类
 */
public class LinuxEtcService implements OSService {
    private String name;
    private int port;
    private String protocal;
    private List<String> aliases;

    public LinuxEtcService() {
        super();
    }

    public static LinuxEtcService newInstance(String line) {
        line = StringUtils.trimBlank(Linuxs.removeLinuxAnnotation(line, null));
        if (line.length() == 0) {
            return null;
        }

        int nameEndPos = StringUtils.indexOfBlank(line, 0, -1);
        if (nameEndPos == -1) {
            return null;
        }

        String name = line.substring(0, nameEndPos);
        int portStartPos = StringUtils.indexOfNotBlank(line, nameEndPos, -1);
        if (portStartPos == -1) {
            return null;
        }

        int portEndPos = StringUtils.indexOfBlank(line, portStartPos, -1);
        if (portEndPos == -1) {
            portEndPos = line.length();
        }

        String portAndProtocal = line.substring(portStartPos, portEndPos);
        int index = portAndProtocal.indexOf('/');
        Ensure.isPosition(index);
        String port = portAndProtocal.substring(0, index);
        String protocal = portAndProtocal.substring(index + 1);
        ArrayList<String> aliases = (portEndPos >= line.length()) ? new ArrayList<String>() : ArrayUtils.asList(StringUtils.splitByBlank(StringUtils.trimBlank(line.substring(portEndPos))));

        LinuxEtcService obj = new LinuxEtcService();
        obj.setName(name);
        obj.setPort(Integer.parseInt(port));
        obj.setProtocal(protocal);
        obj.setAliases(aliases);
        return obj;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public String getProtocal() {
        return protocal;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setProtocal(String protocal) {
        this.protocal = protocal;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String toString() {
        return "LinuxEtcService [name=" + name + ", port=" + port + ", protocal=" + protocal + ", aliases=" + StringUtils.toString(aliases) + "]";
    }

}
