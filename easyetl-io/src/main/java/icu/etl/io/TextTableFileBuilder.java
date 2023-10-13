package icu.etl.io;

import java.nio.charset.StandardCharsets;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.BeanContext;
import icu.etl.ioc.BeanFactory;
import icu.etl.ioc.Codepage;
import icu.etl.util.Attribute;
import icu.etl.util.ClassUtils;
import icu.etl.util.StringUtils;

/**
 * 使用 {@linkplain BeanFactory#get(Class, Object...)} 语句返回一个 {@linkplain TextTableFile} 表格型文件对象 <br>
 * 第一参数必须是 {@linkplain TextTableFile} <br>
 * 第二个参数必须是文件类型，详见表格型文件类上的 {@linkplain EasyBeanClass#type()} 属性值 <br>
 * 第三个参数必须是 {@linkplain Attribute} 对象的引用，属性集合中可以设置 charset，codepage，chardel，rowdel，coldel，escape，column，colname
 *
 * @author jeremy8551@qq.com
 */
public class TextTableFileBuilder implements BeanBuilder<TextTableFile> {

    @SuppressWarnings("unchecked")
    public TextTableFile build(BeanContext context, Object... array) throws Exception {
        Class<TextTableFile> cls = context.getImplement(TextTableFile.class, array);
        TextTableFile file = ClassUtils.newInstance(cls);

        // 设置数据类型的属性
        for (Object obj : array) {
            if (obj instanceof Attribute) {
                Attribute<String> attribute = (Attribute<String>) obj;
                this.setProperty(file, attribute);
            }
        }

        return file;
    }

    public void setProperty(TextTableFile file, Attribute<String> attribute) {
        if (attribute.contains("charset") && attribute.contains("codepage")) {
            throw new IllegalArgumentException();
        } else if (attribute.contains("charset")) {
            file.setCharsetName(attribute.getAttribute("charset"));
        } else if (attribute.contains("codepage")) {
            file.setCharsetName(BeanFactory.get(Codepage.class).get(attribute.getAttribute("codepage")));
        }

        if (attribute.contains("chardel")) {
            file.setCharDelimiter(attribute.getAttribute("chardel"));
        }

        if (attribute.contains("rowdel")) {
            file.setLineSeparator(StringUtils.unescape(attribute.getAttribute("rowdel")));
        }

        if (attribute.contains("coldel")) {
            file.setDelimiter(attribute.getAttribute("coldel"));
        }

        if (attribute.contains("escape")) {
            String escape = attribute.getAttribute("escape");
            if (StringUtils.length(escape, StandardCharsets.ISO_8859_1.name()) == 1) {
                file.setEscape(escape.charAt(0));
            } else {
                throw new IllegalArgumentException(escape);
            }
        }

        if (attribute.contains("column")) {
            file.setColumn(Integer.parseInt(attribute.getAttribute("column")));
        }

        // 单独设置表格列名
        if (attribute.contains("colname")) { // name1,2:name2,4:name3
            char s = ',';
            char m = ':';
            String[] names = StringUtils.split(attribute.getAttribute("colname"), s);
            for (int i = 0; i < names.length; i++) {
                String expr = names[i];
                if (StringUtils.isBlank(expr)) {
                    continue;
                }

                if (expr.indexOf(m) != -1) {
                    String[] property = StringUtils.splitProperty(expr, m);
                    if (property != null) {
                        if (StringUtils.isNumber(property[0])) {
                            file.setColumnName(Integer.parseInt(property[0]), property[1]);
                            continue;
                        } else {
                            throw new IllegalArgumentException(property[0]);
                        }
                    }
                } else {
                    file.setColumnName(i + 1, expr);
                }
            }
        }
    }

}
