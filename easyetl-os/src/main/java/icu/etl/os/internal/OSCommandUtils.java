package icu.etl.os.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.io.BufferedLineReader;
import icu.etl.os.OSCommandStdouts;
import icu.etl.util.Ensure;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

public class OSCommandUtils {

    public final static String START_PREFIX = "[*|*|*|*|*|*|*]";

    private OSCommandUtils() {
    }

    /**
     * 将多命令输出解析为一行
     *
     * @param list
     * @return
     */
    public static String join(List<String> list) {
        return list == null ? "" : StringUtils.trimBlank(StringUtils.join(list, ""));
    }

    /**
     * 与 {@link #splitMultiCommandStdout(CharSequence)} 配套使用
     *
     * @param commands
     * @return
     */
    public static String toMultiCommand(List<String> commands) {
        return toMultiCommand(OSCommandUtils.START_PREFIX, commands.toArray(new String[commands.size()]));
    }

    /**
     * 与 {@link #splitMultiCommandStdout(String, CharSequence)} 配套使用
     *
     * @param prefix
     * @param cmds
     * @return
     */
    private static String toMultiCommand(String prefix, CharSequence... cmds) {
        Ensure.isTrue(cmds.length % 2 == 0, StringUtils.toString(cmds));
        Ensure.isTrue(StringUtils.isNotBlank(prefix) && prefix.indexOf(';') == -1 && prefix.indexOf("\"") == -1, prefix, cmds);

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < cmds.length; i++) {
            String key = cmds[i].toString();
            String value = cmds[++i].toString();
            Ensure.isTrue(key.indexOf(';') == -1 && key.indexOf("\"") == -1, key, value);

            buf.append("echo \"");
            buf.append(prefix);
            buf.append(key);
            buf.append("\"");
            buf.append("; ");
            buf.append(StringUtils.trimBlank(value, ';'));
            buf.append("; ");
        }
        return buf.toString();
    }

    /**
     * 解析 ps 命令的输出
     *
     * @param stdout 标准输出信息
     * @param titles 输出信息标题, 为空时自动使用stdout第一行作为标题行解析
     * @return
     */
    public static List<Map<String, String>> splitPSCmdStdout(CharSequence stdout, String... titles) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        if (StringUtils.isNotBlank(stdout)) {
            BufferedLineReader in = new BufferedLineReader(StringUtils.ltrimBlank(stdout));
            try {
                if (titles == null || titles.length == 0) {
                    if (in.hasNext()) {
                        String line = in.next();
                        String names = StringUtils.trimBlank(line);
                        titles = StringUtils.splitByBlank(names);
                    } else {
                        return list;
                    }
                }

                Ensure.isTrue(StringUtils.indexOf(titles, "pid", 0, titles.length - 1, true) != -1, stdout, titles);
                while (in.hasNext()) {
                    String line = StringUtils.trimBlank(in.next());
                    List<String> array = StringUtils.splitByBlank(line, titles.length);

                    Map<String, String> map = new CaseSensitivMap<String>();
                    for (int i = 0; i < titles.length; i++) {
                        String key = StringUtils.trimBlank(titles[i]);
                        map.put(key, array.get(i));
                    }
                    list.add(map);
                }
            } finally {
                IO.close(in);
            }
        }
        return list;
    }

    /**
     * 分隔多命令输出信息 <br>
     * 与 {@linkplain #toMultiCommand(List)} 配套使用
     *
     * @param stdout 命令信息
     * @return
     */
    public static OSCommandStdouts splitMultiCommandStdout(CharSequence stdout) {
        return splitMultiCommandStdout(OSCommandUtils.START_PREFIX, stdout);
    }

    /**
     * 分隔多命令输出信息
     *
     * @param prefix 每个命令输出的前缀, 后面是命令结果标志
     * @param stdout
     * @return
     */
    private static OSCommandStdouts splitMultiCommandStdout(String prefix, CharSequence stdout) {
        if (StringUtils.isBlank(stdout)) {
            throw new IllegalArgumentException(StringUtils.toString(stdout));
        }

        String key = "";
        OSCommandStdoutsImpl map = new OSCommandStdoutsImpl();
        List<String> list = new ArrayList<String>();
        BufferedLineReader in = new BufferedLineReader(stdout);
        try {
            while (in.hasNext()) {
                String line = in.next();
                if (line.startsWith(prefix)) {
                    if (key.length() == 0) { // for first row
                        key = StringUtils.trimBlank(line.substring(prefix.length()));
                        continue;
                    } else {
                        if (StringUtils.isBlank(key)) {
                            throw new IllegalArgumentException(key);
                        }

                        map.put(key, new ArrayList<String>(list));
                        list.clear();
                        key = StringUtils.trimBlank(line.substring(prefix.length()));
                        continue;
                    }
                } else {
                    list.add(line);
                }
            }

            map.put(key, new ArrayList<String>(list));
            list.clear();
            return map;
        } finally {
            IO.close(in);
        }
    }
}
