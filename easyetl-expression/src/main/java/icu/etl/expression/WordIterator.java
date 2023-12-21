package icu.etl.expression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import icu.etl.util.Ensure;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 单词字符串输入流，每次读取一个单词
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-05-21
 */
public class WordIterator implements Iterator<String> {

    /** 单词序列 */
    private CharSequence src;

    /** 单词集合 */
    private List<Word> list;

    /** 当前位置 */
    private int index;

    /** 右侧位置 */
    private int last;

    /** 标记的位置 */
    private int mark;

    /** 语句分析器 */
    private Analysis analysis;

    /**
     * 初始化
     *
     * @param analysis 词法分析器
     * @param str      字符串
     */
    public WordIterator(Analysis analysis, CharSequence str) {
        this.analysis = Ensure.notNull(analysis);
        this.src = Ensure.notNull(str);
        this.index = 0;
        this.mark = 0;
        this.list = this.parse(str);
        this.last = this.list.isEmpty() ? 0 : this.list.size() - 1;
    }

    /**
     * 解析字符串中的单词
     *
     * @param str 字符串
     */
    protected List<Word> parse(CharSequence str) {
        ArrayList<Word> list = new ArrayList<Word>();
        for (int i = 0, start = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c)) { // 查找单词起始位置
                int end = this.analysis.indexOfWhitespace(str, i); // 搜索单词结束位置
                if (end == -1) {
                    end = str.length();
                }

                // 将单词保存到缓存队列中
                CharSequence word = str.subSequence(start, end);
                list.add(new Word(start, end, word));
                i = end;
                start = end;
            }
        }
        return list;
    }

    /**
     * 判断是否可以通过 {@linkplain #next()} 方法读取一下单词
     *
     * @return 返回 true 表示可以读取下一个单词，false 表示无单词可以返回
     */
    public boolean hasNext() {
        return this.index >= 0 && this.index <= this.last && this.index < this.list.size();
    }

    /**
     * 读取下一个单词
     *
     * @return 返回空字符串表示已读取到最后一个单词
     */
    public String next() {
        if (this.hasNext()) {
            return this.list.get(this.index++).getContent();
        } else {
            return "";
        }
    }

    /**
     * 读取最右侧的单词
     *
     * @return 单词
     */
    public String last() {
        if (this.last >= 0 && this.last >= this.index && this.last < this.list.size()) {
            String str = this.list.get(this.last).getContent();
            if (--this.last < -1) { // 结束位置不能小于 -1
                this.last = -1;
            }
            return str;
        } else {
            return "";
        }
    }

    /**
     * 判断最后一个单词是否等于字符串参数 word
     *
     * @param word 单词
     * @return true 表示相等
     */
    public boolean isLast(String word) {
        if (this.last >= 0 && this.last >= this.index && this.last < this.list.size()) {
            String str = this.list.get(this.last).getContent();
            return str.equals(word);
        } else {
            return false;
        }
    }

    /**
     * 不支持删除元素
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * 判断下一个单词是否等于单词参数 word
     *
     * @param word 单词
     * @return 返回true表示参数与下一个单词相等 false表示参数与下一个单词不等
     */
    public boolean isNext(String word) {
        if (this.hasNext()) {
            String next = this.list.get(this.index).getContent();
            return this.analysis.equals(next, word);
        } else {
            return word == null;
        }
    }

    /**
     * 判断下一个单词是否等于单词参数 word
     *
     * @param words 单词
     * @return 返回true表示参数与下一个单词相等 false表示参数与下一个单词不等
     */
    public boolean isNext(String[] words) {
        if (words == null) {
            return !this.hasNext();
        } else {
            if (this.hasNext()) {
                String next = this.list.get(this.index).getContent();
                for (String word : words) {
                    if (this.analysis.equals(next, word)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * 预览（不会读取）下一个单词
     *
     * @return 单词
     */
    public String previewNext() {
        if (this.hasNext()) {
            return this.list.get(this.index).getContent();
        } else {
            return null;
        }
    }

    /**
     * 预览所有未读单词
     *
     * @return 返回 null 表示所有单词都已读取完毕
     */
    public String previewOther() {
        if (this.hasNext()) {
            Word start = this.list.get(this.index);
            Word last = this.list.get(this.last);
            return this.src.subSequence(start.getBegin(), last.getEnd()).toString();
        } else {
            return "";
        }
    }

    /**
     * 判断当前单词是否与输入参数 word 相等
     *
     * @param word 单词
     * @return true表示相等
     */
    public boolean equals(String word) {
        int index = this.index - 1;
        if (index >= 0 && index <= this.last && index < this.list.size()) {
            return this.analysis.equals(word, this.list.get(index).getContent());
        } else {
            return false;
        }
    }

    /**
     * 读取到指定位置
     *
     * @param rule 查询规则
     * @return 读取到的字符串
     */
    public String read(WordQuery rule) {
        int begin = this.list.get(this.index).getBegin();
        int end = rule.indexOf(this.src, this.list, this.index, this.last);
        if (end == -1) {
            throw new IllegalArgumentException();
        } else {
            Word obj = this.list.get(end);
            String str = this.src.subSequence(begin, obj.getBegin()).toString();
            this.index = end + 1;
            return StringUtils.trimBlank(str);
        }
    }

    /**
     * 从当前位置开始向右读取单词，直到遇见与字符串数组 word 中相同的单词。
     *
     * @param words 单词数组（返回值不包含字符串参数本身）
     * @return 默认删除字符串二段的空白字符
     */
    public String readUntil(String... words) {
        if (words.length == 0) { // 一直截取到字符串末尾
            throw new IllegalArgumentException();
        } else {
            for (int i = this.index; i <= this.last && i < this.list.size(); i++) {
                Word obj = this.list.get(i);
                String word = obj.getContent();

                if (this.analysis.exists(word, words)) {
                    String str = this.src.subSequence(this.list.get(this.index).getBegin(), obj.getBegin()).toString();
                    this.index = i;
                    this.assertNext(word);
                    return StringUtils.trimBlank(str);
                }
            }
            throw new UnsupportedOperationException(ResourcesUtils.getIoxMessage(39, this.src, this.index + 1, words)); // 在字符串 str 中找不到单词 str
        }
    }

    /**
     * 读取全部剩余单词
     *
     * @return 不可能是 null，如果没有单词可读时返回一个长度为零的空字符串（默认删除字符串二段的空白字符）
     */
    public String readOther() {
        if (this.hasNext()) {
            Word start = this.list.get(this.index);
            Word last = this.list.get(this.last);
            String str = this.src.subSequence(start.getBegin(), last.getEnd()).toString();
            this.index = this.last + 1;
            return StringUtils.trimBlank(str);
        } else {
            return "";
        }
    }

    /**
     * 读取下一个单词，且单词必须与单词参数 word 相同。
     *
     * @param word 单词
     */
    public void assertNext(String word) {
        String next = this.next();
        if (word == null) {
            if (next != null && next.length() != 0) {
                throw new IllegalArgumentException();
            }
        } else {
            if (!this.analysis.equals(word, next)) {
                throw new IllegalArgumentException(word);
            }
        }
    }

    /**
     * 读取下一个单词，且单词必须在单词数组 words 范围内。
     *
     * @param words 单词数组
     */
    public void assertNext(String[] words) {
        String next = this.next();
        if (words == null) {
            if (next != null) {
                throw new IllegalArgumentException();
            }
        } else {
            for (String str : words) {
                if (this.analysis.equals(str, next)) {
                    return;
                }
            }
            throw new IllegalArgumentException(next + " -> " + StringUtils.toString(words));
        }
    }

    /**
     * 读取最后一个单词，并判断与输入字符串参数 str 是否相等，如果不等则抛出异常
     *
     * @param word 单词
     */
    public void assertLast(String word) {
        String last = this.last();
        if (word == null) {
            if (last != null && last.length() != 0) {
                throw new IllegalArgumentException();
            }
        } else {
            if (!this.analysis.equals(word, last)) {
                throw new IllegalArgumentException(word + " != " + last + ", src: " + this.src);
            }
        }
    }

    /**
     * 判断单词序列已无单词可以读取
     */
    public void assertOver() {
        if (this.hasNext()) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 将剩余全部单词转为集合
     *
     * @return 单词集合
     */
    public List<String> asList() {
        List<String> list = new ArrayList<String>(this.list.size());
        for (int i = this.index; i >= 0 && i <= this.last && i < this.list.size(); i++) {
            list.add(this.list.get(i).getContent());
        }
        return list;
    }

    /**
     * 标记当前位置
     */
    public void mark() {
        this.mark = this.index;
    }

    /**
     * 恢复上一次标记的位置
     */
    public void reset() {
        this.index = this.mark;
    }

    /**
     * 返回第几个单词
     *
     * @return 返回值从 1 开始，返回 0 表示还未读取单词
     */
    public int getPosition() {
        return this.index;
    }

    public String toString() {
        return this.src.toString();
    }

}
