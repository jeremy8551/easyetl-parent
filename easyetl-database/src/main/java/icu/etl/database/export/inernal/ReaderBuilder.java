package icu.etl.database.export.inernal;

import icu.etl.database.export.ExtractReader;
import icu.etl.database.export.ExtracterContext;
import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.EasyetlContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.ClassUtils;
import icu.etl.util.StringUtils;

/**
 * 数据输入流
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-18
 */
public class ReaderBuilder implements BeanBuilder<ExtractReader> {

    public ExtractReader build(EasyetlContext context, Object... array) throws Exception {
        ExtracterContext cxt = ArrayUtils.indexOf(array, ExtracterContext.class, 0);
        String source = cxt.getSource();

        if (StringUtils.startsWith(source, "select", 0, true, true)) {
            return new DatabaseReader(cxt);
        } else if (ClassUtils.forName(source) != null) {
            return (ExtractReader) ClassUtils.newInstance(source);
        }

        // 解析 http://xxx/xxx/xxx 格式
        String[] split = StringUtils.split(source, "://");
        if (split.length > 0) {
            Class<ExtractReader> cls = context.getImplement(ExtractReader.class, split[0]);
            if (cls != null) {
                return ClassUtils.newInstance(cls);
            }
        }

        throw new UnsupportedOperationException(source);
    }

}
