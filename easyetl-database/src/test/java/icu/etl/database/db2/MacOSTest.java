package icu.etl.database.db2;

import java.util.List;

import icu.etl.os.OSCpu;
import icu.etl.os.OSDateCommand;
import icu.etl.os.OSDisk;
import icu.etl.os.OSFile;
import icu.etl.os.OSFileCommand;
import icu.etl.os.OSMemory;
import icu.etl.os.OSProcess;
import icu.etl.os.OSUser;
import icu.etl.os.OSUserGroup;
import icu.etl.os.macos.MacOS;
import icu.etl.util.Dates;
import icu.etl.util.TimeWatch;

public class MacOSTest {

    public static void main(String[] args) {
        TimeWatch watch = new TimeWatch();
        MacOS os = null;
        try {
            os = new MacOS();
            System.out.println(os.getName() + ", kernel: " + os.getKernelVersion() + ", release: " + os.getReleaseVersion());
            os.enableOSFileCommand();
            OSFileCommand fileCmd = os.getOSFileCommand();

            System.out.println("初始化用时: " + watch.useTime());
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("current user is " + os.getUser());

            System.out.println("exist / " + fileCmd.exists("/"));

            OSDateCommand datecmd = os.getOSDateCommand();
            System.out.println("current time is " + Dates.format19(datecmd.getDate()));

            watch.start();
            System.out.println("");
            System.out.println("");
            System.out.println("");
            for (OSUser user : os.getUsers()) {
                System.out.println(user);
            }

            System.out.println("");
            System.out.println("");
            System.out.println("");
            for (OSUserGroup obj : os.getGroups()) {
                System.out.println(obj);
            }

            System.out.println("");
            System.out.println("");
            System.out.println("");
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
            System.out.println("");
            System.out.println("");
            System.out.println("");
            List<OSProcess> array = os.getOSProgressList("java");
            for (OSProcess p : array) {
                OSProcess osp = os.getOSProgress(p.getPid());
                if (osp == null) {
                    throw new NullPointerException();
                }
            }
            System.out.println("用时: " + watch.useTime());

            System.out.println("");
            System.out.println("");
            System.out.println("");
            watch.start();
            List<DB2Instance> insts = DB2Instance.get(os);
            for (DB2Instance inst : insts) {
                System.out.println(inst);

                os.enableOSFileCommand();
                List<OSFile> find = os.getOSFileCommand().find(inst.getUser().getHome(), "db2profile", 'f', null);
                for (OSFile f : find) {
                    System.out.println("db2profile is " + f);
                }
            }
            System.out.println("用时: " + watch.useTime());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            os.close();
        }
    }
}
