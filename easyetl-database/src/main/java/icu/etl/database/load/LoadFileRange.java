package icu.etl.database.load;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import icu.etl.io.TextTableFile;
import icu.etl.util.CollectionUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 数据装载范围
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-07-16
 */
public class LoadFileRange {

    private long start;

    private long end;

    private int status;

    public LoadFileRange(long start, long end, int status) {
        this.start = start;
        this.end = end;
        this.status = status;
    }

    /**
     * 返回起始位置，从1开始
     *
     * @return
     */
    public long getStart() {
        return start;
    }

    /**
     * 返回终止位置，从1开始
     *
     * @return
     */
    public long getEnd() {
        return end;
    }

    /**
     * 返回状态， -1表示还未开始装载数据 0表示正在装载数据 1表示发生错误 2表示装载完毕
     *
     * @return
     */
    public int getStatus() {
        return status;
    }

    /**
     * 将范围集合专为字符串
     *
     * @param list
     * @return
     */
    public static String toString(List<LoadFileRange> list) {
        if (list == null) {
            return "";
        }

        StringBuilder buf = new StringBuilder();
        for (LoadFileRange range : list) {
            buf.append(range.start).append(",");
            buf.append(range.end).append(",");
            buf.append(range.status).append(";");
        }
        return buf.toString();
    }

    /**
     * 将字节出解析为范围集合
     *
     * @param str
     * @return
     * @throws IOException
     */
    public static List<LoadFileRange> parseString(String str) throws IOException {
        List<LoadFileRange> list = new ArrayList<LoadFileRange>();
        if (str != null) {
            String[] array = StringUtils.split(str, ';');
            for (String range : array) {
                if (StringUtils.isBlank(range)) {
                    continue;
                }

                String[] split = StringUtils.split(range, ',');
                if (split.length != 3) {
                    throw new IOException(ResourcesUtils.getMessage("load.standard.output.msg002", range));
                }
                if (!StringUtils.isLong(split[0])) {
                    throw new IOException(ResourcesUtils.getMessage("load.standard.output.msg003", range));
                }
                if (!StringUtils.isLong(split[1])) {
                    throw new IOException(ResourcesUtils.getMessage("load.standard.output.msg003", range));
                }
                if (!StringUtils.inArray(split[2], "-1", "0", "1", "2")) {
                    throw new IOException(ResourcesUtils.getMessage("load.standard.output.msg003", range));
                }

                LoadFileRange obj = new LoadFileRange(Long.parseLong(split[0]), Long.parseLong(split[1]), Integer.parseInt(split[2]));
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 返回数据文件中未装载数据的范围集合
     *
     * @param file
     * @param expression
     * @param thread
     * @throws IOException
     */
    public static List<LoadFileRange> parseList(TextTableFile file, String expression, int thread) throws IOException {
        List<LoadFileRange> result = new ArrayList<LoadFileRange>();
        List<LoadFileRange> list = LoadFileRange.parseString(expression);

        // 按起始位置排序
        Collections.sort(list, new Comparator<LoadFileRange>() {

            public int compare(LoadFileRange o1, LoadFileRange o2) {
                long val = o1.getStart() - o2.getStart();
                if (val == 0) {
                    return 0;
                } else if (val > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }

        });

        LoadFileRange range = CollectionUtils.lastElement(list);
        long length = file.getFile().length();
        if (range == null || range.getEnd() != length) { // 文件范围跟文件实际大小不一致时，从新生成数据加载范围集合
            long size = (length == 0 || thread == 0) ? 0 : (length / thread);
            if (size <= 0) {
                size = length;
            }

            long start = 1;
            while (start < length) {
                long end = start + size; // 范围的终止位置
                if (end > length) {
                    end = length;
                    result.add(new LoadFileRange(start, end, -1));
                    break;
                } else {
                    result.add(new LoadFileRange(start, end, -1));
                    start = end + 1;
                    continue;
                }
            }
        } else {
            for (LoadFileRange obj : list) {
                int status = obj.getStatus();
                if (status == -1 || status == 0 || status == 1) { // 只执行上次未加载的范围
                    result.add(obj);
                }
            }
        }

        return result;
    }

}
