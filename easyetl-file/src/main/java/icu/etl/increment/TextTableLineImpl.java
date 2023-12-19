package icu.etl.increment;

import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableLine;
import icu.etl.util.Ensure;

/**
 * 将排序文件记录转为原文件记录 <br>
 * <br>
 * 因为排序时会在排序文件记录的最右侧，增加一个字段用于存储记录在原文件中的行号 <br>
 * 所以在执行增量剥离时，需要将排序产生的行号字段排除掉
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/12/4
 */
public class TextTableLineImpl implements TextTableLine {

    private TextTableFile file;

    private TextTableLine line;

    private String content;

    private long lineNumber;

    private int column;

    public TextTableLineImpl(TextTableFile file, TextTableLine line) {
        this.file = file;
        this.line = line;
        String content = this.line.getContent();
        int index = Ensure.isFromZero(content.lastIndexOf(file.getDelimiter()));
        this.content = content.substring(0, index);
        int count = this.line.getColumn(); // 字段总数
        this.column = count - 1; // 有效字段个数，减掉最后的行号字段
        this.lineNumber = Long.parseLong(this.line.getColumn(count)); // 最后一个字段是记录在原文件中的行号
    }

    public long getLineNumber() {
        return this.lineNumber;
    }

    public String getLineSeparator() {
        return this.line.getLineSeparator();
    }

    public boolean isColumnBlank(int position) {
        if (position > this.column) {
            throw new IllegalArgumentException(String.valueOf(position));
        }
        return this.line.isColumnBlank(position);
    }

    public String getColumn(int position) {
        if (position > this.column) {
            throw new IllegalArgumentException(String.valueOf(position));
        }
        return this.line.getColumn(position);
    }

    public void setColumn(int position, String value) {
        if (position > this.column) {
            throw new IllegalArgumentException(String.valueOf(position));
        }
        this.line.setColumn(position, value);
    }

    public int getColumn() {
        return this.column;
    }

    public String getContent() {
        return this.content;
    }

    public void setContext(String line) {
        this.content = line;
        this.line.setContext(line + this.file.getDelimiter() + this.lineNumber);
    }
}
