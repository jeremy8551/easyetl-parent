package icu.etl.os.linux;

import java.math.BigDecimal;

import icu.etl.os.OS;
import icu.etl.os.OSProcess;
import icu.etl.util.Ensure;

/**
 * Linux 进程接口的实现类
 */
public class LinuxProgress implements OSProcess {

    protected String pid;
    protected String ppid;
    protected long memory;
    protected String name;
    protected String cmd;
    protected String cpu;
    protected OS os;

    public LinuxProgress(OS os) {
        super();
        this.os = os;
    }

    public String getPid() {
        return this.pid;
    }

    public String getPPid() {
        return this.ppid;
    }

    public BigDecimal getCpu() {
        return new BigDecimal(this.cpu);
    }

    public long getMemory() {
        return this.memory;
    }

    public String getName() {
        return this.name;
    }

    public String getCmd() {
        return this.cmd;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setPpid(String ppid) {
        this.ppid = ppid;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public boolean kill() {
        Ensure.isTrue(this.os.supportOSCommand(), this.os);
        return this.os.getOSCommand().execute("kill -9 " + this.getPid()) == 0;
    }

    public String toString() {
        return "StandardOSProcess [pid=" + pid + ", ppid=" + ppid + ", memory=" + memory + ", cpu=" + cpu + ", cmd=" + this.cmd + "]";
    }

}
