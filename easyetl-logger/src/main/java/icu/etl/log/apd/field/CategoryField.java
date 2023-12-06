package icu.etl.log.apd.field;

import icu.etl.log.apd.LogEvent;
import icu.etl.log.apd.LogFieldAlign;
import icu.etl.util.StringUtils;

/**
 * %c：输出日志信息所属的类目，通常就是所在类的全名。
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/11/20
 */
public class CategoryField extends AbstractField {

    private int level;

    public CategoryField(int level) {
        this.level = level;
    }

    public String format(LogEvent event) {
        String packageName = event.getCategory();

        // 等于0表示输出类名
        if (level == 0) {
            LogFieldAlign align = this.getAlign();
            if (align != null && align.getMax() >= 0 && packageName.length() > align.getMax()) {
                // 对于超长的类名，要将包名缩减，只保留包名的第一个字符
                StringBuilder buf = new StringBuilder(packageName.length());
                String[] array = StringUtils.split(packageName, '.');
                int last = array.length - 1;
                for (int i = 0; i < last; i++) {
                    buf.append(array[i].charAt(0)).append('.');
                }
                buf.append(array[last]);
                return this.format(buf);
            } else {
                return this.format(packageName);
            }
        }

        String[] array = StringUtils.split(packageName, '.');
        StringBuilder buf = new StringBuilder(packageName.length());

        if (level > 0) { // 从右向左边截取
            int i = array.length - level;
            if (i < 0) {
                i = 0;
            }

            for (; i >= 0 && i < array.length; ) {
                buf.append(array[i]);
                if (++i < array.length) {
                    buf.append('.');
                }
            }
        } else { // level < 0 从左向右边截取
            for (int i = 0, length = Math.min(-level, array.length); i < length; ) {
                buf.append(array[i]);
                if (++i < length) {
                    buf.append('.');
                }
            }
        }

        return this.format(buf);
    }

}
