package icu.etl.io;

import java.io.IOException;
import java.io.Writer;

import icu.etl.annotation.EasyBean;
import icu.etl.annotation.EasyBeanClass;
import icu.etl.ioc.BeanFactory;
import icu.etl.util.Attribute;

/**
 * 表格型数据文件接口 <br>
 * <br>
 * 使用 {@linkplain BeanFactory#get(Class, Object...)} 语句返回一个 {@linkplain TextTableFile} 表格型文件对象 <br>
 * 第一参数必须是 {@linkplain TextTableFile} <br>
 * 第二个参数必须是文件类型，详见表格型文件类上的 {@linkplain EasyBeanClass#type()} 属性值 <br>
 * 第三个参数必须是 {@linkplain Attribute} 对象的引用，属性集合中可以设置 charset，codepage，chardel，rowdel，coldel，escape，column，colname <br>
 * <br>
 * 实现类注解的填写规则: <br>
 * {@linkplain EasyBeanClass#kind()} 属性表示语言, 如: txt, del, fex <br>
 * {@linkplain EasyBeanClass#mode()} 属性填写存储类型, 如: file, net <br>
 * {@linkplain EasyBeanClass#major()} 属性未使用, 填空字符串 <br>
 * {@linkplain EasyBeanClass#minor()} 属性未使用, 填空字符串 <br>
 * {@linkplain EasyBeanClass#description()} 属性表示描述信息 <br>
 * {@linkplain EasyBeanClass#type()} 属性必须填写 {@linkplain TextTableFile}.class <br>
 * <br>
 *
 * @author jeremy8551@qq.com
 * @createtime 2011-07-18
 */
@EasyBean(builder = TextTableFileBuilder.class)
public interface TextTableFile extends TextTable, TextFile {

    /**
     * 返回记录分隔规则
     *
     * @return
     */
    TableLineRuler getRuler();

    /**
     * 返回数据文件的输入流
     *
     * @param cache 缓冲区大小，单位：字符
     * @return
     * @throws IOException
     */
    TextTableFileReader getReader(int cache) throws IOException;

    /**
     * 返回数据文件的输入流
     *
     * @param start  输入流的起始位置, 从 0 开始 <br>
     *               如果起始位置不是一行内容的开始位置，则自动从下一行的起始位置开始读取 <br>
     * @param length 输入流读取的最大字节总数
     * @param cache  缓冲区大小，单位：字符
     * @return
     * @throws IOException
     */
    TextTableFileReader getReader(long start, long length, int cache) throws IOException;

    /**
     * 返回数据文件的输出流
     *
     * @param append true表示追加写入记录 <br>
     *               false表示覆盖原有记录
     * @param cache  缓存行数
     * @return
     * @throws IOException
     */
    TextTableFileWriter getWriter(boolean append, int cache) throws IOException;

    TextTableFileWriter getWriter(Writer writer, int cache) throws IOException;

    /**
     * 统计文本表格型文件字段个数
     *
     * @return 文件中字段个数
     * @throws IOException
     */
    int countColumn() throws IOException;

    TextTableFile clone();

}