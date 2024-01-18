package icu.etl.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import icu.etl.log.Log;
import icu.etl.log.LogFactory;
import icu.etl.printer.Progress;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

public class CommonTextTableFileReaderListener implements TextTableFileReaderListener {
    private final static Log log = LogFactory.getLog(CommonTextTableFileReaderListener.class);

    private Progress progress;

    public CommonTextTableFileReaderListener() {
    }

    /**
     * 创建一个带文件读取进度输出的监听器
     *
     * @param progress
     */
    public CommonTextTableFileReaderListener(Progress progress) {
        this();
        this.progress = progress;
    }

    /**
     * 设置进度输出接口
     *
     * @param progress
     */
    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    /**
     * 删除字段值中的回车符或换行符
     */
    public void processLineSeparator(TextTableFile file, TextTableLine line, long lineNumber) throws IOException {
        int column = line.getColumn();

        // 替换字段值中的回车换行符
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 1; i <= column; i++) {
            String value = line.getColumn(i);
            StringBuilder buf = StringUtils.removeLineSeparator(value);
            if (buf.length() != value.length()) {
                line.setColumn(i, buf.toString());
                list.add(i);
            }
        }

        // 替换当前行内容中的回车换行符
        String old = line.getContent();
        StringBuilder content = StringUtils.removeLineSeparator(old);
        line.setContext(content.toString());

        if (log.isWarnEnabled()) {
            log.warn(ResourcesUtils.getMessage("io.standard.output.msg011", file.getAbsolutePath(), lineNumber, StringUtils.join(list, ", "), StringUtils.escapeLineSeparator(old)));
        }
    }

    public boolean processLine(TextTableFile file, TextTableLine line, long lineNumber) throws IOException {
        if (this.progress != null) {
            this.progress.print();
        }
        return false;
    }

    public boolean processColumnException(TextTableFileReader in, String line, long lineNumber) throws IOException {
        return true;
    }

}