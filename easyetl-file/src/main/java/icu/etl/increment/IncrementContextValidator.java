package icu.etl.increment;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileWriter;
import icu.etl.util.ResourcesUtils;

/**
 * 校验剥离增量上下文信息中的参数是否正确
 *
 * @author jeremy8551@qq.com
 */
public class IncrementContextValidator {

    /**
     * 初始化
     *
     * @param context 上下文信息
     */
    public IncrementContextValidator(IncrementContext context) {
        this.checkReaderAndWriter(context);
        this.checkPosition(context);
        this.checkListeners(context);
    }

    /**
     * 校验监听器
     *
     * @param context 上下文信息
     */
    protected void checkListeners(IncrementContext context) {
        IncrementListenerImpl logger = context.getLogger();
        IncrementReplaceListener replaces = context.getReplaceList();
        List<IncrementListener> listeners = context.getListeners();
        if (listeners != null) {
            for (IncrementListener obj : listeners) {
                if (obj.equals(logger)) {
                    throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(35));
                } else if (obj.equals(replaces)) {
                    throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(36));
                }
            }
        }
    }

    /**
     * 校验输入输出流
     *
     * @param context 上下文信息
     */
    protected void checkReaderAndWriter(IncrementContext context) {
        TextTableFile oldtabfile = context.getOldFile();
        TextTableFile newtabfile = context.getNewFile();
        TextTableFileWriter newOuter = context.getNewWriter();
        TextTableFileWriter updOuter = context.getUpdWriter();
        TextTableFileWriter delOuter = context.getDelWriter();

        if (newtabfile == null) {
            throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(12));
        }
        if (oldtabfile == null) {
            throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(13));
        }

        File newfile = newtabfile.getFile();
        File oldfile = oldtabfile.getFile();

        // 新文件与旧文件不能相等
        if (oldfile.equals(newfile)) {
            throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(14));
        }

        // 输出文件与新旧文件不能相等
        if (newOuter != null) {
            File createfile = new File(newOuter.getTable().getAbsolutePath());
            if (createfile.equals(oldfile) || createfile.equals(newfile)) {
                throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(15));
            }
        }

        if (updOuter != null) {
            File updatefile = new File(updOuter.getTable().getAbsolutePath());
            if (updatefile.equals(oldfile) || updatefile.equals(newfile)) {
                throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(16));
            }
        }

        if (delOuter != null) {
            File deletefile = new File(delOuter.getTable().getAbsolutePath());
            if (deletefile.equals(oldfile) || deletefile.equals(newfile)) {
                throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(17));
            }
        }
    }

    /**
     * 校验位置信息
     *
     * @param context 上下文信息
     */
    protected void checkPosition(IncrementContext context) {
        IncrementPosition position = context.getPosition();
        if (position == null || position.getNewIndexPosition().length == 0 || position.getOldIndexPosition().length == 0) {
            throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(25));
        }
        if (position.getNewIndexPosition().length != position.getOldIndexPosition().length) {
            throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(26));
        }
        if (position.getNewComparePosition().length != position.getNewComparePosition().length) {
            throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(27));
        }

        // 新文件中索引字段位置信息
        Set<Integer> set = new HashSet<Integer>(position.getNewIndexPosition().length);
        for (int i : position.getNewIndexPosition()) {
            if (set.contains(i)) {
                throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(71, i));
            } else {
                set.add(i);
            }
        }

        // 旧文件中索引字段位置信息
        set.clear();
        for (int i : position.getOldIndexPosition()) {
            if (set.contains(i)) {
                throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(71, i));
            } else {
                set.add(i);
            }
        }

        for (int i : position.getNewIndexPosition()) {
            if (i <= 0) {
                throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(28));
            }
        }

        for (int i : position.getOldIndexPosition()) {
            if (i <= 0) {
                throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(29));
            }
        }

        for (int i : position.getNewComparePosition()) {
            if (i <= 0) {
                throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(30));
            }
        }

        for (int i : position.getOldComparePosition()) {
            if (i <= 0) {
                throw new IllegalArgumentException(ResourcesUtils.getIncrementMessage(31));
            }
        }
    }

}
