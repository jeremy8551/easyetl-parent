package icu.etl.database;

import java.io.IOException;
import java.util.List;

import icu.etl.database.db2.DB2Instance;
import icu.etl.ioc.BeanFactory;
import icu.etl.os.OS;
import icu.etl.os.OSCpu;
import icu.etl.os.OSDisk;
import icu.etl.os.OSFile;
import icu.etl.os.OSFileCommand;
import icu.etl.os.OSMemory;
import icu.etl.os.OSNetworkCard;
import icu.etl.os.OSProcess;
import icu.etl.os.OSUser;
import icu.etl.os.OSUserGroup;
import icu.etl.time.Timer;
import icu.etl.util.TimeWatch;

public class 测试远程连接Linux与DB2和WAS探测 {

    public static void main(String[] args) throws IOException {
        String host = TestEnv.getSSHHost();
        int port = TestEnv.getSSHPort();
        String username = TestEnv.getSSHUsername();
        String password = TestEnv.getSSHPassword();

        host = "130.1.10.10";
        port = 22;
        username = "etl@was@130.1.16.54";
        password = "xxx";

        TimeWatch watch = new TimeWatch();
        OS os = BeanFactory.get(OS.class, host, port, username, password);
        try {
            System.out.println();
            System.out.println();
            System.out.println("操作系统名: " + os.getName());

            System.out.println();
            System.out.println();
            System.out.println("内核版本号: " + os.getKernelVersion());

            System.out.println();
            System.out.println();
            System.out.println("发行版本号: " + os.getReleaseVersion());

            System.out.println();
            System.out.println();
            System.out.println("打开操作系统的命令操作接口: " + os.enableOSCommand());

            System.out.println();
            System.out.println();
            System.out.println("打开操作系统的文件操作接口: " + os.enableOSFileCommand());
            OSFileCommand fileCmd = os.getOSFileCommand();

            watch.start();

            System.out.println();
            System.out.println();
            System.out.println("处理器信息: ");
            List<OSCpu> ps = os.getOSCpus();
            for (OSCpu p : ps) {
                System.out.println("cpus: " + p);
            }

            System.out.println();
            System.out.println();
            System.out.println("内存信息: ");
            OSMemory mems = os.getOSMemory();
            System.out.println("mems: " + mems);

            System.out.println();
            System.out.println();
            System.out.println("硬盘信息: ");
            List<OSDisk> disks = os.getOSDisk();
            for (OSDisk disk : disks) {
                System.out.println("disk: " + disk);
            }

            System.out.println();
            System.out.println();
            System.out.println("网卡信息: ");
            List<OSNetworkCard> cards = os.getOSNetwork().getOSNetworkCards();
            for (OSNetworkCard c : cards) {
                System.out.println("nets: " + c);
            }
            System.out.println("用时: " + watch.useTime());

            System.out.println("初始化用时: " + watch.useTime());
            Timer.sleep(2000);
            System.out.println("当前用户是: " + os.getUser());
            System.out.println("判断目录 " + os.getUser().getHome() + " 是否存在: " + fileCmd.exists(os.getUser().getHome()));

            watch.start();
            System.out.println("");
            System.out.println("");
            System.out.println("用户信息: ");
            for (OSUser user : os.getUsers()) {
                System.out.println(user);
            }

            System.out.println("");
            System.out.println("");
            System.out.println("用户组信息: ");
            for (OSUserGroup obj : os.getGroups()) {
                System.out.println(obj);
            }

            watch.start();
            System.out.println("");
            System.out.println("");
            System.out.println("查找 java 进程");
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

                String[] databaseNames = inst.getDatabaseNames();
                for (String databaseName : databaseNames) {
                    System.out.println();
                    System.out.println();
                    int dbport = inst.getDatabase(databaseName).getPort();
//					dbport = inst.getPort();
                    System.out.println("在实例 " + inst.getName() + " 上查找数据库 " + databaseName + " 端口号 " + dbport + " 对应的服务信息: " + os.getOSService(dbport));
                }

                os.enableOSFileCommand();
                List<OSFile> find = os.getOSFileCommand().find(inst.getUser().getHome(), "db2profile", 'f', null);
                for (OSFile f : find) {
                    System.out.println();
                    System.out.println("数据库配置文件在 " + f);
                }
            }
            System.out.println("用时: " + watch.useTime());
        } finally {
            os.close();
        }
    }

}
