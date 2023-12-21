package icu.etl.expression;

import icu.etl.util.StringUtils;

/**
 * 单词类
 */
public class Word {

    /** 单词内容 */
    private String content;

    /** 起始位置 */
    private int start;

    /** 结束位置 */
    private int end;

    /**
     * 单词
     *
     * @param start   起始位置（包含）
     * @param end     结束位置（不包含）
     * @param content 单词内容
     */
    public Word(int start, int end, CharSequence content) {
        this.start = start;
        this.end = end;
        this.content = StringUtils.trimBlank(content);
    }

    /**
     * 单词内容
     *
     * @return 单词内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 单词在原字符串中的起始位置
     *
     * @return 起始位置
     */
    public int getBegin() {
        return start;
    }

    /**
     * 返回单词结束位置（不包含）
     *
     * @return 结束位置（不包含）
     */
    public int getEnd() {
        return this.end;
    }
}