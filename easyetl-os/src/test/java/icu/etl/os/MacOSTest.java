package icu.etl.os;

import java.util.List;

import icu.etl.os.macos.MacOS;
import icu.etl.util.Dates;
import icu.etl.util.TimeWatch;
import org.junit.Assert;
import org.junit.Test;

public class MacOSTest {

    @Test
    public void test() {
        TimeWatch watch = new TimeWatch();
        MacOS os = null;
        try {
            os = new MacOS();
            System.out.println(os.getName() + ", kernel: " + os.getKernelVersion() + ", release: " + os.getReleaseVersion());
            Assert.assertTrue(os.enableOSFileCommand());
            OSFileCommand fileCmd = os.getOSFileCommand();

            System.out.println("初始化用时: " + watch.useTime());
            Dates.sleep(2000);

            System.out.println("current user is " + os.getUser());
            System.out.println("exist / " + fileCmd.exists("/"));

            OSDateCommand datecmd = os.getOSDateCommand();
            System.out.println("current time is " + Dates.format19(datecmd.getDate()));

            watch.start();
            System.out.println();
            System.out.println();
            System.out.println();
            for (OSUser user : os.getUsers()) {
                System.out.println(user);
            }

            System.out.println();
            System.out.println();
            System.out.println();
            for (OSUserGroup obj : os.getGroups()) {
                System.out.println(obj);
            }

            System.out.println();
            System.out.println();
            System.out.println();
//			for (OSService obj : os.getOSServices()) {
//				System.out.println(obj);
//			}
            System.out.println("service: " + os.getOSService(60000));
            System.out.println("用时: " + watch.useTime());

            List<OSCpu> ps = os.getOSCpus();
            for (OSCpu p : ps) {
                System.out.println("cpus: " + p);
            }

            List<OSDisk> disks = os.getOSDisk();
            for (OSDisk d : disks) {
                System.out.println(d.toString());
            }

            OSMemory memorys = os.getOSMemory();
            System.out.println(memorys);

            watch.start();
            System.out.println();
            System.out.println();
            System.out.println();
            List<OSProcess> list = os.getOSProgressList("java");
            for (OSProcess process : list) {
                System.out.println("pid: " + process.getPid());
                System.out.println("ppid: " + process.getPPid());
                System.out.println("cmd: " + process.getCmd());
                System.out.println("name: " + process.getName());
                System.out.println("cpu: " + process.getCpu());
                System.out.println("memory: " + process.getMemory());
                System.out.println();
                System.out.println();
            }

            System.out.println("用时: " + watch.useTime());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            os.close();
        }
    }

}
