package icu.etl.os.macos;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icu.etl.annotation.EasyBean;
import icu.etl.expression.DataUnitExpression;
import icu.etl.expression.Expression;
import icu.etl.io.BufferedLineReader;
import icu.etl.log.STD;
import icu.etl.os.OSCommandStdouts;
import icu.etl.os.OSCpu;
import icu.etl.os.OSDisk;
import icu.etl.os.OSMemory;
import icu.etl.os.OSProcess;
import icu.etl.os.internal.OSCommandUtils;
import icu.etl.os.internal.OSDiskImpl;
import icu.etl.os.internal.OSMemoryImpl;
import icu.etl.os.internal.OSProcessorImpl;
import icu.etl.os.linux.LinuxLocalOS;
import icu.etl.os.linux.LinuxProgress;
import icu.etl.util.CollUtils;
import icu.etl.util.Ensure;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * Apple Mac OS 操作系统的接口实现类
 */
@EasyBean(kind = "macos", mode = "local", major = "18", minor = "7")
public class MacOS extends LinuxLocalOS {

    public MacOS() throws IOException {
        super();
    }

    protected void init() {
        this.name = "macos";
        this.executeCommand("sw_vers");
        String stdout = this.cmd.getStdout();
        BufferedLineReader in = new BufferedLineReader(stdout);
        try {
            if (in.hasNext()) {
                String[] array = StringUtils.split(StringUtils.trimBlank(in.next()), ':');
                if (array.length == 2) {
                    this.name = StringUtils.trimBlank(array[1]);
                }
            }

            if (in.hasNext()) {
                String[] array = StringUtils.split(StringUtils.trimBlank(in.next()), ':');
                if (array.length == 2) {
                    this.release = StringUtils.trimBlank(array[1]);
                }
            }

            this.cmd.execute("uname -a");
            this.kernel = StringUtils.trimBlank(StringUtils.splitByBlank(this.cmd.getStdout())[2]);

            if (STD.out.isDebugEnabled()) {
                STD.out.debug(this.name + " " + this.kernel + " " + this.release);
            }
        } finally {
            IO.close(in);
        }
    }

    public List<OSProcess> getOSProgressList(String findStr) {
        String command = "ps -ef ";
        if (StringUtils.isNotBlank(findStr)) {
            command = "ps -ef | head -n 1; ps -ef | grep -v grep | grep " + findStr;
        }

        this.executeCommand(command);
        List<Map<String, String>> listmap = OSCommandUtils.splitPSCmdStdout(this.cmd.getStdout());
        List<OSProcess> list = new ArrayList<OSProcess>();
        for (Map<String, String> map : listmap) {
            LinuxProgress progress = new LinuxProgress(this);
            progress.setCpu(map.get("C"));
            progress.setMemory(StringUtils.isLong(map.get("SZ")) ? Long.parseLong(map.get("SZ")) : 0);
            progress.setPid(map.get("pid"));
            progress.setPpid(map.get("ppid"));
            progress.setCmd(map.get("cmd"));
            list.add(progress);
        }
        return list;
    }

    public List<OSCpu> getOSCpus() {
        List<String> commands = new ArrayList<String>();
        commands.add("modeName");
        commands.add("sysctl -n machdep.cpu.brand_string");

        commands.add("coreSize");
        commands.add("sysctl -n machdep.cpu.core_count");

        commands.add("cpuSize");
        commands.add("sysctl -n machdep.cpu.thread_count");

        commands.add("cacheSize");
        commands.add("sysctl -n machdep.cpu.cache.size");

        OSCommandStdouts map = this.cmd.execute(commands);
        int cpuSize = new Expression(StringUtils.join(map.get("cpuSize"), "")).intValue();
        int coreSize = new Expression(StringUtils.join(map.get("coreSize"), "")).intValue();
        BigDecimal cacheSize = new Expression(StringUtils.join(map.get("cacheSize"), "")).decimalValue();
        String modeName = StringUtils.join(map.get("modeName"), "");

        List<OSCpu> list = new ArrayList<OSCpu>();
        for (int i = 0, c = 0; i < cpuSize; i++) {
            OSProcessorImpl obj = new OSProcessorImpl();

            obj.setId(String.valueOf(i));
            obj.setModeName(modeName);
            obj.setCoreId(String.valueOf(c));
            obj.setCacheSize(cacheSize);
            obj.setCores(coreSize);
            obj.setPhysicalId(String.valueOf(c));
            obj.setSiblings(1);

            if (++c >= coreSize) {
                c = 0;
            }

            list.add(obj);
        }
        return list;
    }

    public List<OSDisk> getOSDisk() {
        List<OSDisk> list = new ArrayList<OSDisk>();
        this.cmd.execute("df -tl -b");
        BufferedLineReader in = new BufferedLineReader(StringUtils.trimBlank(this.cmd.getStdout()));
        try {
            List<String> titles = StringUtils.splitByBlank(StringUtils.trimBlank(in.next()), 6);
            int length = titles.size();
            String unit = StringUtils.split(titles.get(1), '-')[0];
            while (in.hasNext()) {
                String line = StringUtils.trimBlank(in.next());
                String[] array = StringUtils.splitByBlank(line);
                if (array.length < length) {
                    line = line + in.next();
                    array = CollUtils.toArray(StringUtils.splitByBlank(line, length));
                } else if (array.length > length) {
                    array = CollUtils.toArray(StringUtils.splitByBlank(line, length));
                }

                Ensure.isTrue(array.length == length, line, titles);

                OSDiskImpl obj = new OSDiskImpl();
                obj.setId(array[0]);
                obj.setType("");
                obj.setTotal(DataUnitExpression.parse(array[1] + "*" + unit));
                obj.setUsed(DataUnitExpression.parse(array[2] + "*" + unit));
                obj.setFree(DataUnitExpression.parse(array[3] + "*" + unit));
                obj.setAmount(array[5]);
                list.add(obj);
            }
            return list;
        } finally {
            IO.close(in);
        }
    }

    public OSMemory getOSMemory() {
        OSMemoryImpl obj = new OSMemoryImpl();

        List<String> commands = new ArrayList<String>();
        commands.add("total");
        commands.add("sysctl -n hw.memsize");

        commands.add("pageSizeByte");
        commands.add("vm_stat | grep \"page size of \" | awk -F\" \" '{print $8}'");

        commands.add("free");
        commands.add("vm_stat | grep \"Pages free\" | awk -F\" \" '{print $3}'");

        commands.add("active");
        commands.add("vm_stat | grep \"Pages active\" | awk -F\" \" '{print $3}'");

        OSCommandStdouts map = this.cmd.execute(commands);

        BigDecimal total = new Expression(StringUtils.join(map.get("total"), "")).decimalValue();
        int pageSizeByte = new Expression(StringUtils.join(map.get("pageSizeByte"), "").toString()).intValue();
        BigDecimal free = new Expression(StringUtils.join(map.get("free"), "") + "00 * " + pageSizeByte).decimalValue();
        BigDecimal active = new Expression(StringUtils.join(map.get("active"), "") + "00 * " + pageSizeByte).decimalValue();

        obj.setTotal(total);
        obj.setFree(free);
        obj.setActive(active);
        return obj;
    }

}
