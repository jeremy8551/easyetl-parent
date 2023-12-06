package icu.etl.database.export.inernal;

import icu.etl.annotation.EasyBean;
import icu.etl.database.export.ExtractReader;
import icu.etl.database.export.ExtracterContext;
import icu.etl.ioc.EasyBeanBuilder;
import icu.etl.ioc.EasyBeanInfo;
import icu.etl.ioc.EasyContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.ClassUtils;
import icu.etl.util.StringUtils;

/**
 * 数据输入流
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-18
 */
@EasyBean
public class ReaderBuilder implements EasyBeanBuilder<ExtractReader> {

    public ExtractReader getBean(EasyContext context, Object... args) throws Exception {
        ExtracterContext cxt = ArrayUtils.indexOf(args, ExtracterContext.class, 0);
        String source = cxt.getSource();
        if (StringUtils.startsWith(source, "select", 0, true, true)) {
            return context.createBean(DatabaseReader.class, "database", cxt);
        }

        Class<Object> cls = ClassUtils.forName(source, false, context.getClassLoader());
        if (cls != null) {
            return (ExtractReader) context.createBean(cls);
        }

        // 解析 http://xxx/xxx/xxx 格式
        String[] split = StringUtils.split(source, "://");
        if (split.length > 0) {
            EasyBeanInfo beanInfo = context.getBeanInfo(ExtractReader.class, split[0]);
            if (beanInfo != null) {
                return (ExtractReader) context.createBean(beanInfo.getType());
            }
        }

        throw new UnsupportedOperationException(source);
    }

}
